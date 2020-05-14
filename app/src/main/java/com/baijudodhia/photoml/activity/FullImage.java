package com.baijudodhia.photoml.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.baijudodhia.photoml.R;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

public class FullImage extends AppCompatActivity {
    String s_ImagePath;
    PhotoView pv_FullPhotoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullimage);

        getSupportActionBar().setTitle(null);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        pv_FullPhotoView = findViewById(R.id.pv_fullimage);
        s_ImagePath = i.getExtras().getString("s_ImagePath");
        System.out.println(s_ImagePath);
        Glide.with(this)
                .load(s_ImagePath)
                .into(pv_FullPhotoView);
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
        if (id == R.id.menu_actionbar_firebaseimageml) {
            Intent intent = new Intent(this, FirebaseImageML.class);
            intent.putExtra("s_ImagePath", s_ImagePath);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}