package com.baijudodhia.photoml.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.baijudodhia.photoml.R;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

public class FullImage extends AppCompatActivity {
    String imagePath;
    PhotoView fullImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullimage);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent i = getIntent();
        fullImageView = findViewById(R.id.fullimage);
        imagePath = i.getExtras().getString("imagePath");
        Glide.with(this)
                .load(imagePath)
                .into(fullImageView);
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actionbar_fullimage, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_actionbar_label) {
            //Call method to perform Label Detection
        }
        if (id == R.id.menu_actionbar_ocr) {
            //Call method to perform OCR
        }
        if (id == R.id.menu_actionbar_face) {
            //Call method to perform Face Detection
        }
        if (id == R.id.menu_actionbar_barcode) {
            //Call method to perform Barcode Detection
        }
        return super.onOptionsItemSelected(item);
    }
}