# AURA Android — Releases & Distribution

### Release Channels
- **Debug Builds:** Generated on every commit to `main` via `.github/workflows/android-ci.yml`.
- **Production Releases:** Tagged with semantic versions (e.g., `v1.0.0`) via `.github/workflows/android-release.yml`.

### Artifact Checklist
- `AURA-Android-debug.apk`: Standard debug APK suitable for sideloading and testing on physical Android devices or emulators.
- `AURA-Android-release.apk`: Production-ready optimized APK signed with project release configuration.
- `AURA-Android-release.aab`: Android App Bundle for Google Play Store publishing.

### Versioning Policy
AURA strictly follows [Semantic Versioning 2.0.0](https://semver.org/):
`MAJOR.MINOR.PATCH`
