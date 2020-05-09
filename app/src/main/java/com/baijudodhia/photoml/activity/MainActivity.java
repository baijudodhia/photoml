package com.baijudodhia.photoml.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baijudodhia.photoml.R;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public final static int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    ArrayList<String> imagesList = new ArrayList<>();

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
                Toast.makeText(MainActivity.this, "PhotoML can't function with No Storage Permissions!", Toast.LENGTH_SHORT).show();
            } else {
                // Request the permission
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        imagesList = FetchExternalStorageImageMedia(MainActivity.this);
                        Intent intent = new Intent(MainActivity.this, ImageGallery.class);
                        intent.putStringArrayListExtra("imagesList", imagesList);
                        startActivity(intent);
                    }
                }, WELCOME_SCREEN_TIMEOUT);
            }
        } else {
            // Permission has already been granted
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    imagesList = FetchExternalStorageImageMedia(MainActivity.this);
                    Intent intent = new Intent(MainActivity.this, ImageGallery.class);
                    intent.putStringArrayListExtra("imagesList", imagesList);
                    startActivity(intent);
                }
            }, WELCOME_SCREEN_TIMEOUT);
        }
    }

    public ArrayList<String> FetchExternalStorageImageMedia(Activity context) {
        ArrayList<String> externalStorageImageUri = new ArrayList<String>();
        Cursor mCursor = getContentResolver()
                .query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null,
                        null,
                        null,
                        MediaStore.Images.Media.DEFAULT_SORT_ORDER);
        mCursor.moveToFirst();
        while (!mCursor.isAfterLast()) {
            String data = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            System.out.println(data + "\n");
            externalStorageImageUri.add(data);
            mCursor.moveToNext();
        }
        mCursor.close();
        return externalStorageImageUri;
    }
}
