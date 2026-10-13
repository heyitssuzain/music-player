# AURA Android — Build & Compilation Guide

## Prerequisites
- Android SDK 36 (Minimum API 24: Android 7.0 Nougat)
- JDK 17 or JDK 21
- Gradle 8.x+ (or `./gradlew`)

## Command Line Builds

### 1. Build Debug APK
```bash
gradle assembleDebug
```
Output path: `app/build/outputs/apk/debug/app-debug.apk`

### 2. Run Local JVM & Robolectric Tests
```bash
gradle testDebugUnitTest
```

### 3. Build Release APK
```bash
gradle assembleRelease
```
Output path: `app/build/outputs/apk/release/app-release.apk`

### 4. Build Android App Bundle (.aab)
```bash
gradle bundleRelease
```
Output path: `app/build/outputs/bundle/release/app-release.aab`

## Cloud Builds via GitHub Actions
No local Android Studio or local toolchain installation is required. Simply push to `main` or trigger `.github/workflows/android-ci.yml` via Workflow Dispatch to download the ready-to-install `AURA-debug-apk` artifact. Release builds produce `AURA-release-apk` and `AURA-release-aab`.
