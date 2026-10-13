# AURA Android — Architecture Specification

## 1. System Architecture Overview

AURA is engineered according to Clean Architecture and MVVM (Model-View-ViewModel) principles for Android.

```
┌─────────────────────────────────────────────────────────────┐
│                       Jetpack Compose UI                    │
│   (Home, Player, Mini-Player, Library, Playlists, Voice)   │
└──────────────────────────────┬──────────────────────────────┘
                               │ StateFlow / Events
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                          ViewModels                         │
│  (MainViewModel, PlayerViewModel, LibraryViewModel, etc.)   │
└──────────────┬──────────────────────────────┬───────────────┘
               │                              │
               ▼                              ▼
┌──────────────────────────────┐ ┌────────────────────────────┐
│      Audio Engine Layer      │ │      Repository Layer      │
│  (AuraMediaControllerClient  │ │  (MusicRepository,         │
│   <-> AuraMediaService       │ │   DownloadRepository,      │
│   <-> Media3 ExoPlayer)      │ │   VoiceRepository)         │
└──────────────────────────────┘ └──────┬──────────────┬──────┘
                                        │              │
                                        ▼              ▼
                         ┌────────────────────┐ ┌─────────────┐
                         │    Room SQLite     │ │   Network   │
                         │    (AuraDatabase)  │ │  Providers  │
                         └────────────────────┘ └─────────────┘
```

---

## 2. Layer Definitions

### 2.1 UI Layer (Jetpack Compose)
- **Theme:** AURA Dark Theme (`#0B0E14` base surface, `#141923` elevated cards, `#00E5FF` electric cyan, `#7C4DFF` deep violet, `#388AF6` electric blue).
- **Navigation:** Single-activity architecture hosting `AuraNavigationRoot` with smooth cross-screen transitions.
- **Components:**
  - `AuraTopBar` & `AuraBottomBar` with glassmorphic backdrop.
  - `MiniPlayerBar`: Persistent floating player bar across all tabs with play/pause, title, artist, progress bar, and full player sheet expansion.
  - `FullPlayerScreen`: Large album art, interactive scrubbing slider, repeat/shuffle toggles, speed selection, queue drawer, and lyrics/details tab.
  - `VoiceControlModal`: Speech visualization, live transcript feedback, intent indicator.

### 2.2 Media Engine Layer (AndroidX Media3 ExoPlayer)
- **Service:** `AuraMediaService` extending `androidx.media3.session.MediaSessionService`.
- **Playback Capabilities:**
  - Local URI playback (`content://` and `file://`).
  - Remote streaming with progressive MP3/M4A/Opus/HLS playback.
  - Audio focus management (ducking on notifications, pausing on phone calls or headphone unplugging `ACTION_AUDIO_BECOMING_NOISY`).
  - System Media Notification with action buttons (Previous, Play/Pause, Next, Seek) and lock-screen metadata.
  - `MediaSession` integration for Bluetooth headsets, car audio (Android Auto compatible), and wearable controls.

### 2.3 Persistence Layer (Room Database / SQLite)
- **Database:** `AuraDatabase` (SQLite Room)
- **Entities:**
  - `TrackEntity`: Unified record for local, online, and cached audio tracks.
  - `PlaylistEntity`: User-created and smart playlists.
  - `PlaylistEntryEntity`: Ordered many-to-many relationship linking tracks to playlists.
  - `FavoriteEntity`: Quick index of starred tracks.
  - `HistoryEntity`: Chronological listening timestamps.
  - `DownloadEntity`: State of downloaded audio files, file paths, size in bytes, and statuses (`QUEUED`, `DOWNLOADING`, `COMPLETED`, `FAILED`, `CANCELLED`).
  - `SettingEntity`: Key-value storage for theme, audio crossfade, speech recognizer language, and provider endpoint configs.

### 2.4 Online Provider & Extractor Layer
- **Interface:** `MusicProvider` contract returning typed `OnlineTrack` and `ResolvedAudioStream`.
- **Implementations:**
  - `PipedInvidiousProvider`: Connects to public, open instances to resolve streaming audio URLs and rich metadata.
  - `DirectStreamProvider`: Resolves direct audio streams (Creative Commons, Internet Archive, podcast RSS feeds).
  - `DiagnosticResolver`: Validates connectivity, latency, and extractor compatibility.

### 2.5 Voice Control Layer
- **Engine:** Android `SpeechRecognizer` API with offline speech recognition flag when device-supported.
- **Intent Parser:** Deterministic natural language processor supporting:
  - English: "play", "pause", "resume", "stop", "next", "previous", "shuffle on", "repeat", "favorite", "volume up", "search <song>", "play <song>".
  - Hindi / Hinglish: "gana bajao", "rok do", "agla gana", "pichhla gana", "pasand karo", "khojo <song>".
- Zero paid AI dependencies required for core controls.

### 2.6 Download & Offline Library Layer
- **Storage:** App-specific internal audio directory (`context.filesDir/audio_downloads/`) ensuring strict sandboxed privacy and zero need for risky broad storage permissions.
- **Workflow:** HTTP streaming pipeline with chunked download progress emitting `Flow<DownloadProgress>`, verification of file sizes, and Room catalog insertion upon completion.
