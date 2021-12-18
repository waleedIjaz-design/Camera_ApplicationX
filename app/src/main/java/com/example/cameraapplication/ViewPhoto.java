package com.example.cameraapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.jar.Attributes;

public class ViewPhoto extends AppCompatActivity {


    ImageView imigic2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);
        imigic2 = findViewById(R.id.imigic2);

        String path = getIntent().getStringExtra("path");
        Log.d("path", "" + path);
        File imageFile = new File(path);
        if (imageFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(path);
            imigic2.setImageBitmap(myBitmap);
        }

    }


}