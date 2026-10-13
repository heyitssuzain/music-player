# AURA Android — Troubleshooting Guide

### 1. Audio Pauses When Backgrounded
- **Cause:** Battery optimization or missing `FOREGROUND_SERVICE_MEDIA_PLAYBACK` permission.
- **Fix:** AURA automatically binds `AuraMediaService` with `foregroundServiceType="mediaPlayback"`. If device vendor (e.g. Xiaomi MIUI, Huawei) kills background apps aggressively, set AURA's battery usage to "Unrestricted" in device Settings.

### 2. No Local Songs Visible
- **Cause:** Audio files stored outside standard MediaStore directories or storage permission was denied.
- **Fix:** Navigate to Settings -> Library Rescan, or check app permissions under Android Settings -> Apps -> AURA -> Permissions -> Music & Audio.

### 3. Voice Commands Not Responding
- **Cause:** Microphone permission denied or Google Speech Services not installed on de-Googled ROM.
- **Fix:** Grant microphone permission on prompt. On devices without Google services, manual touch controls operate fully without limitation.

### 4. Online Search Fails or Times Out
- **Cause:** Network offline or provider instance rate-limited.
- **Fix:** Go to Settings -> Provider Diagnostics. Check connection status and verify internet connectivity.
