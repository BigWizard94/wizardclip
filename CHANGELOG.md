# Changelog

## 1.0.1

- Fixed the History composer button layout: "Paste from Clipboard",
  "Save clip", and "Save as snippet" now wrap onto a second line on
  narrow phones (FlowRow) instead of squeezing "Save as snippet" into
  a narrow vertical column. Labels, order, icons, and enabled logic
  are unchanged.

## 1.0.0

- Initial local-only release.
- Clipboard history: add a clip by typing, Paste from Clipboard, copy
  any clip back with one tap, delete clips, and clear history
  (snippets survive).
- Search (full-text) and filters: All / Pinned / Snippets. Pin and
  unpin clips.
- Snippets: reusable templates with a title and text — create, edit,
  delete, and copy with one tap.
- Sensitive-clip handling: OTP codes, password-like strings, and
  Luhn-valid card-like numbers are flagged while typing, masked in
  the list, shown with a countdown, and auto-deleted after 5 minutes.
- Share-intent receiver: share text to WizardClip from another app
  (`ACTION_SEND`, `text/plain`).
- Settings: pause capture, clear history, and the privacy /
  limitation notes. Devices screen explains that sync is coming in a
  later update.
- No account, no ads, no tracking, no analytics, no network
  permission. See `PRIVACY.md`.
