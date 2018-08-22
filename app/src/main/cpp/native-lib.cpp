#include <jni.h>
#include <android/bitmap.h>
#include <android/log.h>
#include <string>

extern "C" {
#include "jpeglib.h"
}

typedef uint8_t BYTE;
#define TAG "sakurajiang"
#define LOGE(...) __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__)
#define true 1
#define false 0

int generateJPEG(BYTE *data, int w, int h, int quality, const char *outfile, jboolean optimize){
    int nComponent = 3;

    struct jpeg_compress_struct jcs;

    struct jpeg_error_mgr jem;

    jcs.err = jpeg_std_error(&jem);

    //2.为JPEG对象分配空间并初始化
    jpeg_create_compress(&jcs);
    //3.获取文件信息
    FILE *file = fopen(outfile, "wb");
    if (file == NULL) {
        return 0;
    }
    //4.指定压缩数据源
    jpeg_stdio_dest(&jcs, file);
    //image_width->JDIMENSION->typedef unsigned int

    jcs.image_width = w;
    jcs.image_height = h;
    //false 使用哈夫曼算法
    jcs.arith_code = false;
    //input_components为1代表灰度图，在等于3时代表彩色位图图像
    jcs.input_components = nComponent;
    if (nComponent == 1) {
        //in_color_space为JCS_GRAYSCALE表示灰度图，在等于JCS_RGB时代表彩色位图图像
        jcs.in_color_space = JCS_GRAYSCALE;
    } else {
        jcs.in_color_space = JCS_RGB;
    }

    jpeg_set_defaults(&jcs);
    //optimize_coding为TRUE，将会使得压缩图像过程中基于图像数据计算哈弗曼表，由于这个计算会显著消耗空间和时间，默认值被设置为FALSE。
    jcs.optimize_coding = optimize;
    //5. 为压缩设定参数，包括图像大小，颜色空间
    jpeg_set_quality(&jcs, quality, true);

    //6.开始压缩
    jpeg_start_compress(&jcs, TRUE);

    JSAMPROW row_pointer[1];
    int row_stride;
    //行宽经过compress中循环变为了image宽度的3倍了，需要通过循环截成正常宽度
    row_stride = jcs.image_width * nComponent;
    while (jcs.next_scanline < jcs.image_height) {
        row_pointer[0] = reinterpret_cast<JSAMPROW>(&data[jcs.next_scanline * row_stride]);
        //写入数据 http://www.cnblogs.com/darkknightzh/p/4973828.html
        jpeg_write_scanlines(&jcs, row_pointer, 1);
    }

    //7.压缩完毕
    jpeg_finish_compress(&jcs);
    //8.释放资源
    jpeg_destroy_compress(&jcs);
    fclose(file);

    return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean

JNICALL
Java_com_example_sakurajiang_testimagenative_utils_NativeBitmapUtils_compressBitmapWithNative(
        JNIEnv *env,
        jobject thiz,
        jobject bitmap, int w, int h, jstring jfilepath,
        jint quality) {
    AndroidBitmapInfo bitmapInfo;
    BYTE *pixelColor;
    BYTE *data;
    BYTE *tempData;
    const char *filepath = env->GetStringUTFChars(jfilepath, 0);
    LOGE("------ 文件目录 %s", filepath);
    if(AndroidBitmap_getInfo(env,bitmap,&bitmapInfo)) {
        LOGE("parse error");
        env->ReleaseStringUTFChars(jfilepath, filepath);
        return false;
    }
    //锁住 与操作canvas类似，操作前先锁住
    if ((AndroidBitmap_lockPixels(env, bitmap, (void **) &pixelColor)) < 0) {
        LOGE("lock pixels error");
        env->ReleaseStringUTFChars(jfilepath, filepath);
        return false;
    }
    BYTE r,g,b;
    int color;
    data = (BYTE *) malloc(w*h*3);
    tempData = data;
    for(int i=0;i<w;i++){
        for(int j=0;j<h;j++){
            color = *((int *) pixelColor);
            r = ((color&0x00FF0000)>>16);
            g = ((color&0x0000FF00)>>8);
            b = color&0x000000FF;
            *data = b;
            *(data + 1) = g;
            *(data + 2) = r;
            data += 3;
            pixelColor += 4;
        }
    }
    AndroidBitmap_unlockPixels(env,bitmap);
    int resultCode = generateJPEG(tempData,w,h,quality,filepath, true);
    LOGE("------压缩完成 0 失败 ，1 成功 resultCode= %d", resultCode);

    free(tempData);
    if (resultCode == 0) {
        env->ReleaseStringUTFChars(jfilepath, filepath);
        return false;
    }

    env->ReleaseStringUTFChars(jfilepath, filepath);

    return true;
}




