package com.baijudodhia.photoml.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.baijudodhia.photoml.R;
import com.baijudodhia.photoml.adapters.ImageGalleryAdapter;

import java.util.ArrayList;

public class ImageGallery extends AppCompatActivity {
    public static float sf_DeviceWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    public static float sf_DeviceHEight = Resources.getSystem().getDisplayMetrics().heightPixels;
    public int i_ColumnCount;
    public RecyclerView recyclerView;
    ImageGalleryAdapter imageGalleryAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    ArrayList<String> arrls_ImagePath = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagegallery);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#F5F5F5\">Photos</font>"));

        //Initialization for i_ColumnCount for GridLayoutManager during App launch
        if (sf_DeviceHEight > sf_DeviceWidth) {
            i_ColumnCount = 3;
        } else {
            i_ColumnCount = 5;
        }

        SetLayout();

        swipeRefreshLayout = findViewById(R.id.srl_imagegallery);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Code to retrieve newly added images from device
                SetLayout();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public ArrayList<String> FetchExternalStorageImageMedia() {
        ArrayList<String> externalStorageImageUri = new ArrayList<>();
        Cursor mCursor = getContentResolver()
                .query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null,
                        null,
                        null,
                        MediaStore.Images.Media.DATE_TAKEN + " DESC");
        mCursor.moveToFirst();
        while (!mCursor.isAfterLast()) {
            String s_ImagePath = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            externalStorageImageUri.add(s_ImagePath);
            mCursor.moveToNext();
        }
        mCursor.close();
        return externalStorageImageUri;
    }

    public void SetLayout() {
        arrls_ImagePath = FetchExternalStorageImageMedia();
        imageGalleryAdapter = new ImageGalleryAdapter(this, arrls_ImagePath);

        recyclerView = findViewById(R.id.rv_imagegallery);
        recyclerView.setLayoutManager(new GridLayoutManager(this, i_ColumnCount));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(imageGalleryAdapter);

        imageGalleryAdapter.setClickListener(new ImageGalleryAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(ImageGallery.this, FullImage.class);
                intent.putExtra("s_ImagePath", arrls_ImagePath.get(position));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            i_ColumnCount = 5;
            recyclerView.setLayoutManager(new GridLayoutManager(this, i_ColumnCount));
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            i_ColumnCount = 3;
            recyclerView.setLayoutManager(new GridLayoutManager(this, i_ColumnCount));
        }
    }
}