# Client Route Field 2.1: PDF Import Guide

## What is ready

The app can import the text-based Daily Schedule PDF supplied for this project without needing Excel or mailbox access. It reads the separate crew sections, defaults to Rebecca's crew when present, matches job notes from the notes page, and preserves stop order.

This is a **manual open/share import**, not automatic email retrieval. The intended mailbox is `rebeccafay1992@gmail.com`, but no Gmail account is connected or accessed.

## Install the update

1. Download the attached **Client-Route-Field-2.1.apk** onto the test phone or tablet.
2. Open it and choose **Update** or **Install**.
3. Open **Client Route Field**.

The supplied 2.1 APK uses the same signing key and package as the 2.0 APK supplied in this conversation. It was successfully installed over that build on the Android 6 emulator. Keep your data; do not uninstall or clear storage.

If you installed a separate GitHub Actions APK, its debug signing key may differ. If Android rejects the update, stop and report the message instead of uninstalling. The older app named **Client Route** remains separate, and its records are not automatically migrated.

## Fast daily workflow

While parked:

1. Download the schedule PDF from Gmail onto the tablet.
2. Open **Client Route Field → Import schedule PDF**.
3. Select the downloaded PDF in Android's file picker.
4. Confirm the **job date**. It starts with the date printed in the PDF; change it only if appropriate.
5. Confirm the **crew**. Rebecca's section is selected automatically when identified.
6. Review the stops and their job notes. Uncheck any you do not want.
7. Tap **Import selected stops**, then **Import**.
8. Tap **Open route**.
9. When ready to leave, use **Start & Navigate**. Confirm navigation while parked.

Alternatively, use a PDF viewer's **Share** or **Open with** menu and select **Client Route Field** if it is offered. Sharing behavior varies by Gmail/viewer version; the in-app file picker is the dependable fallback.

## Your supplied PDF

The Android 6 test identified two crew sections and correctly selected the Rebecca/Kyle section with three stops. The corresponding notes from page two were joined to those jobs. Importing the same section and date twice produced three additions on the first attempt and three skipped duplicates on the second.

The supplied PDF's date is September 16, 2026. The **Route date** button on the main screen lets you view an imported route on another date. Selecting the Today tab returns to today's date.

## How information is saved

- Client profiles use the reviewed first name, last name and full address.
- The printed schedule label is retained in the profile's notes when a new profile is created.
- Gate/access flags recognized in a selected stop are saved to the profile.
- Task notes from the PDF's notes page are saved to the specific job.
- Crew names are copied into each job. Roles absent from the PDF are not invented; add them in Job details.
- Imported addresses are preserved in full rather than guessing missing city/state/ZIP components. Editing an imported contact later may require splitting the address into its normal form fields.
- Re-importing the same crew, date and stop skips the existing imported job, including completed jobs. It does not overwrite manually edited notes.
- Changing the imported date creates a different day's jobs.

## Review safeguards and limitations

- The parser supports the specific text-based, letter-size Daily Schedule layout supplied here. It does not promise to parse arbitrary PDFs.
- Scanned/image-only PDFs, encrypted PDFs, Excel workbooks and different table layouts are unsupported.
- Files are limited to 10 MB and 10 pages.
- Depot/Start of Day rows, blank rows and `#N/A` rows are not imported as customer jobs.
- Unusual names, non-numbered addresses and addresses without a comma start unchecked for deliberate review. Use **Edit this stop** and verify the destination before including them.
- Name interpretation follows the supplied surname-first schedule. The preview shows the proposed profile name before saving.
- Duplicate protection applies to imported jobs keyed by date, crew and stop label/address. It does not automatically merge a manually created job or recognize every differently spelled version of an address.
- Changed/revised schedules are not automatically reconciled. Existing matching jobs are skipped; deleted or rescheduled stops require manual review.
- Client records, photos and PDF processing stay on the tablet. No schedule is uploaded by this importer.
- The supplied PDF is a test fixture only; it is not bundled in the production APK or committed to GitHub.

## Verification completed

- Version 2.1 APK built; Android lint completed with zero errors.
- Installed over the supplied 2.0 APK on Android API 23.
- Three instrumentation tests passed, covering timer/profile persistence, optional client fields/main screens, and the actual supplied PDF's extraction/notes/duplicate-safe import.
- Import preview visually inspected at 600×960.
- Real tablet Gmail handoff, GPS navigation, camera capture and file-provider behavior still need a physical-device check.

## Automatic Gmail import later

Rebecca's mailbox address is known, but an address is not authorization. Automatic retrieval still requires her secure account authorization and a provider-supported integration. The original workbook, if obtained tomorrow, will allow an Excel path to be implemented and tested as well.

Do not send email passwords in chat. No unattended inbox sync is running.
