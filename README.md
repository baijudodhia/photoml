<img src="https://baijudodhia.github.io/photoml/app/src/main/assets/photomllogoedited.png" align="left" width="50" style="margin-right:10px;">

<br />

# PhotoML Android App

PhotoML is an Android App to demonstrate **on-device** FirebaseVision ML-Kit Tasks like _Label detection_, _Optical Character Recognition/Text Recognition_, _Face detection_ and _Barcode scanning_ on images selected from in-built gallery viewer without having to upload the image from device for every new image. The app supports both portrait and landscape layouts.

## Screenshots

<p align="center">
  <img width="200" src="https://baijudodhia.github.io/photoml/screenrecords/PortraitLabel.gif" alt="Label Portrait" title="Label Portrait">
  <img width="200" src="https://baijudodhia.github.io/photoml/screenrecords/PortraitOCR.gif" alt="OCR Portrait" title="OCR Portrait">
  <img width="200" src="https://baijudodhia.github.io/photoml/screenrecords/PortraitFace.gif" alt="Face Portrait" title="Face Portrait">
  <img width="200" src="https://baijudodhia.github.io/photoml/screenrecords/PortraitBarcode.gif" alt="Barcode Portrait" title="Barcode Portrait">
</p>

## Debug APK

Download from [here](https://baijudodhia.github.io/photoml/apk/photoml-debug-app.apk).

## Installation

Clone this repository and import into **Android Studio**

```bash
git clone https://github.com/baijudodhia/photoml.git
```

## Android Permissions

The app requires Read-only Storage permissions
Add the following code in the **AndroidManifest.xml** file to access Storage

```java
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Sign in with your google account.
3. Add new project > Enter project name > Skip analytics > Create project.
4. Add Android App > Enter details > Register App.
5. Download **google-services.json** file and follow the setup to place file in your project to get started.

## Dependencies

1. Add the following dependencies in the **build.gradle (Module:Project)** file

- Firebase dependency

```java
classpath 'com.google.gms:google-services:4.3.3'
```

2. Add the following dependencies in the **build.gradle (Module:App)** file

- Apply Firebase plugin

```java
apply plugin: 'com.google.gms.google-services'
```

- Android dependencies

```java
//AndroidX dependencies
implementation 'androidx.appcompat:appcompat:1.1.0'
implementation 'androidx.constraintlayout:constraintlayout:1.1.3'
implementation 'androidx.legacy:legacy-support-v4:1.0.0'
testImplementation 'junit:junit:4.12'
androidTestImplementation 'androidx.test.ext:junit:1.1.1'
androidTestImplementation 'androidx.test.espresso:espresso-core:3.2.0'

//Material Design Dependency
implementation "com.google.android.material:material:1.1.0"
```

- 3rd Party dependencies

```java
//RecyclerView Dependencies
implementation 'androidx.appcompat:appcompat:1.0.0'
implementation 'androidx.recyclerview:recyclerview:1.1.0'

//Glide Dependencies
implementation 'com.github.bumptech.glide:glide:4.11.0'
annotationProcessor 'com.github.bumptech.glide:compiler:4.11.0'

//PhotoView Dependency for auto image zooming and double tap and pinch zoom replacing ImageView
implementation 'com.github.chrisbanes:PhotoView:2.3.0'
```

- Firebase dependencies

```java
//Firebase ML-Kit general dependency, also used for OCR and Barcode
implementation 'com.google.firebase:firebase-ml-vision:24.0.3'

//Firebase ML-Kit recongnize and label images dependency works along with general dependency
implementation 'com.google.firebase:firebase-ml-vision-image-label-model:20.0.1'

//Firebase ML-Kit face detection dependency works along with general dependency
implementation 'com.google.firebase:firebase-ml-vision-face-model:20.0.1'
```

## Project Assets

All assets belong to their respective owners. A list of reference is provided to all the assets.

1. Firebase Logo from [Firebase](https://firebase.google.com/brand-guidelines).
2. App Logo (Colors Edited) from [Flaticon](https://www.flaticon.com/free-icon/gallery_758462?term=gallery&page=1&position=40).
3. Flaticon License available [here](https://baijudodhia.github.io/photoml/app/src/main/assets/FlaticonLicense.pdf).
