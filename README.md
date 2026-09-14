# Client Route

A local-only Android tablet app for managing client addresses and visit times, then opening driving directions in Google Maps.

## Included features

- First and last name, phone number, physical address, start time, and end time.
- Native client list with a simple tablet-friendly UI.
- Tap a client to open directions, edit the record, or delete the record.
- Local-only data storage using SharedPreferences. Client data is not sent to a server.
- Android 6.0.1 and later support (`minSdkVersion 23`).

## Get the installable APK

1. Open the **Actions** tab in GitHub.
2. Select the latest **Build Android APK** run.
3. Open the completed run and download the **Client-Route-debug-APK** artifact.
4. Extract `app-debug.apk` if GitHub downloaded it as a ZIP file.
5. Transfer the APK to the Android tablet, open it, and allow installation from the source when Android asks.
6. Open **Client Route** and allow location access. Google Maps should be installed for the best directions experience.

## Build locally

Open this project in Android Studio and run **Build > Build APK(s)**. The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## Important note

The debug APK is installable but is not Play Store signed. It is appropriate for direct installation onto your own tablet.