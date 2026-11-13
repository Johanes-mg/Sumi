<div align="center">
  <img src="logo.png" alt="Sumi Logo" width="120">

  <h1 align="center">Sumi</h1>

  <h3 align="center">Lightweight calendar and daily status manager</h3>

  <p><strong>A small Android application to track people, personal calendars and daily work statuses, focused on low-memory devices.</strong></p>

  <p>
    <img src="https://img.shields.io/badge/License-MIT-blue" alt="MIT License">
    <img src="https://img.shields.io/badge/Built%20with-Kotlin-purple?logo=kotlin" alt="Kotlin">
    <img src="https://img.shields.io/badge/Interface-Jetpack%20Compose-green?logo=jetpackcompose" alt="Jetpack Compose">
    <img src="https://img.shields.io/badge/Platform-Android-lightgrey?logo=android" alt="Android">
    <img src="https://img.shields.io/badge/Min%20SDK-24-blue" alt="Min SDK 24">
    <img src="https://img.shields.io/badge/Status-Release-brightgreen" alt="Release">
  </p>
</div>

---

## Overview

Sumi is a small Android app written in Kotlin with Jetpack Compose. It helps you register people, maintain a monthly calendar for each of them, and mark daily work statuses (office, leave, mission). Data is stored locally; an optional QR-based synchronization lets two devices share the same calendar.

Source code is available on GitHub:
https://github.com/Johanes-mg/Sumi

Release APKs are attached to GitHub Releases:
https://github.com/Johanes-mg/Sumi/releases

## Features

### People
- Register, rename and delete people.
- Open a dedicated monthly calendar for each person.
- Import other members in read-only mode through QR code or JSON file.

### Monthly calendar
- Navigate month by month.
- Tap a day to cycle through Office, Leave, Mission.
- Create missions that fill every day between a start and an end date.

### Monthly summary
- Live counter of Office, Leave and Mission days for the visible month.

### Appearance
- Light and dark themes.
- French and English interface.
- Borderless layout, black-outlined day indicators.

### Sharing
- Export and import the whole set of local people as a JSON file.
- QR code that carries an initial snapshot.
- Optional real-time sync between two devices via a shared QR identifier.

## Permissions

Sumi requests only the permissions required by its user-facing features.

    CAMERA
    Used exclusively by the QR-code scanner to import a shared calendar
    or to resume an existing synchronization.

    INTERNET
    Used only when the user explicitly triggers the synchronization
    feature (QR code shown or scanned). Not used at any other time.

Sumi does not declare ACCESS_NETWORK_STATE, ACCESS_FINE_LOCATION, or any
other permission. The camera hardware is declared optional so the app
remains installable on devices without a camera.

## Network and privacy

The QR-based synchronization stores the shared calendar on jsonblob.com,
a free third-party JSON storage service. The upload happens only after the
user explicitly shows or scans a synchronization QR code, and continues
for subsequent modifications until the user stops sharing. No analytics,
no tracking, no advertising, no Google Play Services.

If you want a fully offline usage, simply never scan or display a
synchronization QR code. The JSON export and import features work
without any network access.

## Technology stack

- Kotlin
- Jetpack Compose and Material 3
- SharedPreferences for local persistence
- R8 full mode, resource shrinking, baseline profiles
- zxing-android-embedded for QR scanning and generation

## Project structure

- Sumi/
    - app/
        - src/main/
            - java/com/sumijojoh/mg/
                - MainActivity.kt
                - data/
                    - AppSettings.kt
                    - AppSettingsRepository.kt
                    - DayState.kt
                    - JsonExporter.kt
                    - MissionInfo.kt
                    - Person.kt
                    - PersonRepository.kt
                    - QrSync.kt
                    - SyncClient.kt
                - ui/
                    - SumiApp.kt
                    - screens/
                        - PersonCalendarScreen.kt
                        - PersonListScreen.kt
                        - SettingsScreen.kt
                    - strings/Textes.kt
                    - theme/Theme.kt
            - res/
                - mipmap-mdpi, mipmap-hdpi, mipmap-xhdpi, mipmap-xxhdpi, mipmap-xxxhdpi
                - values/themes.xml
            - AndroidManifest.xml
        - build.gradle.kts
        - proguard-rules.pro
    - gradle/wrapper
    - build.gradle.kts
    - settings.gradle.kts
    - gradle.properties
    - gradlew, gradlew.bat
    - logo.png
    - LICENSE.md
    - README.md
    - .gitignore

## Build from source

Requirements:
- OpenJDK 17
- Android SDK with platform 34 and build-tools
- Linux, macOS or Windows

Clone the repository:

    git clone https://github.com/Johanes-mg/Sumi.git
    cd Sumi

Build the debug APK:

    ./gradlew assembleDebug

Output is written to app/build/outputs/apk/debug/app-debug.apk.

Build a signed release APK. Create a signing.properties file at the project root:

    storeFile=release.jks
    storePassword=...
    keyAlias=...
    keyPassword=...

Then run:

    ./gradlew assembleRelease

Output is written to app/build/outputs/apk/release/app-release.apk.

## Design principles

- Clarity: every screen must be understandable at a glance.
- Efficiency: minimal allocations to reduce garbage collection.
- Consistency: identical visual behaviour for identical actions.
- Lightness: keep the release APK small and fast on entry-level devices.

## License

MIT License. See LICENSE.md for the full text.

Copyright (c) 2024-2025 Johanes-mg

## Author

- Johanes-mg

---

<div align="center">
<strong>Sumi - Simple by design.</strong>
</div>
