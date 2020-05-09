package com.baijudodhia.photoml.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.baijudodhia.photoml.R;
import com.baijudodhia.photoml.adapters.PhotoGalleryAdapter;

import java.lang.String;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public final static int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    ImageView photoItemView;
    PhotoGalleryAdapter photoGalleryAdapter;
    int numberOfColumns = 3;
    ArrayList<String> photoItemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Asking for permission is not granted
//            Toast.makeText(getApplicationContext(), "Permission Not Granted, So Asking", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            photoItemList = FetchExternalStorageImageMedia(this);
        } else {
            photoItemList = FetchExternalStorageImageMedia(this);
        }

        RecyclerView recyclerView = findViewById(R.id.Activity_RecyclerViewPhotoGallery);
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        adapter = new ImageAdapter(this, imagesList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(48);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        recyclerView.setAdapter(adapter);


        adapter.setClickListener(new ImageAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(MainActivity.this, imagesList.get(position), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public ArrayList<String> FetchExternalStorageImageMedia(Activity context) {
        ArrayList<String> galleryImageUrls = new ArrayList<String>();
        Cursor mCursor = getContentResolver()
                .query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null,
                        null,
                        null,
                        MediaStore.Images.Media.DEFAULT_SORT_ORDER);
        mCursor.moveToFirst();
        while (!mCursor.isAfterLast()) {
//        for (int i = 0; i < 20; i++) {
//            System.out.println("_ID :         " + mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media._ID)));
//            System.out.println("File Name :   " + mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)));
            String data = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
//            System.out.println("File Path :   " + data);
//            System.out.println("---------------");
            galleryImageUrls.add(data);
            mCursor.moveToNext();
        }
        mCursor.close();
        return galleryImageUrls;
    }

    public ArrayList<Bitmap> generateBitmap(ArrayList<String> imagesList) {
        ArrayList<Bitmap> bitmapArrayList = new ArrayList<>();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightD = displayMetrics.heightPixels;
        int widthD = displayMetrics.widthPixels;

        BitmapFactory.Options options = new BitmapFactory.Options();


        int arraysize = imagesList.size();

        for (int i = 0; i < arraysize; i++) {

            System.out.println("i - " + i);

            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagesList.get(i), options);
//        BitmapFactory.decodeResource(getResources(), bitmap, options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;
            String imageType = options.outMimeType;
            int inSampleSize = 1;

            if (imageHeight > (widthD / 3) || imageWidth > (widthD / 3)) {

                final int halfHeight = imageHeight / 2;
                final int halfWidth = imageWidth / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) >= (widthD / 3)
                        && (halfWidth / inSampleSize) >= (widthD / 3)) {
                    inSampleSize *= 2;
                }
            }

            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = false;

            Bitmap resultBit = BitmapFactory.decodeFile(imagesList.get(i), options);

            bitmapArrayList.add(resultBit);
        }

//        System.out.println(imagesList.get(0) + "\n");
//        System.out.println("h - " + imageHeight + " w - " + imageWidth + " ty - " + imageType);


//        System.out.println("sample size - " + inSampleSize);
//
//
//        ArrayList<Integer> testList = new ArrayList<>(0);
//        testList.add(R.drawable.ic_launcher_background);
//        testList.add(R.drawable.ic_launcher_foreground);

//        for (int i = 0; i < imagesList.size(); i++) {
//            bitmapArrayList.add(resultBit);
//        }
//        bitmapArrayList.add(resultBit);

        return bitmapArrayList;
    }

}