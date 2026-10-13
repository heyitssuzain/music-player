# AURA Android — Changelog

## [1.0.0] - 2026-10-13

### Removed
- **Authentication System Removed**: Completely removed Firebase Auth, Google Sign-In, Credential Manager, and `google-services` plugin dependencies.
- Purged all authentication walls, sign-in screens, account requirements, and login-only redirects.
- Removed unused dependencies and cleaned up project dependency catalog.

### Added
- **Cinematic Anime Landing Page**:
  - Original anime-inspired protagonist visual centerpiece with glowing electric violet and cyan rim lighting.
  - Multi-plane 2D parallax and ambient Canvas orbital particle generator.
  - Hero copywriting: "YOUR MUSIC. YOUR UNIVERSE.", "Discover your next obsession."
  - Primary CTA ("ENTER AURA") and Secondary CTA ("EXPLORE FEATURES").
  - On-device local persistence for onboarding state in Room SQLite (`has_completed_landing`).
  - Added "Revisit Landing Page" in Settings screen for returning users.
- **Provider Routing & YouTube Search Repair**:
  - Implemented 400ms debouncing on search queries with automatic job cancellation.
  - Added URL encoding support for multiword and multilingual (English & Hindi) search queries.
  - Fixed provider-routing defect in `ProviderRegistry`: stream resolution now directly dispatches to the track's owning provider (`track.sourceProvider`).
  - Enhanced `PipedProvider` with multi-endpoint fallback and audio bitrate selection.
  - Implemented `YtDlpProvider` implementing `MusicProvider` contract with typed diagnostic reporting.
- **GitHub Actions Workflows**:
  - Standardized `.github/workflows/android-ci.yml` and `.github/workflows/android-release.yml` with `gradle/actions/setup-gradle@v4`.
  - Artifact naming updated to exact PRD specification: `AURA-debug-apk`, `AURA-release-apk`, `AURA-release-aab`.
- **Unit Testing**:
  - Added `ProviderRoutingTest` verifying provider registry state, stream resolution routing, search query URL encoding, and yt-dlp diagnostics.
- **Documentation**:
  - Added `docs/ANIMATION_DESIGN.md` (2D parallax vs 3D evaluation and motion choreography).
  - Added `docs/ASSET_LICENSES.md` (licensing and redistribution permissions).
  - Updated `docs/YTDLP_ANDROID_FEASIBILITY.md`, `progress.md`, and `README.md`.

