# Client Route Field

Version 2.1 adds offline import of the supplied Daily Schedule PDF for a shared field-service tablet. See [PDF-IMPORT-GUIDE.md](PDF-IMPORT-GUIDE.md) for installation, the daily workflow, safeguards and testing. [Version 2.0 notes](FIELD-RELEASE-NOTES.md) describe the underlying job-management features.

## Download

Use **Client-Route-Field-2.1.apk** in this repository for the delivered build. The older root `app-debug.apk` belongs to the previous Client Route version.

The new APK updates the directly supplied **Client Route Field 2.0** APK in place, but remains separate from the original **Client Route** app. Keep existing data; do not uninstall or clear storage.

GitHub Actions also builds a `Client-Route-Field-debug-APK` artifact. Those debug builds can use different signing certificates from the delivered APK; do not uninstall a populated app to work around an update rejection.

## What works in this build

PDF schedule import with crew/date selection, joined job notes and duplicate prevention; date-selectable route, start/navigation timer, optional arrival marker, finish-job history, persistent client flags, per-job notes and crew roles, local issue photos, rolling reports, and ZIP backup export. Clients can also be added manually.

## Not complete

**Automatic email retrieval and Excel schedule parsing are not implemented or connected.** Rebecca's receiving mailbox is identified, but its owner must still authorize access. The supplied PDF now works through manual open/share or the file picker. Original-app data migration, permanent production signing and in-app backup restore remain outstanding.

## Verification

- APK compiled successfully with JDK 17 and Gradle 7.2.
- Android lint completed with zero errors; non-blocking warnings remain.
- Installed and launched on an Android API 23 emulator at 600×960.
- Three instrumentation tests passed: job/profile persistence, optional client fields/main-screen creation, and actual PDF extraction/notes/duplicate-safe import.
- Today dashboard and PDF import preview visually inspected.
- Real-device GPS, camera capture, photo viewer and backup-provider behavior still require testing.

## Build

Run `gradle assembleDebug lintDebug` with JDK 17, Gradle 7.2, Android platform 30 and build-tools 30.0.3. Minimum supported API is 23. Device tests use `gradle connectedDebugAndroidTest`.
