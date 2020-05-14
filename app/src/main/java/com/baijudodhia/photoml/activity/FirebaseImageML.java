package com.baijudodhia.photoml.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.baijudodhia.photoml.R;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

public class FirebaseImageML extends AppCompatActivity {
    public static float deviceWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    public static float deviceHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    String imagePath;
    PhotoView photoView;
    TextView textView;
    Button label, ocr, face, barcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebaseimageml);

        getSupportActionBar().setTitle(null);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        imagePath = intent.getExtras().getString("imagePath");
        textView = findViewById(R.id.result);
        photoView = findViewById(R.id.photoView);
        label = findViewById(R.id.btn_label);
        ocr = findViewById(R.id.btn_ocr);
        face = findViewById(R.id.btn_face);
        barcode = findViewById(R.id.btn_barcode);

        if (deviceHeight > deviceWidth) {
            float screenRatio = deviceWidth / deviceHeight;
            float photoViewHeight = deviceWidth * screenRatio;
            photoView.getLayoutParams().height = (int) photoViewHeight;
            photoView.requestLayout();
        }

        Glide.with(this)
                .load(imagePath)
                .into(photoView);

        label.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                LabelDetection();
            }
        });

        ocr.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                OCRDetection();
            }
        });

        face.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                FaceDetection();
            }
        });

        barcode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                BarcodeDetection();
            }
        });
    }

    //Show result of Firebase ML-Kit detection in TextView
    public void ShowDetection(final String title, final StringBuilder builder) {
        textView.setText(null);
        textView.setMovementMethod(new ScrollingMovementMethod());
        if (builder.length() != 0) {
            textView.append(builder);
        } else {
            textView.append(title.substring(0, title.indexOf(' ')) + " detector didn't find anything!");
        }
        if (builder.length() != 0) {
            textView.append("\n(hold the text to copy it!)");
            textView.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View view) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(title, builder);
                    clipboard.setPrimaryClip(clip);
                    Snackbar.make(findViewById(R.id.result), "Copied!", 500).show();
                    return true;
                }
            });
        }
    }

    //Label Detection
    public void LabelDetection() {
        textView.setText("label loading...");
        final StringBuilder builder = new StringBuilder();
        final ArrayList<String> labeltext = new ArrayList<String>();
        BitmapDrawable drawable = (BitmapDrawable) photoView.getDrawable();
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
        textView.setText("ocr loading...");
        final StringBuilder builder = new StringBuilder();
        BitmapDrawable drawable = (BitmapDrawable) photoView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
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
        textView.setText("face loading...");
        final StringBuilder builder = new StringBuilder();
        BitmapDrawable drawable = (BitmapDrawable) photoView.getDrawable();
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
                            } else {
                                builder.append(faces.size() + " Faces detected\n\n");
                            }
                        } else {
                            builder.append("No faces detected!");
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
                });
        result.addOnFailureListener(
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
        textView.setText("barcode loading...");
        final StringBuilder builder = new StringBuilder();
        BitmapDrawable drawable = (BitmapDrawable) photoView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector();
        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image);
        result.addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
            @Override
            public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                // Task completed successfully
                for (FirebaseVisionBarcode barcode : barcodes) {
                            /* Not used for display
                            Rect bounds = barcode.getBoundingBox();
                            Point[] corners = barcode.getCornerPoints();
                            String rawValue = barcode.getRawValue();
                            */
                    // IMPORTANT - Can't print multiple barcodes at same time, only print the last barcode retireved from list barcodes
                    int valueType = barcode.getValueType();
                    System.out.println("VALUE - " + valueType + "\n");
                    // Switch case for supported value types for barcodes
                    switch (valueType) {
                        case FirebaseVisionBarcode.TYPE_UNKNOWN: //0
                            builder.append("Unknown Barcode :(\n");
                            break;
                        case FirebaseVisionBarcode.TYPE_CONTACT_INFO: //1
                            String titleContact = barcode.getContactInfo().getTitle();
                            String personName = barcode.getContactInfo().getName().toString();
                            String phones = barcode.getContactInfo().getPhones().toString();
                            String emails = barcode.getContactInfo().getEmails().toString();
                            String addresses = barcode.getContactInfo().getAddresses().toString();
                            String organizations = barcode.getContactInfo().getOrganization();
                            String urls = barcode.getContactInfo().getUrls().toString();
                            int contact = 1;
                            builder.append("Contact Barcode -\n");
                            if (!titleContact.isEmpty()) {
                                builder.append(contact++ + ". Title - " + titleContact + "\n");
                            }
                            if (!personName.isEmpty()) {
                                builder.append(contact++ + ". Person Name - " + personName + "\n");
                            }
                            if (!phones.isEmpty()) {
                                builder.append(contact++ + ". Phone - " + phones + "\n");
                            }
                            if (!emails.isEmpty()) {
                                builder.append(contact++ + ". Emails - " + emails + "\n");
                            }
                            if (!addresses.isEmpty()) {
                                builder.append(contact++ + ". Address - " + addresses + "\n");
                            }
                            if (!organizations.isEmpty()) {
                                builder.append(contact++ + ". Organization - " + organizations + "\n");
                            }
                            if (!urls.isEmpty()) {
                                builder.append(contact++ + ". URLS - " + urls + "\n");
                            }
                            ShowDetection("Barcode Detection", builder);
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
                            ShowDetection("Barcode Detection", builder);
                            break;
                        case FirebaseVisionBarcode.TYPE_ISBN: //3
                            builder.append("ISBN Barcode -\nno data available\n");
                            ShowDetection("Barcode Detection", builder);
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
                            ShowDetection("Barcode Detection", builder);
                            break;
                        case FirebaseVisionBarcode.TYPE_PRODUCT: //5
                            builder.append("Product Barcode -\nno data available\n");
                            ShowDetection("Barcode Detection", builder);
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
                            ShowDetection("Barcode Detection", builder);
                            break;
                        case FirebaseVisionBarcode.TYPE_TEXT: //7
                            builder.append("Text Barcode -\nno data available\n");
                            ShowDetection("Barcode Detection", builder);
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
                            ShowDetection("Barcode Detection", builder);
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
                            ShowDetection("Barcode Detection", builder);
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
                            ShowDetection("Barcode Detection", builder);
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
                            ShowDetection("Barcode Detection", builder);
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
                            ShowDetection("Barcode Detection", builder);
                        default:
                            break;
                    }
                }
                if (builder.length() == 0) {
                    ShowDetection("Barcode Detection", builder);
                }
            }
        });
        result.addOnFailureListener(new OnFailureListener() {
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