# AURA Android — Dependency Manifest

All dependencies are centrally locked and managed via Gradle Version Catalog (`gradle/libs.versions.toml`).

## 1. Core Platform & UI
- **Android Gradle Plugin (AGP):** 9.1.1
- **Kotlin:** 2.2.10
- **Jetpack Compose BOM:** 2024.09.00
- **Material 3:** Modern M3 components, dynamic color, typography, tonal elevations
- **Navigation Compose:** 2.8.9
- **Lifecycle & ViewModel Compose:** 2.8.7

## 2. Media Engine
- **AndroidX Media3 ExoPlayer:** 1.5.1 (`androidx.media3:media3-exoplayer`)
- **AndroidX Media3 Session:** 1.5.1 (`androidx.media3:media3-session`)
- **AndroidX Media3 Common & UI:** 1.5.1

## 3. Data & Storage
- **Room SQLite Runtime:** 2.7.0 (`androidx.room:room-runtime`)
- **Room Kotlin Extensions:** 2.7.0 (`androidx.room:room-ktx`)
- **Room Compiler (KSP):** 2.7.0 (`androidx.room:room-compiler` via KSP 2.3.5)

## 4. Networking & Parsing
- **OkHttp:** 4.10.0
- **Logging Interceptor:** 4.10.0
- **Retrofit & Moshi:** 2.12.0 / 1.15.2
- **Coil Compose:** 2.7.0 (`io.coil-kt:coil-compose`)

## 5. Voice & Utilities
- **Android Platform SpeechRecognizer:** Built-in Android Speech API
- **Coroutines:** 1.10.2 (`kotlinx.coroutines.android` / `core`)
