# Client Route Field

Version 2.0 is a local-workflow Android test build for a shared field-service tablet. See [FIELD-RELEASE-NOTES.md](FIELD-RELEASE-NOTES.md) for features, installation, limitations and testing steps.

## Download

Use **Client-Route-Field-2.0.apk** in this repository for the delivered build. The older root `app-debug.apk` belongs to the previous Client Route version.

The new APK installs separately as **Client Route Field** and does not copy the original app's records. Keep the original app installed. Do not clear its data.

GitHub Actions also builds a `Client-Route-Field-debug-APK` artifact. Those debug builds can use different signing certificates from the delivered APK; do not uninstall a populated app to work around an update rejection.

## What works in this build

Today route, start/navigation timer, optional arrival marker, finish-job history, persistent client flags, per-job notes and crew roles, local issue photos, rolling reports, and ZIP backup export. Clients are added manually to today's route.

## Not complete

**Automatic email import and Excel schedule parsing are not implemented or connected.** The receiving mailbox and original `.xlsx` workbook are needed to complete that integration. Original-app data migration, permanent production signing and in-app backup restore are also outstanding.

## Verification

- APK compiled successfully with JDK 17 and Gradle 7.2.
- Android lint completed with zero errors; non-blocking warnings remain.
- Installed and launched on an Android API 23 emulator at 600×960.
- Two instrumentation tests passed: job/profile persistence and optional client fields/main-screen creation.
- Today dashboard screenshot visually inspected.
- Real-device GPS, camera capture, photo viewer and backup-provider behavior still require testing.

## Build

Run `gradle assembleDebug lintDebug` with JDK 17, Gradle 7.2, Android platform 30 and build-tools 30.0.3. Minimum supported API is 23. Device tests use `gradle connectedDebugAndroidTest`.
