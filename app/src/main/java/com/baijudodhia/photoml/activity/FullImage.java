package com.baijudodhia.photoml.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

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
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));
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
        if (id == R.id.menu_actionbar_tab) {
            Intent intent = new Intent(this, ImageScan.class);
            intent.putExtra("imagePath", imagePath);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    //Show AlertDialog containing result of Firebase ML-Kit detection
    public void ShowDetection(final String title, final StringBuilder builder) {
        final AlertDialog.Builder alertbuilder = new AlertDialog.Builder(this);
        alertbuilder.setCancelable(true);
        alertbuilder.setTitle(title);
        if (builder.length() != 0) {
            alertbuilder.setMessage(builder);
        } else {
            alertbuilder.setMessage(title.substring(0, title.indexOf(' ')) + " detector didn't find anything!");
        }
        alertbuilder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        if (title.contentEquals("OCR Detection") && builder.length() != 0) {
            alertbuilder.setNeutralButton("COPY TEXT", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(title, builder);
                    clipboard.setPrimaryClip(clip);
                    Snackbar.make(findViewById(R.id.fullimage), "Copied!", 2000).show();
                }
            });
        }
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
                        if (labeltext.size() != 0) {
                            if (labeltext.size() == 1) {
                                builder.append(labeltext.size() + " Label detected\n\n");
                            } else {
                                builder.append(labeltext.size() + " Labels detected\n\n");
                            }
                        }
                        int i = 1;
                        for (String label : labeltext) {
                            builder.append(i + ". " + label + "\n");
                            i++;
                        }
                        ShowDetection("Label Detection", builder);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("Apologies :(\nLabel detector encountered a problem!\n");
                        ShowDetection("Label Detection", builder);
                    }
                });
    }

    public void OCRDetection() {
        final StringBuilder builder = new StringBuilder();
        BitmapDrawable drawable = (BitmapDrawable) fullImageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                            /* Not used since App is directly extracting the line and doesn't require the blocks for display
                            String blockText = block.getText();
                            Float blockConfidence = block.getConfidence();
                            List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
                            Point[] blockCornerPoints = block.getCornerPoints();
                            Rect blockFrame = block.getBoundingBox();
                            */
                            for (FirebaseVisionText.Line line : block.getLines()) {
                                String lineText = line.getText();
                                builder.append(lineText + "\n");
                                /* Not used since not required for display
                                Float lineConfidence = line.getConfidence();
                                List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                                Point[] lineCornerPoints = line.getCornerPoints();
                                Rect lineFrame = line.getBoundingBox();
                                */
                                /* Not used since App is directly extracting the line and doesn't require individual element (Word/Character) for display
                                for (FirebaseVisionText.Element element : line.getElements()) {
                                    String elementText = element.getText();
                                    Float elementConfidence = element.getConfidence();
                                    List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
                                    Point[] elementCornerPoints = element.getCornerPoints();
                                    Rect elementFrame = element.getBoundingBox();
                                }
                                */
                            }
                        }
                        ShowDetection("OCR Detection", builder);
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                StringBuilder builder = new StringBuilder();
                                builder.append("Apologies :(\nOCR detector encountered a problem!\n");
                                ShowDetection("OCR Detection", builder);
                            }
                        });
    }

    //Face Detection
    public void FaceDetection() {
        final StringBuilder builder = new StringBuilder();
        BitmapDrawable drawable = (BitmapDrawable) fullImageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionFaceDetectorOptions highAccuracyOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .enableTracking()
                        .build();
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(highAccuracyOpts);
        detector.detectInImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<List<FirebaseVisionFace>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionFace> faces) {
                                // Task completed successfully
                                if (faces.size() != 0) {
                                    if (faces.size() == 1) {
                                        builder.append(faces.size() + " Face detected\n\n");
                                    } else {
                                        builder.append(faces.size() + " Faces detected\n\n");
                                    }
                                }
                                for (FirebaseVisionFace face : faces) {
                                    int id = face.getTrackingId();
                                    float rotY = face.getHeadEulerAngleY(); // Head is rotated to the right rotY degrees
                                    float rotZ = face.getHeadEulerAngleZ(); // Head is tilted sideways rotZ degrees
                                    builder.append("1. Face Tracking ID [" + id + "]\n");
                                    builder.append("2. Head Rotation to Right [" + rotY + "]\n");
                                    builder.append("3. Head Tilted Sideways [" + rotZ + "]\n");
                                    if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                        if (face.getSmilingProbability() > 0) {
                                            float SmilingProbability = face.getSmilingProbability();
                                            builder.append("4. Smiling Probability [" + SmilingProbability + "]\n");
                                        }
                                    }
                                    if (face.getLeftEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                        if (face.getLeftEyeOpenProbability() > 0) {
                                            float LeftEyeOpenProbability = face.getLeftEyeOpenProbability();
                                            builder.append("5. Left Eye Open Probability [" + LeftEyeOpenProbability + "]\n");
                                        }
                                    }
                                    if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                        if (face.getRightEyeOpenProbability() > 0) {
                                            float RightEyeOpenProbability = face.getRightEyeOpenProbability();
                                            builder.append("6. Right Eye Open Probability [" + RightEyeOpenProbability + "]\n");
                                        }
                                    }
                                    builder.append("\n");
                                }
                                ShowDetection("Face Detection", builder);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                StringBuilder builder = new StringBuilder();
                                builder.append("Apologies :(\nFace detector encountered a problem!\n");
                                ShowDetection("Face Detection", builder);
                            }
                        });
    }

    //Barcode Detection
    public void BarcodeDetection() {
        final StringBuilder builder = new StringBuilder();
        BitmapDrawable drawable = (BitmapDrawable) fullImageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector();
        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                        // Task completed successfully
                        for (FirebaseVisionBarcode barcode : barcodes) {
                            /* Not used for display
                            Rect bounds = barcode.getBoundingBox();
                            Point[] corners = barcode.getCornerPoints();
                            String rawValue = barcode.getRawValue();
                            */
                            int valueType = barcode.getValueType();
                            // Switch case for supported value types for barcodes
                            switch (valueType) {
                                case FirebaseVisionBarcode.TYPE_WIFI:
                                    String ssid = barcode.getWifi().getSsid();
                                    String password = barcode.getWifi().getPassword();
                                    int type = barcode.getWifi().getEncryptionType();
                                    int wifi_item = 1;
                                    builder.append("WiFi Barcode - \n");
                                    if (!ssid.isEmpty()) {
                                        builder.append(wifi_item++ + ". SSID - " + ssid + "\n");
                                    }
                                    if (!password.isEmpty()) {
                                        builder.append(wifi_item++ + ". Password - " + password + "\n");
                                    }
                                    builder.append(wifi_item++ + ". Encryption Type - " + type + "\n");
                                    ShowDetection("Barcode Detection", builder);
                                    break;
                                case FirebaseVisionBarcode.TYPE_URL:
                                    String title = barcode.getUrl().getTitle();
                                    String url = barcode.getUrl().getUrl();
                                    int url_item = 1;
                                    builder.append("URL Barcode - \n");
                                    if (title.length() != 0) {
                                        builder.append(url_item++ + ". Title - " + title + "\n");
                                    }
                                    if (!url.isEmpty()) {
                                        builder.append(url_item++ + ". URL - " + url + "\n");
                                    }
                                    ShowDetection("Barcode Detection", builder);
                                    break;
                                default:
                                    break;
                            }
                        }
                        if (builder.length() == 0) {
                            ShowDetection("Barcode Detection", builder);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        StringBuilder builder = new StringBuilder();
                        builder.append("Apologies :(\nBarcode Detector encountered a problem!\n");
                        ShowDetection("Barcode Detection", builder);
                    }
                });
    }
}