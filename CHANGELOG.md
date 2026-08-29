# Changelog (JDupdater fork)

This file only covers changes made in this personal fork since it diverged from
[Tobi823/ffupdater](https://github.com/Tobi823/ffupdater) (originally forked at upstream version 81.0.0 / 179).
For the upstream project's own history, see its repository.

## 84.0.1 (183)
- Added French translations for Vanadium, WebLibre and TrichromeLibrary (previously English-only).
- Added a Settings option for Vanadium's `prebuilt/` subfolder, independent from the branch setting
  (GrapheneOS has changed this folder name across branches before, e.g. `arm64` vs `arm64-multilib`).
- Cleaned up README.md and F-Droid (`fastlane/`) metadata: removed stale browser list entries, a broken
  markdown link left over from removing Brave, outdated "FFUpdater" branding, and links pointing to the
  upstream repository instead of this fork.

## 84.0.0 (182)
- Added WebLibre (independent privacy browser built on Mozilla's Gecko engine, not a Firefox fork).
  Stable releases only - alpha releases were tried first but use a different package/signing setup that
  broke updates when mixed with stable under one app entry.
- Removed Thorium (end of life, no longer maintained upstream).
- Merged TrichromeLibrary into Vanadium's install flow: TrichromeLibrary is no longer shown as a
  separate app. Selecting or updating Vanadium now transparently installs/updates the library first,
  then Vanadium itself, in one step.

## 83.0.1 (181)
- Added WebLibre with alpha releases included (later reverted in 84.0.0, see above).

## 83.0.0 (181)
- Fixed a version-comparison bug: JDupdater's own self-update check always reported "update available"
  even when already up to date, because the GitHub release tag (`v83.0.0`) and the app's internal
  version name (`83.0.0`) were compared with mismatched formats.

## 82.0.0 / 82.0.1 / 82.0.2 (180)
*(82.0.1 and 82.0.2 were re-tags of the same code after a `gradlew` executable-bit CI issue - no functional changes between them.)*
- Rebranded the app from FFUpdater to **JDupdater** (display name only; the underlying Android package
  ID was kept unchanged so existing installs keep updating smoothly).
- Added **Vanadium** and **TrichromeLibrary** (GrapheneOS's hardened Chromium browser and its required
  shared-library dependency), downloaded directly from GrapheneOS's GitLab repository. The GrapheneOS
  branch to track is configurable in Settings.
- Removed Brave, Brave Beta, Brave Nightly, Firefox Focus, Firefox Focus Beta, Firefox Klar, and
  Privacy Browser (apps not used on this fork's target devices).
- Reduced bundled translations to English and French only, to keep the app lighter.
- Set up signed release builds and GitHub Releases via GitHub Actions (previously only builds, no
  releases or code signing).
- Self-update check (the "JDupdater" entry in the app list) now points at this fork's own GitHub
  repository/releases instead of upstream's.
