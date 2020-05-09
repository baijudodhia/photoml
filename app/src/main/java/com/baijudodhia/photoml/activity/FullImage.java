package com.baijudodhia.photoml.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.baijudodhia.photoml.R;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;

import java.util.ArrayList;
import java.util.List;

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
            LabelDetection();
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

    //Show AlertDialog containing result of Firebase ML-Kit detection
    public void showmessage(String title, StringBuilder builder) {
        final AlertDialog.Builder alertbuilder = new AlertDialog.Builder(this);
        alertbuilder.setCancelable(true);
        alertbuilder.setTitle(title);
        if (builder.length() != 0) {
            alertbuilder.setMessage(builder);
        } else {
            alertbuilder.setMessage(title + " not detected!");
        }
        alertbuilder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertbuilder.show();
    }

    //Label Detection
    public void LabelDetection() {
        final StringBuilder builder = new StringBuilder();
        final ArrayList<String> labeltext = new ArrayList<String>();
        BitmapDrawable drawable = (BitmapDrawable) fullImageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler();
        labeler.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                        for (FirebaseVisionImageLabel label : labels) {
                            String text = label.getText();
                            float confidence = label.getConfidence();
                            //Only add labels with confidence greater than 0.5
                            if (confidence > 0) {
                                labeltext.add(text + " [" + String.format("%.2f", confidence) + " conf]");
                            }
                        }
                        if (labels.size() != 0) {
                            builder.append("Labels Detected\n");
                        } else {
                            builder.append("Labels Not Detected!");
                        }
                        int i = 1;
                        for (String label : labeltext) {
                            builder.append(i + ". " + label + "\n");
                            i++;
                        }
                        builder.append("\n");
                        showmessage("Label Detecion", builder);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("Apologies :(\nLabel Detector encountered a problem!\n");
                        showmessage("Label Detecion", builder);
                    }
                });
    }
}