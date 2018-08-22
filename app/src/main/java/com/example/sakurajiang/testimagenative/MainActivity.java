package com.example.sakurajiang.testimagenative;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sakurajiang.testimagenative.utils.NativeBitmapUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    public TextView my_tv;
    public ImageView originIV;
    public ImageView compressIV;
    public ImageView nativeCompressIV;
    public EditText qualityET;
    public LinearLayout my_ll;
    public static final int GALLERY_REQUEST_CODE = 1;
    public Bitmap origin_bm;
    public Bitmap compress_bm;
    public Bitmap native_compress_bm;
    public final String fileName = System.currentTimeMillis() + ".jpg";
    public static final String TAG = "MainActivity";
    public int compress_bitmap_file_size;
    public int native_compress_bitmap_file_size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        my_ll=findViewById(R.id.my_ll);
        my_tv = findViewById(R.id.show_message);
        qualityET = findViewById(R.id.my_et);
        originIV = findViewById(R.id.origin_iv);
        compressIV = findViewById(R.id.compress_iv);
        nativeCompressIV = findViewById(R.id.native_compress_iv);
        initListener();
    }

    public void initListener(){
        my_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePhoto();
            }
        });
        qualityET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (origin_bm == null) {
                    Toast.makeText(MainActivity.this, getResources().getText(R.string.select_image), Toast.LENGTH_LONG).show();
                }
                compressImage();
            }
        });
    }

    public void compressImage() {
        if(origin_bm==null){
            Toast.makeText(this,getResources().getString(R.string.select_image),Toast.LENGTH_LONG).show();
            return;
        }
        createFile(fileName);
        compress_bm = compressBitmap(origin_bm, TextUtils.isEmpty(qualityET.getText()) ? 50 : Integer.valueOf(qualityET.getText().toString()));
        if (NativeBitmapUtils.compressBitmapWithNative(origin_bm, origin_bm.getWidth(), origin_bm.getHeight(), "/data/data/com.example.sakurajiang.testimagenative/files/" + fileName, TextUtils.isEmpty(qualityET.getText()) ? 50 : Integer.valueOf(qualityET.getText().toString()))) {
            try {
                FileInputStream fileInputStream = openFileInput(fileName);
                FileInputStream  fileInputStream1  = openFileInput(fileName);
                byte[] byt = new byte[4096];
                Log.i(TAG,""+fileInputStream1.read(byt)/1024);
                native_compress_bitmap_file_size = byt.length/1024;
                native_compress_bm = BitmapFactory.decodeStream(fileInputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        initView();
    }

    public void initView(){
        originIV.setImageBitmap(origin_bm);
        compressIV.setImageBitmap(compress_bm);
        nativeCompressIV.setImageBitmap(native_compress_bm);
        Log.i(TAG, ""+origin_bm.getByteCount()/1024+compress_bm.getByteCount()/1024+native_compress_bm.getByteCount()/1024);
        my_tv.setText(String.format(getResources().getString(R.string.image_size), getResources().getString(R.string.compress_image), compress_bitmap_file_size)
        +"\n"+String.format(getResources().getString(R.string.image_size), getResources().getString(R.string.native_compress_image), native_compress_bitmap_file_size));
    }

    public Bitmap compressBitmap(Bitmap bitmap,int quality){
        if(bitmap!=null){
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,quality,byteArrayOutputStream);
            compress_bitmap_file_size = byteArrayOutputStream.toByteArray().length/1024;
            Log.i(TAG, "byteArrayOutputStream"+byteArrayOutputStream.toByteArray().length/1024);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            return BitmapFactory.decodeStream(byteArrayInputStream);
        }
        return null;
    }

    public void createFile(String fileName){
        try {
            openFileOutput(fileName, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void choosePhoto(){
        Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/jpeg");
        startActivityForResult(intentToPickPic, GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==GALLERY_REQUEST_CODE&&data!=null){
                try {
                    if(data.getData()!=null) {
                        origin_bm= BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData()));
                        compressImage();
                        Log.i(TAG, String.valueOf(origin_bm));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
