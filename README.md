# WizardClip

A privacy-focused clipboard manager for Android, by Bigwizard Media.

WizardClip keeps a searchable history of your clips, pinned clips, and
reusable snippets — locally, on your device, with no account, no ads,
no tracking, and no analytics.

## Latest build

Debug-signed preview build, updated automatically after every
successful push to `main` as one rolling prerelease (not a stable
release). This is the fastest way to get the newest APK without
waiting for anyone to relay it.

- Release page: https://github.com/BigWizard94/wizardclip/releases/tag/latest
- Direct APK download: https://github.com/BigWizard94/wizardclip/releases/download/latest/WizardClip-debug.apk

Install: download the APK, allow install from unknown sources /
sideloading when Android asks, then open the APK to install. The
checksum file `WizardClip-debug.apk.sha256` is attached to the same
prerelease. Versioned stable releases, if any, will be separate `v*`
tags.

## Features (v1, local-only)

- Clipboard history: add a clip by typing, with **Paste from Clipboard**,
  copy any clip back with one tap, delete clips, and clear history
  (snippets survive).
- Search (full-text) and filters: All / Pinned / Snippets.
- Pin and unpin clips.
- Snippets: reusable templates with a title and text — create, edit,
  delete, and copy with one tap.
- Sensitive-clip handling: OTP codes, password-like strings, and
  Luhn-valid card-like numbers are flagged while you type, masked in
  the list, shown with a countdown, and auto-deleted after 5 minutes
  (expired clips are also removed on launch and while the app is open).
- Share-intent receiver: share text to WizardClip from another app
  (`ACTION_SEND`, `text/plain`) and it is saved as a clip.
- Settings: pause capture, clear history, and the privacy / limitation notes.
- Devices screen: LAN, self-hosted, and managed sync are honestly marked
  as coming in a later update — v1 has no sync and no simulated sync.
- Purple/teal Material 3 design, light and dark themes, edge-to-edge.

## The honest Android limitation

Since Android 10, an app can only read the clipboard while it is in the
foreground. Since Android 12, Android shows a toast whenever an app
reads the clipboard, and since Android 13 the system may clear the
clipboard after about an hour.

WizardClip works with those limits instead of working around them. It
never reads the clipboard in the background, never polls it, and has
no accessibility service and no foreground service. Capture in v1 is
foreground-only: **Paste from Clipboard** while the app is open,
typing a clip, or sharing text to WizardClip. The app says this
in-app, in Settings and on the History screen.

## Privacy model

- **Local-only in v1.** Clips and snippets live in a Room database on
  this device. Nothing is sent anywhere.
- **No network permission.** The manifest declares no permissions at
  all. The local-only claim is true in the manifest, not just in docs.
- **No third-party SDKs** beyond AndroidX / Room / Compose / Material 3.
  No analytics or crash-reporting SDKs.
- **No account, no login.**
- Sensitive clips are masked and auto-deleted after 5 minutes.

Sync roadmap (not in v1): local-first, zero-knowledge end-to-end
encryption (the server only ever sees ciphertext), LAN / peer-to-peer
and self-hosted relay free forever, and an optional paid managed relay
later. Privacy and encryption will be identical across all three —
paying would only ever pay for hosting, never for privacy.

## Building

Requirements: JDK 17, Android SDK with platform `android-36` and
build-tools `36.0.0`.

```sh
./gradlew assembleDebug
```

The debug APK is at:

```
app/build/outputs/apk/debug/app-debug.apk
```

Run the unit tests (sensitive-clip detector):

```sh
./gradlew testDebugUnitTest
```

GitHub Actions builds the debug APK on every push to `main` and
uploads it as a workflow artifact. Debug signing only — there is no
release keystore in this repository.

## Project

- Package: `info.bigwizardmedia.wizardclip`
- Language: Kotlin + Jetpack Compose + Material 3, Room 3
- `compileSdk` / `targetSdk`: 36, `minSdk`: 26
- License: MIT (see `LICENSE`)

WizardClip is a working name and may change.
