package com.baijudodhia.photoml.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.baijudodhia.photoml.R;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
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

public class FirebaseImageML extends AppCompatActivity {
    public static float sf_DeviceWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    public static float sf_DeviceHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    String s_ImagePath;
    PhotoView pv_PhotoPreview;
    MaterialTextView mTextView;
    MaterialButton mbtn_LabelDetection, mbtn_OcrDetection, mbtn_FaceDetection, mbtn_BarcodeDetection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebaseimageml);

        getSupportActionBar().setTitle(null);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        s_ImagePath = intent.getExtras().getString("s_ImagePath");
        mTextView = findViewById(R.id.mtv_firebaseimageml_results);
        pv_PhotoPreview = findViewById(R.id.pv_firebaseimageml);
        mbtn_LabelDetection = findViewById(R.id.mbtn_firebaseimageml_label);
        mbtn_OcrDetection = findViewById(R.id.mbtn_firebaseimageml_ocr);
        mbtn_FaceDetection = findViewById(R.id.mbtn_firebaseimageml_face);
        mbtn_BarcodeDetection = findViewById(R.id.mbtn_firebaseimageml_barcode);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            float screenRatio = sf_DeviceWidth / sf_DeviceHeight;
            float photoViewHeight = sf_DeviceWidth * screenRatio;
            pv_PhotoPreview.getLayoutParams().height = (int) photoViewHeight;
            pv_PhotoPreview.requestLayout();
        }

        Glide.with(this)
                .load(s_ImagePath)
                .into(pv_PhotoPreview);

        mbtn_LabelDetection.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                LabelDetection();
            }
        });

        mbtn_OcrDetection.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                OCRDetection();
            }
        });

        mbtn_FaceDetection.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                FaceDetection();
            }
        });

        mbtn_BarcodeDetection.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                BarcodeDetection();
            }
        });
    }

    //Show result of Firebase ML-Kit detection in TextView
    public void ShowDetection(final String title, final StringBuilder builder, boolean success) {
        if (success == true) {
            mTextView.setText(null);
            mTextView.setMovementMethod(new ScrollingMovementMethod());
            if (builder.length() != 0) {
                mTextView.append(builder);
                if (title.substring(0, title.indexOf(' ')).equalsIgnoreCase("OCR")) {
                    mTextView.append("\n(hold the text to copy it!)");
                } else {
                    mTextView.append("(hold the text to copy it!)");
                }
                mTextView.setOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View view) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(title, builder);
                        clipboard.setPrimaryClip(clip);
                        Snackbar.make(findViewById(R.id.mtv_firebaseimageml_results), "Copied!", 500).show();
                        return true;
                    }
                });
            } else {
                mTextView.append(title.substring(0, title.indexOf(' ')) + " detector didn't find anything!");
            }
        } else if (success == false) {
            mTextView.setText(null);
            mTextView.setMovementMethod(new ScrollingMovementMethod());
            mTextView.append(builder);
        }
    }

    //Label Detection
    public void LabelDetection() {
        mTextView.setText("Label detector loading...");
        final StringBuilder builder = new StringBuilder();
        final ArrayList<String> labeltext = new ArrayList<>();
        BitmapDrawable drawable = (BitmapDrawable) pv_PhotoPreview.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler();
        labeler.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                        // Task completed successfully
                        if (labels.size() != 0) {
                            if (labels.size() == 1) {
                                builder.append(labels.size() + " Label detected\n\n");
                            } else if (labels.size() > 1) {
                                builder.append(labels.size() + " Labels detected\n\n");
                            }
                        }
                        int label_count = 1;
                        for (FirebaseVisionImageLabel label : labels) {
                            String text = label.getText();
                            float confidence = label.getConfidence();
                            //Only add labels with confidence greater than 0.5
                            if (confidence > 0) {
                                builder.append(label_count++ + ". " + text + " [" + String.format("%.2f", confidence) + " conf]\n");
                            }
                        }
                        builder.append("\n");
                        ShowDetection("Label Detection", builder, true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("Apologies :(\nLabel detector encountered a problem!\n");
                        ShowDetection("Label Detection", builder, false);
                    }
                });
    }

    public void OCRDetection() {
        mTextView.setText("OCR detector loading...");
        final StringBuilder builder = new StringBuilder();
        BitmapDrawable drawable = (BitmapDrawable) pv_PhotoPreview.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                            // Task completed successfully
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
                        ShowDetection("OCR Detection", builder, true);
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                StringBuilder builder = new StringBuilder();
                                builder.append("Apologies :(\nOCR detector encountered a problem!\n\n");
                                ShowDetection("OCR Detection", builder, false);
                            }
                        });
    }

    //Face Detection
    public void FaceDetection() {
        mTextView.setText("Face detector loading...");
        final StringBuilder builder = new StringBuilder();
        BitmapDrawable drawable = (BitmapDrawable) pv_PhotoPreview.getDrawable();
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
        Task<List<FirebaseVisionFace>> result = detector.detectInImage(image);
        result.addOnSuccessListener(
                new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> faces) {
                        // Task completed successfully
                        if (faces.size() != 0) {
                            if (faces.size() == 1) {
                                builder.append(faces.size() + " Face detected\n\n");
                            } else if (faces.size() > 1) {
                                builder.append(faces.size() + " Faces detected\n\n");
                            }
                        }
                        for (FirebaseVisionFace face : faces) {
                            int id = face.getTrackingId();
                            float rotY = face.getHeadEulerAngleY(); // Head is rotated to the right rotY degrees
                            float rotZ = face.getHeadEulerAngleZ(); // Head is tilted sideways rotZ degrees
                            builder.append("1. Face Tracking ID [" + id + "]\n");
                            builder.append("2. Head Rotation to Right [" + String.format("%.2f", rotY) + " °]\n");
                            builder.append("3. Head Tilted Sideways [" + String.format("%.2f", rotZ) + " °]\n");
                            if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                if (face.getSmilingProbability() > 0) {
                                    float SmilingProbability = face.getSmilingProbability();
                                    builder.append("4. Smiling Probability [" + String.format("%.2f", SmilingProbability) + "]\n");
                                }
                            }
                            if (face.getLeftEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                if (face.getLeftEyeOpenProbability() > 0) {
                                    float LeftEyeOpenProbability = face.getLeftEyeOpenProbability();
                                    builder.append("5. Left Eye Open Probability [" + String.format("%.2f", LeftEyeOpenProbability) + "]\n");
                                }
                            }
                            if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                if (face.getRightEyeOpenProbability() > 0) {
                                    float RightEyeOpenProbability = face.getRightEyeOpenProbability();
                                    builder.append("6. Right Eye Open Probability [" + String.format("%.2f", RightEyeOpenProbability) + "]\n");
                                }
                            }
                            builder.append("\n");
                        }
                        ShowDetection("Face Detection", builder, true);
                    }
                });
        result.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        StringBuilder builder = new StringBuilder();
                        builder.append("Apologies :(\nFace detector encountered a problem!\n");
                        ShowDetection("Face Detection", builder, false);
                    }
                });
    }

    //Barcode Detection
    public void BarcodeDetection() {
        mTextView.setText("Barcode detector loading...");
        final StringBuilder builder = new StringBuilder();
        BitmapDrawable drawable = (BitmapDrawable) pv_PhotoPreview.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector();
        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image);
        result.addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
            @Override
            public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                // Task completed successfully
                if (barcodes.size() != 0) {
                    if (barcodes.size() == 1) {
                        builder.append(barcodes.size() + " Barcode Detected - \n\n");
                    } else if (barcodes.size() > 1) {
                        builder.append(barcodes.size() + " Barcodes Detected - \n\n");
                    }
                }
                for (FirebaseVisionBarcode barcode : barcodes) {
                            /* Not used for display
                            Rect bounds = barcode.getBoundingBox();
                            Point[] corners = barcode.getCornerPoints();
                            String rawValue = barcode.getRawValue();
                            */
                    // IMPORTANT - Can't print multiple barcodes at same time, only print the last barcode retireved from list barcodes
                    int valueType = barcode.getValueType();
                    // Switch case for supported value types for barcodes
                    switch (valueType) {
                        case FirebaseVisionBarcode.TYPE_UNKNOWN: //0
                            builder.append("Unknown Barcode :(\n");
                            builder.append("\n");
                            ShowDetection("Barcode Detection", builder, true);
                            break;
                        case FirebaseVisionBarcode.TYPE_CONTACT_INFO: //1
                            String titleContact = barcode.getContactInfo().getTitle();
                            String personName = barcode.getContactInfo().getName().getFormattedName();
                            List<FirebaseVisionBarcode.Phone> phoneContacts = barcode.getContactInfo().getPhones();
                            List<FirebaseVisionBarcode.Email> emailContacts = barcode.getContactInfo().getEmails();
                            List<FirebaseVisionBarcode.Address> addresses = barcode.getContactInfo().getAddresses();
                            String organizations = barcode.getContactInfo().getOrganization();
                            String[] urls = barcode.getContactInfo().getUrls();
                            int contact = 1;
                            builder.append("Contact Barcode -\n");
                            if (!titleContact.isEmpty()) {
                                builder.append(contact++ + ". Title - " + titleContact + "\n");
                            }
                            if (!personName.isEmpty()) {
                                builder.append(contact++ + ". Person Name - " + personName + "\n");
                            }
                            if (!phoneContacts.isEmpty()) {
                                if (phoneContacts.size() == 1) {
                                    builder.append(contact++ + ". Phone - \n");
                                } else if (phoneContacts.size() > 1) {
                                    builder.append(contact++ + ". Phones - \n");
                                }
                                for (int phoneno = 0; phoneno < phoneContacts.size(); phoneno++) {
                                    builder.append("Phone Sr. No. " + (phoneno + 1) + "\n");
                                    if (phoneContacts.get(phoneno).getType() == 0) {
                                        builder.append("Phone Type - Unknown\n");
                                    } else if (phoneContacts.get(phoneno).getType() == 1) {
                                        builder.append("Phone Type - Work\n");
                                    } else if (phoneContacts.get(phoneno).getType() == 2) {
                                        builder.append("Phone Type - Home\n");
                                    } else if (phoneContacts.get(phoneno).getType() == 3) {
                                        builder.append("Phone Type - Fax\n");
                                    } else if (phoneContacts.get(phoneno).getType() == 4) {
                                        builder.append("Phone Type - Mobile\n");
                                    } else {
                                        builder.append("Phone Type - Unknown\n");
                                    }
                                    if (phoneContacts.get(phoneno).getNumber() != null) {
                                        builder.append("Phone Number - " + phoneContacts.get(phoneno).getNumber() + "\n");
                                    }
                                }
                            }
                            if (!emailContacts.isEmpty()) {
                                if (emailContacts.size() == 1) {
                                    builder.append(contact++ + ". Email - \n");
                                } else if (emailContacts.size() > 1) {
                                    builder.append(contact++ + ". Emails - \n");
                                }
                                for (int emailno = 0; emailno < emailContacts.size(); emailno++) {
                                    builder.append("Email Sr. No. " + (emailno + 1) + "\n");
                                    if (emailContacts.get(emailno).getType() == 0) {
                                        builder.append("Email Type - Unknown\n");
                                    } else if (emailContacts.get(emailno).getType() == 1) {
                                        builder.append("Email Type - Work\n");
                                    } else if (emailContacts.get(emailno).getType() == 2) {
                                        builder.append("Email Type - Home\n");
                                    } else {
                                        builder.append("Email Type - Unknown\n");
                                    }
                                    if (!(emailContacts.get(emailno).getAddress().isEmpty())) {
                                        builder.append("Email Address - " + emailContacts.get(emailno).getAddress() + "\n");
                                    }
                                    if (!(emailContacts.get(emailno).getSubject().isEmpty())) {
                                        builder.append("Email Subject - " + emailContacts.get(emailno).getSubject() + "\n");
                                    }
                                    if (!(emailContacts.get(emailno).getBody().isEmpty())) {
                                        builder.append("Email Body - " + emailContacts.get(emailno).getBody() + "\n");
                                    }
                                }
                            }
                            if (!addresses.isEmpty()) {
                                if (addresses.size() == 1) {
                                    builder.append(contact++ + ". Address - \n");
                                } else if (phoneContacts.size() > 1) {
                                    builder.append(contact++ + ". Addresses - \n");
                                }
                                for (int addressno = 0; addressno < addresses.size(); addressno++) {
                                    builder.append("Address Sr. No. " + (addressno + 1) + "\n");
                                    if (emailContacts.get(addressno).getType() == 0) {
                                        builder.append("Address Type - Unknown\n");
                                    } else if (emailContacts.get(addressno).getType() == 1) {
                                        builder.append("Address Type - Work\n");
                                    } else if (emailContacts.get(addressno).getType() == 2) {
                                        builder.append("Address Type - Home\n");
                                    } else {
                                        builder.append("Address Type - Unknown\n");
                                    }
                                    if (!(addresses.get(addressno).getAddressLines().length < 1)) {
                                        for (int addresslines = 0; addresslines < addresses.get(addressno).getAddressLines().length; addresslines++) {
                                            builder.append("Address Line " + (addresslines + 1) + " - ");
                                            builder.append(addresses.get(addressno).getAddressLines()[addresslines] + "\n");
                                        }
                                    }
                                }
                            }
                            if (!organizations.isEmpty()) {
                                builder.append(contact++ + ". Organization - " + organizations + "\n");
                            }
                            if (!(urls.length < 1)) {
                                if (urls.length == 1) {
                                    builder.append(contact++ + ". URL - \n");
                                } else if (urls.length > 1) {
                                    builder.append(contact++ + ". URLs - \n");
                                }
                                for (int urllength = 0; urllength < urls.length; urllength++) {
                                    builder.append("URL " + (urllength + 1) + " - ");
                                    builder.append(urls[urllength] + "\n");
                                }
                            }
                            builder.append("\n");
                            ShowDetection("Barcode Detection", builder, true);
                            break;
                        case FirebaseVisionBarcode.TYPE_EMAIL: //2
                            int emailtype = barcode.getEmail().getType();
                            String emailaddress = barcode.getEmail().getAddress();
                            String emailsubject = barcode.getEmail().getSubject();
                            String emailbody = barcode.getEmail().getBody();
                            int email_item = 1;
                            builder.append("Email Barcode -\n");
                            if (!(Integer.toString(emailtype).isEmpty())) {
                                if (emailtype == 0) {
                                    builder.append(email_item++ + ". Type -  Unknown\n");
                                } else if (emailtype == 1) {
                                    builder.append(email_item++ + ". Type - Work\n");
                                } else if (emailtype == 2) {
                                    builder.append(email_item++ + ". Type - Home\n");
                                } else {
                                    builder.append(email_item++ + ". Type - Unknown\n");
                                }
                            }
                            if (!(emailaddress.isEmpty())) {
                                builder.append(email_item++ + ". Address - " + emailaddress + "\n");
                            }
                            if (!(emailsubject.isEmpty())) {
                                builder.append(email_item++ + ". Subject - " + emailsubject + "\n");
                            }
                            if (!(emailbody.isEmpty())) {
                                builder.append(email_item++ + ". Body - " + emailbody + "\n");
                            }
                            builder.append("\n");
                            ShowDetection("Barcode Detection", builder, true);
                            break;
                        case FirebaseVisionBarcode.TYPE_ISBN: //3
                            builder.append("ISBN Barcode -\nno data available\n");
                            builder.append("\n");
                            ShowDetection("Barcode Detection", builder, true);
                            break;
                        case FirebaseVisionBarcode.TYPE_PHONE: //4
                            int phonetype = barcode.getPhone().getType();
                            String phonenumber = barcode.getPhone().getNumber();
                            int phonenumber_item = 1;
                            builder.append("Phone Barcode -\n");
                            if (!(Integer.toString(phonetype).isEmpty())) {
                                if (phonetype == 0) {
                                    builder.append(phonenumber_item++ + ". Type - Unknown\n");
                                } else if (phonetype == 1) {
                                    builder.append(phonenumber_item++ + ". Type - Work\n");
                                } else if (phonetype == 2) {
                                    builder.append(phonenumber_item++ + ". Type - Home\n");
                                } else if (phonetype == 3) {
                                    builder.append(phonenumber_item++ + ". Type - Fax\n");
                                } else if (phonetype == 4) {
                                    builder.append(phonenumber_item++ + ". Type - Mobile\n");
                                } else {
                                    builder.append(phonenumber_item++ + ". Type - Unknown\n");
                                }
                            }
                            if (!(phonenumber.isEmpty())) {
                                builder.append(phonenumber_item++ + ". Number - " + phonenumber + "\n");
                            }
                            builder.append("\n");
                            ShowDetection("Barcode Detection", builder, true);
                            break;
                        case FirebaseVisionBarcode.TYPE_PRODUCT: //5
                            builder.append("Product Barcode -\nno data available\n");
                            builder.append("\n");
                            ShowDetection("Barcode Detection", builder, true);
                            break;
                        case FirebaseVisionBarcode.TYPE_SMS: //6
                            String smsnumber = barcode.getSms().getPhoneNumber();
                            String smsmessage = barcode.getSms().getMessage();
                            int sms_item = 1;
                            builder.append("SMS Barcode -\n");
                            if (!(smsnumber.isEmpty())) {
                                builder.append(sms_item++ + ". Number - " + smsnumber + "\n");
                            }
                            if (!(smsmessage.isEmpty())) {
                                builder.append(sms_item++ + ". Message- " + smsmessage + "\n");
                            }
                            builder.append("\n");
                            ShowDetection("Barcode Detection", builder, true);
                            break;
                        case FirebaseVisionBarcode.TYPE_TEXT: //7
                            builder.append("Text Barcode -\nno data available\n");
                            builder.append("\n");
                            ShowDetection("Barcode Detection", builder, true);
                            break;
                        case FirebaseVisionBarcode.TYPE_URL: //8
                            String title = barcode.getUrl().getTitle();
                            String url = barcode.getUrl().getUrl();
                            int url_item = 1;
                            builder.append("URL Barcode -\n");
                            if (title.length() != 0) {
                                builder.append(url_item++ + ". Title - " + title + "\n");
                            }
                            if (!url.isEmpty()) {
                                builder.append(url_item++ + ". URL - " + url + "\n");
                            }
                            builder.append("\n");
                            ShowDetection("Barcode Detection", builder, true);
                            break;
                        case FirebaseVisionBarcode.TYPE_WIFI: //9
                            String wifissid = barcode.getWifi().getSsid();
                            String wifipassword = barcode.getWifi().getPassword();
                            int wifitype = barcode.getWifi().getEncryptionType();
                            int wifi_item = 1;
                            builder.append("WiFi Barcode -\n");
                            if (!wifissid.isEmpty()) {
                                builder.append(wifi_item++ + ". SSID - " + wifissid + "\n");
                            }
                            if (!wifipassword.isEmpty()) {
                                builder.append(wifi_item++ + ". Password - " + wifipassword + "\n");
                            }
                            if (!(Integer.toString(wifitype).isEmpty())) {
                                if (wifitype == 1) {
                                    builder.append(wifi_item++ + ". Encryption Type - Open\n");
                                } else if (wifitype == 2) {
                                    builder.append(wifi_item++ + ". Encryption Type - WPA\n");
                                } else if (wifitype == 3) {
                                    builder.append(wifi_item++ + ". Encryption Type - WEP\n");
                                } else {
                                    builder.append(wifi_item++ + ". Encryption Type - Unknown\n");
                                }
                            }
                            builder.append("\n");
                            ShowDetection("Barcode Detection", builder, true);
                            break;
                        case FirebaseVisionBarcode.TYPE_GEO: //10
                            double geolat = barcode.getGeoPoint().getLat();
                            double geolng = barcode.getGeoPoint().getLng();
                            int geo_item = 1;
                            builder.append("Geo Barcode -\n");
                            if (!(Double.toString(geolat).isEmpty())) {
                                builder.append(geo_item++ + ". Latitude - " + geolat + "\n");
                            }
                            if (!(Double.toString(geolng).isEmpty())) {
                                builder.append(geo_item++ + ". Longitude - " + geolng + "\n");
                            }
                            builder.append("\n");
                            ShowDetection("Barcode Detection", builder, true);
                            break;
                        case FirebaseVisionBarcode.TYPE_CALENDAR_EVENT: //11
                            String summary = barcode.getCalendarEvent().getSummary();
                            FirebaseVisionBarcode.CalendarDateTime start = barcode.getCalendarEvent().getStart();
                            FirebaseVisionBarcode.CalendarDateTime end = barcode.getCalendarEvent().getEnd();
                            String organizer = barcode.getCalendarEvent().getOrganizer();
                            String location = barcode.getCalendarEvent().getLocation();
                            String description = barcode.getCalendarEvent().getDescription();
                            String status = barcode.getCalendarEvent().getStatus();
                            int calendar_item = 1;
                            builder.append("Calendar Event Barcode -\n");
                            if (!summary.isEmpty()) {
                                builder.append(calendar_item++ + ". Summary - " + summary + "\n");
                            }
                            if (!start.equals(null)) {
                                builder.append(calendar_item++ + ". Start Date & Time - ");
                                builder.append(start.getDay() + "/" + start.getMonth() + "/" + start.getYear() + " ");
                                builder.append(start.getHours() + ":" + start.getMinutes() + ":" + start.getSeconds() + "\n");
                            }
                            if (!end.equals(null)) {
                                builder.append(calendar_item++ + ". End Date & Time - ");
                                builder.append(end.getDay() + "/" + end.getMonth() + "/" + end.getYear() + " ");
                                builder.append(end.getHours() + ":" + end.getMinutes() + ":" + end.getSeconds() + "\n");
                            }
                            if (!organizer.isEmpty()) {
                                builder.append(calendar_item++ + ". Organizer - " + organizer + "\n");
                            }
                            if (!location.isEmpty()) {
                                builder.append(calendar_item++ + ". Location - " + location + "\n");
                            }
                            if (!description.isEmpty()) {
                                builder.append(calendar_item++ + ". Description - " + description + "\n");
                            }
                            if (!status.isEmpty()) {
                                builder.append(calendar_item++ + ". Status - " + status + "\n");
                            }
                            builder.append("\n");
                            ShowDetection("Barcode Detection", builder, true);
                            break;
                        case FirebaseVisionBarcode.TYPE_DRIVER_LICENSE: //12
                            String dlfirstname = barcode.getDriverLicense().getFirstName();
                            String dlmiddlename = barcode.getDriverLicense().getMiddleName();
                            String dllastname = barcode.getDriverLicense().getLastName();
                            String dlgender = barcode.getDriverLicense().getGender();
                            String dlbirthdate = barcode.getDriverLicense().getBirthDate();
                            String dllicensenumber = barcode.getDriverLicense().getLicenseNumber();
                            String dlissuedate = barcode.getDriverLicense().getIssueDate();
                            String dlexpirydate = barcode.getDriverLicense().getExpiryDate();
                            String dlissuingcountry = barcode.getDriverLicense().getIssuingCountry();
                            String dladdressstreet = barcode.getDriverLicense().getAddressStreet();
                            String dladdresscity = barcode.getDriverLicense().getAddressCity();
                            String dladdressstate = barcode.getDriverLicense().getAddressState();
                            String dladdresszip = barcode.getDriverLicense().getAddressZip();
                            String dldocumenttype = barcode.getDriverLicense().getDocumentType();
                            int dl_item = 1;
                            builder.append("Driver License Barcode -\n");
                            if (!(dlfirstname.isEmpty())) {
                                builder.append(dl_item++ + ". First Name - " + dlfirstname + "\n");
                            }
                            if (!(dlmiddlename.isEmpty())) {
                                builder.append(dl_item++ + ". Middle Name - " + dlmiddlename + "\n");
                            }
                            if (!(dllastname.isEmpty())) {
                                builder.append(dl_item++ + ". Last Name - " + dllastname + "\n");
                            }
                            if (!(dlgender.isEmpty())) {
                                builder.append(dl_item++ + ". Gender - " + dlgender + "\n");
                            }
                            if (!(dlbirthdate.isEmpty())) {
                                builder.append(dl_item++ + ". Date of Birth - " + dlbirthdate + "\n");
                            }
                            if (!(dllicensenumber.isEmpty())) {
                                builder.append(dl_item++ + ". License Number - " + dllicensenumber + "\n");
                            }
                            if (!(dlissuedate.isEmpty())) {
                                builder.append(dl_item++ + ". Issue Date - " + dlissuedate + "\n");
                            }
                            if (!(dlexpirydate.isEmpty())) {
                                builder.append(dl_item++ + ". Expiry Date - " + dlexpirydate + "\n");
                            }
                            if (!(dlissuingcountry.isEmpty())) {
                                builder.append(dl_item++ + ". Issuing Country - " + dlissuingcountry + "\n");
                            }
                            if (!(dladdressstreet.isEmpty())) {
                                builder.append(dl_item++ + ". Street - " + dladdressstreet + "\n");
                            }
                            if (!(dladdresscity.isEmpty())) {
                                builder.append(dl_item++ + ". City - " + dladdresscity + "\n");
                            }
                            if (!(dladdressstate.isEmpty())) {
                                builder.append(dl_item++ + ". State - " + dladdressstate + "\n");
                            }
                            if (!(dladdresszip.isEmpty())) {
                                builder.append(dl_item++ + ". ZIP Code- " + dladdresszip + "\n");
                            }
                            if (!(dldocumenttype.isEmpty())) {
                                builder.append(dl_item++ + ". Document Type - " + dldocumenttype + "\n");
                            }
                            builder.append("\n");
                            ShowDetection("Barcode Detection", builder, true);
                        default:
                            break;
                    }
                }
                ShowDetection("Barcode Detection", builder, true);
            }
        });
        result.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Task failed with an exception
                StringBuilder builder = new StringBuilder();
                builder.append("Apologies :(\nBarcode Detector encountered a problem!\n\n");
                ShowDetection("Barcode Detection", builder, false);
            }
        });
    }
}