package com.baijudodhia.photoml.commons;

import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.baijudodhia.photoml.activity.FullImage;

public class Alerts extends FullImage {
    //Show AlertDialog containing result of Firebase ML-Kit detection
    public void ShowMessage(String title, StringBuilder builder) {
        AlertDialog.Builder alertbuilder = new AlertDialog.Builder(this);
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
}
