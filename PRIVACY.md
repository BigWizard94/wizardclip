# WizardClip Privacy Policy (v1)

WizardClip v1 is local-only. This document states exactly what the
app does in v1; if a later version adds sync, this document will be
updated before that version is released.

## What is stored, and where

- Clips and snippets are stored in a Room database on this device
  only. Nothing is sent anywhere.
- There is no account, no login, and no server in v1.

## What is not collected

- No ads, no tracking, and no analytics.
- No third-party SDKs beyond AndroidX / Room / Compose / Material 3.
- The app declares no network permission in its manifest, so it
  cannot send data over the network in v1.

## Sensitive clips

Sensitive clips — OTP codes, password-like text, and card numbers —
are masked in the list and auto-deleted 5 minutes after saving.
Expired clips are also removed on launch and while the app is open.

## Clipboard capture limits (Android)

Since Android 10, an app can only read the clipboard while it is in
the foreground. WizardClip never reads the clipboard in the
background, never polls it, and has no accessibility service and no
foreground service. Capture in v1 is foreground-only: Paste from
Clipboard while the app is open, typing a clip, or sharing text to
WizardClip.

## Sync roadmap (future work, not in v1)

Planned design, not yet implemented: clips are encrypted on the
device before they leave it, so a relay only ever sees ciphertext,
and the encryption key never leaves the user's devices. Planned
options are LAN / peer-to-peer sync and a self-hosted relay, free
forever, plus an optional paid managed relay. Privacy and encryption
will be identical across all three — paying would only ever pay for
hosting, never for privacy.
