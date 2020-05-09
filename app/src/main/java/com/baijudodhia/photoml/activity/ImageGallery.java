package com.baijudodhia.photoml.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Gallery;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baijudodhia.photoml.R;
import com.baijudodhia.photoml.adapters.ImageGalleryAdapter;

import java.util.ArrayList;

public class ImageGallery extends AppCompatActivity {
    ImageGalleryAdapter imageGalleryAdapter;
    int numberOfColumns = 3;
    ArrayList<String> imagesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagegallery);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFF")));
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#000\">Photos</font>"));
        imagesList = getIntent().getStringArrayListExtra("imagesList");
        RecyclerView recyclerView = findViewById(R.id.Activity_RecyclerViewPhotoGallery);
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        imageGalleryAdapter = new ImageGalleryAdapter(this, imagesList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(imageGalleryAdapter);
        imageGalleryAdapter.setClickListener(new ImageGalleryAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent fullimage = new Intent(ImageGallery.this, FullImage.class);
                fullimage.putExtra("imagePath", imagesList.get(position));
                startActivity(fullimage);
            }
        });
    }
}