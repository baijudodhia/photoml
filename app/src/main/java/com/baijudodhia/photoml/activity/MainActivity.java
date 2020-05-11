package com.baijudodhia.photoml.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baijudodhia.photoml.R;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {
    public final static int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int WELCOME_SCREEN_TIMEOUT = 2000;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        //Initialize FirebaseApp on PhotoML App start
        FirebaseApp.initializeApp(this);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Explain request for permission and try again
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, ImageGallery.class);
                        startActivity(intent);
                    }
                }, WELCOME_SCREEN_TIMEOUT);
            } else {
                // Request the permission
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, ImageGallery.class);
                        startActivity(intent);
                    }
                }, WELCOME_SCREEN_TIMEOUT);
            }
        } else {
            // Permission has already been granted
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(MainActivity.this, ImageGallery.class);
                    startActivity(intent);
                }
            }, WELCOME_SCREEN_TIMEOUT);
        }
    }
}
