# AURA Android — Project Audit & Master PRD Remediation

**Date:** 2026-10-13  
**Auditor:** AURA Lead Systems Architect  
**Repository State:** Native Android (Kotlin + Jetpack Compose + Media3 + Room)

---

## 1. Baseline Repository Inspection

- **Framework:** Native Android (Kotlin 2.2.10, Jetpack Compose, Material 3, AndroidX Media3 1.5.1, Room SQLite 2.7.0, KSP 2.3.5).
- **Application ID:** `com.aistudio.aura.mplay`
- **Toolchain:** Android SDK Compile 36, Target 36, Min SDK 24, Java 17 compile options.
- **Audio Engine:** AndroidX Media3 ExoPlayer with foreground `MediaSessionService` (`AuraMediaService`) and `AuraMediaController`.
- **Persistence:** Room SQLite database (`aura_music.db`) with entities for Tracks, Playlists, PlaylistEntries, Favorites, History, Downloads, Settings.
- **Speech Recognition:** Android platform `SpeechRecognizer` with deterministic English and Hindi/Hinglish intent parsing.

---

## 2. Master PRD Remediation & Defects Repaired

1. **Complete Removal of Authentication Walls & Dependencies:**
   - **Observed State:** Commented-out and dormant auth dependencies (Firebase Auth, Credential Manager, Google Sign-In) and `google-services` plugin were present in `build.gradle.kts` and `libs.versions.toml`.
   - **Fix Applied:** Completely removed all authentication libraries and plugin configurations. Zero login screens, account gates, or sign-up walls exist. Fresh installs enter directly into the application.

2. **Cinematic Anime Landing Page:**
   - **Observed State:** App lacked the cinematic landing entrance required by the PRD.
   - **Fix Applied:** Created `LandingScreen.kt` featuring an original anime-inspired protagonist asset (`aura_anime_character.jpg`), multi-plane 2D parallax, ambient Canvas glowing particle generator, required hero copy ("YOUR MUSIC. YOUR UNIVERSE.", "Discover your next obsession."), and primary/secondary CTAs ("ENTER AURA", "EXPLORE FEATURES").
   - **Persistence:** Local onboarding completion is tracked in Room SQLite (`has_completed_landing`). Returning users can revisit the landing page via the Settings screen.

3. **YouTube Search & Provider Routing Repair:**
   - **Observed State:** `ProviderRegistry` defaulted to `DirectStreamProvider` (which returned 6 synthetic demo tracks), shadowing online search queries. Additionally, stream resolution routed through `activeProvider` instead of the result's owning provider. Keystrokes were not debounced.
   - **Fix Applied:**
     - Set `PipedProvider` as the primary online search provider.
     - Implemented 400ms debouncing with job cancellation in `AuraViewModel.searchMusic`.
     - Added URL encoding for multiword, Hindi, and English queries.
     - Added `resolveStream(track)` in `ProviderRegistry` that dispatches directly to the track's owning provider (`track.sourceProvider`).
     - Added `YtDlpProvider` implementing `MusicProvider` contract with typed diagnostics.

4. **GitHub Actions Workflow & APK Delivery:**
   - **Observed State:** Workflow had unaligned artifact names and invoked system Gradle rather than standard setup actions.
   - **Fix Applied:** Updated `.github/workflows/android-ci.yml` and `.github/workflows/android-release.yml` with `gradle/actions/setup-gradle@v4` and exact PRD artifact names: `AURA-debug-apk`, `AURA-release-apk`, and `AURA-release-aab`.

5. **Test Coverage & Verification:**
   - Added `ProviderRoutingTest` verifying provider dispatch, resolution routing, URL encoding, and diagnostics.
