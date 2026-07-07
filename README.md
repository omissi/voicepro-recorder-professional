# VoicePro Recorder Professional

Android voice recorder built with Kotlin + Jetpack Compose + Material 3.

## Included screens
- Splash screen
- Home with recording mode carousel
- Live transcribe / Pro screen
- Recording screen with animated waveform, mark, pause/resume, save, discard
- Saved successfully screen
- Recordings list
- Player screen
- Trim / Cut editor
- Voice changer with effects and pitch/speed preview
- Settings matching the URecorder-style reference layout while using original VoicePro branding
- Language selector: System, English, Arabic, Turkish, Portuguese, Spanish, French, German
- Backup & Restore, Trash, Help, Privacy, Terms
- AdMob banner and interstitial test ads

## Build
Use GitHub Actions workflow: `.github/workflows/android-build.yml`.

The workflow builds:
- `voicepro-recorder-debug.apk`
- `voicepro-recorder-release-unsigned.apk`

For Play Store publishing, sign a release APK/AAB with your private keystore and replace AdMob test IDs with production IDs.
