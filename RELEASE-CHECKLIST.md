# Release checklist (draft — no publishing action taken)

Status of v1.0.1 in this change: button-layout fix, version bump
(versionCode 2 / versionName 1.0.1), `CHANGELOG.md`, and `PRIVACY.md`
are done; the GitHub Actions debug build must be green before any
release. Device testing is for Chase — this app has not been tested
on a device or emulator by the builder.

## Remaining decisions / Chase-only items

- [ ] Release keystore: create a release signing keystore, store it
      outside the repository, and back it up externally (password
      manager / encrypted USB). Losing it breaks the update path for
      existing installs. No keystore was created in this change, and
      no password is recorded anywhere in this repository.
- [ ] Release signing in CI: add the keystore to GitHub Actions
      secrets and a release build workflow only after the keystore
      exists and is backed up.
- [ ] Distribution decision: GitHub Release (free, direct download)
      vs F-Droid submission (free, needs fastlane metadata and
      review) vs Google Play ($25 developer registration fee).
      Play was previously skipped for another Bigwizard Media app
      over the fee — confirm before assuming it.
- [ ] Screenshots for store listings: phone screenshots of History,
      Snippets, Devices, and Settings, plus an app icon / feature
      graphic if a store listing requires one.
- [ ] Device test of v1.0.1 on a real phone: paste/copy, search/pin,
      snippet edit, Share-to-WizardClip, sensitive-clip countdown /
      expiry, pause toggle, dark theme.
- [ ] Sync Phase 2 scope (separate work, not part of v1.0.1):
      LAN / peer-to-peer first, or self-hosted relay first, and the
      E2E encryption design. No sync code is in v1.0.1.

## Do not do for a v1 release

- Do not add an INTERNET or other network permission to the v1
  local-only build.
- Do not publish a release, submit to a store, or auto-publish from
  CI until the items above are decided.
