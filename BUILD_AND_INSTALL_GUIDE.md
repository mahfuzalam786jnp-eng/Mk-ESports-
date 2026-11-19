# MK Esports — Build, Signing & Installation Guide

## 1. Architecture & Firebase Configuration
- **Package / Application ID**: `com.aistudio.mkesports.vqrtxz`
- **Database**: Cloud Firestore Enterprise Edition (`R.string.firestore_database_id` defined in `app/src/main/res/values/firebase_applet_config.xml`)
- **Authentication**: Firebase Authentication via Google Sign-In (`CredentialManager` + `GetSignInWithGoogleOption`) combined with in-app Mobile Number & Development Test OTP (`123456`) verification.
  - *Production SMS OTP note*: To switch from Development Test OTP to live SMS delivery, enable **Phone** under *Firebase Console → Authentication → Sign-in method*.
- **Owner-Controlled Admin**: Enforced in `firestore.rules` using `request.auth.token.email == 'mahfuzalam786jnp@gmail.com' && request.auth.token.email_verified == true`. No admin passwords are stored in application code.
- **Simulated Demo Currency**: All MK Demo Coins are virtual simulation credits. Real-money deposits and withdrawals are disabled.

---

## 2. Debug APK Build Instructions

From the project root directory:

```bash
gradle :app:assembleDebug
```

- Output APK path:
  `app/build/outputs/apk/debug/app-debug.apk`
- Run local JVM & Robolectric UI tests:
  ```bash
  gradle :app:testDebugUnitTest
  ```
- Run Firestore Security Rules tests against the local emulator:
  ```bash
  FIRESTORE_EMULATOR_HOST="127.0.0.1:8085" node --test firestore.test.js
  ```

---

## 3. Signed Release APK Instructions

1. **Generate an Upload Keystore** (one-time setup if you do not have `my-upload-key.jks`):
   ```bash
   keytool -genkey -v -keystore my-upload-key.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
   ```
2. **Register your Release SHA-1 & SHA-256** in **Firebase Console → Project Settings → Your Android App (`com.aistudio.mkesports.vqrtxz`)** so Google Sign-In works in release builds:
   ```bash
   keytool -list -v -keystore my-upload-key.jks -alias upload
   ```
   Download the updated `google-services.json` into `app/google-services.json` if needed.
3. **Export Signing Environment Variables** (used by `signingConfigs.create("release")` in `app/build.gradle.kts`):
   ```bash
   export KEYSTORE_PATH="$(pwd)/my-upload-key.jks"
   export STORE_PASSWORD="your-store-password"
   export KEY_PASSWORD="your-key-password"
   ```
4. **Build the Signed Release APK (or AAB for Play Store)**:
   ```bash
   gradle :app:assembleRelease
   # Or for Google Play App Bundle:
   gradle :app:bundleRelease
   ```
   - Release APK output: `app/build/outputs/apk/release/app-release.apk`
   - Release AAB output: `app/build/outputs/bundle/release/app-release.aab`

---

## 4. Installation Guide (Android Device / Emulator)

### Via ADB (USB Debugging enabled on Android device)
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
For the signed release APK:
```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

### Direct APK Sideload on Device
1. Transfer `app-debug.apk` or `app-release.apk` to your Android phone (Android 7.0 / API 24 or higher).
2. Tap the APK file in your device's **Files** app.
3. Allow **Install unknown apps** if prompted, then tap **Install**.
4. Launch **MK Esports**, sign in with Google, complete Mobile Number + Test OTP (`123456`) verification + Free Fire MAX UID setup, and claim your 1,000 welcome MK Demo Coins.
