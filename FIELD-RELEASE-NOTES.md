# Client Route Field 2.0

## Release status

This is an installable **local-workflow test build**, not the completed email-import release. It installs alongside the original Client Route app using package `com.kalab.clientroute.fieldpreview`. It does not overwrite or automatically import the original app's records.

Automatic email import and Excel schedule parsing are **not implemented**. Setup explicitly shows email as not connected. The supplied schedule was a PDF export, not the original workbook, and the receiving mailbox was not identified.

## Included

- Today screen with a manually assembled ordered route.
- Start & Navigate saves a job start timestamp and opens a navigation app. Only one active job is allowed.
- Finish Job confirms completion and stores the end timestamp, date, customer, crew/roles, notes and photos.
- Total elapsed time includes travel, on-site work and equipment loading. It is not worker payroll time.
- Optional Mark Arrived separates travel from on-site elapsed time without interrupting the total timer.
- Timer uses persisted timestamps, so it does not depend on the screen remaining open. Changing the device clock can affect elapsed time; leave automatic date/time enabled.
- Default crew and roles are reusable, entered as one `Name - Role` line per worker. These are text records, not separate worker payroll accounts.
- Required first name, last name, street and city; state selector defaults to TX. Phone, apartment/suite and ZIP are optional. US phone formatting is retained.
- Persistent client notes and access flags, including gate codes, kept separately from per-job notes.
- Job history with notes, workers/roles, totals and photos.
- Camera-app handoff saves full-size issue photos privately against the job. Optional caption follows capture. Photos are not transmitted to a server.
- Today, last 7 days and last 30 days reports, grouped by job start date and excluding unfinished jobs.
- ZIP export of client JSON, job/profile JSON and original photos. There is no in-app restore yet.

## Install without losing the old app

1. Keep the original Client Route installed. Do not clear its storage.
2. Copy the supplied `Client-Route-Field-2.0.apk` to the Android device and open it.
3. Allow installation from the file manager if prompted.
4. Open **Client Route Field**, not the original Client Route.
5. This app starts with separate empty storage. Create a test client first.

This is a debug-signed APK. The original APK in the repository has a different signing certificate, which is why this build uses a separate application ID. GitHub Actions debug builds can also have a different certificate from this locally built APK. Do not uninstall a populated installation to resolve a signing conflict. A stable production signing key and migration process remain release prerequisites.

## Quick field test

While parked:

1. Setup: enter the default crew, one worker and role per line; save.
2. Clients: add a test customer with name, street, city and state. Leave phone and ZIP blank to test optional fields.
3. Open the profile and save a test gate/access flag and client note.
4. Add the client to today's route.
5. Today: choose Start & Navigate. Verify the correct destination before moving.
6. Return to the app. Close and reopen it; confirm the active timer remains.
7. Optionally mark arrival. Add job notes, change worker roles and take an issue photo.
8. After equipment is loaded, choose Finish Job and confirm.
9. Check the client's history and Reports for that job, notes, crew and photo.
10. Export a backup from Setup and keep it private.

Use navigation setup and job controls only while safely parked. This app does not detect vehicle motion or enforce a driving lockout.

## Remaining work before customer rollout

- Identify and authorize the receiving mailbox with a provider-appropriate authentication method.
- Obtain the original `.xlsx` schedule and test the actual sheet names, formulas and column structure.
- Handle separate crew sections, shared destinations, notes, gate-only stops, invalid/blank rows and duplicate emails. Do not assume every schedule line represents a person.
- Implement and verify email retrieval, attachment parsing, review of uncertain addresses/names, and duplicate-safe imports.
- Establish stable production signing and migrate original records without data loss.
- Test camera capture, photo viewing, backup export and navigation on the actual Android 6.0.1 tablet.
- Add in-app backup restore and more structured worker analytics if needed.

## Build

Use JDK 17, Gradle 7.2, Android platform 30 and build-tools 30.0.3, matching the existing project toolchain. Run `gradle assembleDebug lintDebug`. Android minimum API remains 23. Instrumentation tests can be run with `gradle connectedDebugAndroidTest` on a test device.
