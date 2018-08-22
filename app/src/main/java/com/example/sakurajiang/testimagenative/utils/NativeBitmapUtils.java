package com.example.sakurajiang.testimagenative.utils;

import android.graphics.Bitmap;

public class NativeBitmapUtils {
    static {
        System.loadLibrary("native-lib");
    }

    public static native boolean compressBitmapWithNative(Bitmap bitmap, int width, int height, String filePath,
                                        int quality);
}
