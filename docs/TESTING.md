# AURA Android — Testing Strategy & Verification

## 1. Automated Local JVM Tests
Run:
```bash
gradle testDebugUnitTest
```

### Coverage Scope:
- **Audio Models & Queue State Transitions:** Tests queue additions, removals, reordering, shuffling, and repeat cycles (`AuraTrackQueueTest`).
- **Voice Intent Parser:** Deterministic intent recognition tests for play, pause, next, volume, and search in English and Hindi/Hinglish (`VoiceIntentParserTest`).
- **Provider Adapters & URL Resolution:** Stream parsing, Hindi/English URL encoding, diagnostics, and provider routing (`ProviderRoutingTest`).
- **Database DAOs & Migrations:** Room CRUD validation on tracks, playlists, favorites, and history.
- **Cinematic Landing Page:** Offline first launch, zero authentication gates, ENTER AURA transition, and local onboarding state persistence.

## 2. Real-Device Acceptance Test Checklist
1. **Launch & Permissions:** Install APK on device. Verify edge-to-edge layout, dark theme, and permission prompts only when accessing local audio or voice.
2. **Local Music Playback:** Rescan device music, play an audio file, verify album art and track metadata.
3. **Background Audio & Media Notification:** Press home button or lock device; verify playback continues smoothly, notification shows correct controls, and track title updates.
4. **Online Discovery:** Search for a track; select and stream.
5. **Playlists & Favorites:** Add track to Favorites, create a playlist, reorder tracks.
6. **Voice Commands:** Tap microphone button, speak "play", "pause", or "agla gana"; verify immediate execution.
7. **Offline Downloads:** Download an authorized track, toggle airplane mode, verify seamless offline playback.
