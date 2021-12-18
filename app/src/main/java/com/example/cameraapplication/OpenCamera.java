package com.example.cameraapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class OpenCamera extends AppCompatActivity {


    private int REQUEST_CODE_PERMISSIONS = 101;

    private String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    ImageButton btnCapture;
    ImageButton flashLightOn, flashLightOff, openGallery, flipCamera;

    ProcessCameraProvider cameraProvider;
    androidx.camera.core.Camera camera;
    PreviewView previewViewsss;
    boolean isFlashOn = false;
    public ImageCapture imageCapture;
    CameraManager camManager;
    int lenceFacing;
    int REQUEST_GALLERY = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_camera);

        previewViewsss = findViewById(R.id.previewViewsss);
        btnCapture = findViewById(R.id.btnCapture);
        flipCamera = findViewById(R.id.flipCamera);
        flashLightOn = findViewById(R.id.flashLightOn);
        flashLightOff = findViewById(R.id.flashLightOff);
        openGallery = findViewById(R.id.openGallery);
        camManager = (CameraManager) getSystemService(CAMERA_SERVICE);


        //Events

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capturePhoto();
            }
        });



        flashLightOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFlashOn == false) {
                    flashLightOn.setEnabled(true);
                    on();
                }
            }
        });


        flashLightOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFlashOn == true) {
                    flashLightOff.setEnabled(true);
                    off();
                }
            }
        });

        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_GALLERY);
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);


            }
        });



        //runtime permissions asked to user
        if (allPermissionGranted()) {
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        //Procedure of Camera Appear on Screen
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {

            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                lenceFacing = CameraSelector.LENS_FACING_BACK;

                startCameraX( cameraProvider, lenceFacing);

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }, getExecutor());

    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }


    // Start the camera after permissions granted by user
    private void startCameraX(ProcessCameraProvider cameraProvider, int lenceFacing) {
        cameraProvider.unbindAll();

        this.cameraProvider = cameraProvider;


        //camera selector use case
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lenceFacing)
                .build();


        //preview use case
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewViewsss.getSurfaceProvider());


        //image capture use case
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        this.camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);

    }


    public void fliipBtn(View view) {
        cameraProvider.unbindAll();
        if (lenceFacing == CameraSelector.LENS_FACING_FRONT) {
            lenceFacing = CameraSelector.LENS_FACING_BACK;
            startCameraX(cameraProvider, lenceFacing);
        } else {
            lenceFacing = CameraSelector.LENS_FACING_FRONT;
            startCameraX(cameraProvider, lenceFacing);
        }
    }


    public void on() {
        if (isFlashOn == true) {
            Toast.makeText(getApplicationContext(), "flashlight is already on", Toast.LENGTH_SHORT).show();
        } else {
            camera.getCameraControl().enableTorch(true);
            isFlashOn = true;
        }
    }


    public void off() {
        if (isFlashOn == false) {
            Toast.makeText(getApplicationContext(), "flashlight is already off", Toast.LENGTH_SHORT).show();
        } else {
            camera.getCameraControl().enableTorch(false);
            isFlashOn = false;
        }
    }


    //Save picture to storage after capture
    private void capturePhoto() {
        Toast.makeText(this, "Captured", Toast.LENGTH_SHORT).show();
        File photoDir = new File("/sdcard/Pictures/CameraXPhotos");

        if (!photoDir.exists()) {
            photoDir.mkdir();
        }
        Date date = new Date();
        String timeStamp = String.valueOf(date.getTime());
        String photoFilePath = photoDir.getAbsolutePath() + "/" + timeStamp + ".jpg";

        File photoFile = new File(photoFilePath);


        //Image Capture
        imageCapture.takePicture
                (new ImageCapture.OutputFileOptions.Builder(photoFile).build(),
                        getExecutor(),
                        new ImageCapture.OnImageSavedCallback() {
                            @Override
                            public void onImageSaved(@NonNull @NotNull ImageCapture.OutputFileResults outputFileResults) {

                                Toast.makeText(OpenCamera.this, "Photo has been saved successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), ViewPhoto.class);
                                intent.putExtra("path", "" + photoFile);
                                startActivity(intent);
                            }


                            @Override
                            public void onError(@NonNull @NotNull ImageCaptureException exception) {
                                Toast.makeText(OpenCamera.this, "Error photo saving " + exception.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });


    }

    //All permissions asked
    private boolean allPermissionGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {

            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


}