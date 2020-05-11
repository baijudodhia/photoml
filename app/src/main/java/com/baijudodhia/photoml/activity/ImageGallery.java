package com.baijudodhia.photoml.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baijudodhia.photoml.R;
import com.baijudodhia.photoml.adapters.ImageGalleryAdapter;

import java.util.ArrayList;

public class ImageGallery extends AppCompatActivity {
    public static float deviceWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    public static float deviceHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    public int numberOfColumns;
    public RecyclerView recyclerView;
    ImageGalleryAdapter imageGalleryAdapter;
    ArrayList<String> imagesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagegallery);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#F5F5F5\">Photos</font>"));

        //Initialization for numberOfColumns for GridLayoutManager during App launch
        if (deviceHeight > deviceWidth) {
            numberOfColumns = 3;
        } else {
            numberOfColumns = 5;
        }

        imagesList = FetchExternalStorageImageMedia(this);
        imageGalleryAdapter = new ImageGalleryAdapter(this, imagesList);

        recyclerView = findViewById(R.id.Activity_RecyclerViewPhotoGallery);
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
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

    public ArrayList<String> FetchExternalStorageImageMedia(Activity context) {
        ArrayList<String> externalStorageImageUri = new ArrayList<String>();
        Cursor mCursor = getContentResolver()
                .query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null,
                        null,
                        null,
                        MediaStore.Images.Media.DATE_TAKEN + " DESC");
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            numberOfColumns = 5;
            recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            numberOfColumns = 3;
            recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        }
    }
}