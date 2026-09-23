# Changelog (JDupdater fork)

**English** | [Français](CHANGELOG.fr.md)

This file only covers changes made in this personal fork since it diverged from
[Tobi823/ffupdater](https://github.com/Tobi823/ffupdater) (originally forked at upstream version 81.0.0 / 179).
For the upstream project's own history, see its repository.

## 86.1.2 (213)
**Fixed**
- The "project page" link shown at the top of the app info dialog ("i" button) pointed to a Mozilla
  marketing page (`mozilla.org/firefox/browsers/mobile/android/`) instead of the actual source code for
  Firefox Release, Firefox Beta and Firefox Nightly. It now points to the real repository,
  `github.com/mozilla-firefox/firefox`.
- Firefox Klar's project page pointed to `github.com/mozilla-mobile/firefox-android`, which was archived on
  2024-06-17 when Fenix/Focus/Klar and Android Components were merged into the unified mozilla-central repo.
  It now points to the same current repository as above.
- Tor Browser and Tor Browser Alpha's project page pointed to a torproject.org download page instead of the
  source. It now points to the actual source repository,
  `gitlab.torproject.org/tpo/applications/tor-browser`.

## 86.1.1 (212)
**Fixed**
- Fixed the "Available" line on the main screen wrapping awkwardly mid-date on longer version numbers or
  older releases (e.g. "Disponible : 153.0.8010.52.0 (Il y a 4 jours, 23:19)" breaking across two lines).
  The relative age now sits on its own (smaller) line below the version, and uses a shorter, abbreviated
  format ("il y a 4 h" / "5 sept." instead of "il y a 4 heures, 13:16" / "5 sept., 16:47").
- Fixed Firefox Nightly's card showing a raw, unformatted build timestamp in square brackets (e.g.
  `[2026-09-22T21:13:42]`) next to both the installed and available version, on top of the already-shown
  relative age - which made its card wrap onto 3-4 lines and get cut off, unlike every other app. This
  internal build time (used only to tell apart two Nightly builds sharing the same version text) is no
  longer shown on the card.

## 86.1.0 (211)
**Removed**
- Removed Vivaldi: it is only partially open source (the Android/desktop UI is proprietary, only the
  Chromium core is open).
- Removed FairEmail, K-9 Mail, Thunderbird and Thunderbird Beta: they are email clients, not browsers.
  (Orbot stays: it isn't a browser either, but it's actively maintained and was already listed under
  "Other applications", not the browser categories.)

**Added**
- Installed apps whose latest known release is 90+ days old now show a grey "Outdated" badge on their card,
  next to the existing blue "Update" badge, so you notice even without opening the app's info dialog.
- The "Add app" screen already had an unused "End-of-life browser" section (`DisplayCategory.EOL`). Not-yet-
  installed apps whose latest known release is 90+ days old are now shown there automatically instead of
  their usual category (same 90-day rule as the badge above), rather than needing to be hardcoded into that
  category by hand. Apps tracked by branch instead of dated releases (Vanadium, TrichromeLibrary) are never
  affected, since their release has no publish date to measure an age from.

**Fixed**
- Fixed cached self-update APKs (`FFUPDATER_<...>.apk` in `Android/data/de.marmaro.krt.ffupdater/files/download`)
  never being deleted: silently self-installing JDupdater always requires user interaction (no root/Shizuku),
  so `background__delete_cache_if_install_failed` (off by default) kept every failed self-update download
  forever, and downloads started from the notification/`DownloadActivity` never cleaned up older cached
  versions at all (only `AppUpdater`, the background path, did). `DownloadActivity` now deletes cached APKs
  of older versions of the app being downloaded before starting a new download, regardless of those settings.
  Existing leftover files must still be deleted once by hand (e.g. with a file manager); this only prevents
  new ones from piling up.
  This affects every app, not just JDupdater: a failed background install (unusual for regular browsers,
  which usually only need Android 12+/root/Shizuku to install silently in the first place) no longer leaves
  its cache behind either. You can still turn it back off per app category in Settings.

## 86.0.1 (210)
**CI**
- The Android workflow signs the APK itself with `apksigner` (instead of the unmaintained
  `r0adkll/sign-android-release@v1`, which used Node 20, the deprecated `set-output` command and old
  build tools 29.0.3) and fails if the signing certificate is not the one expected by the self-update check.
  `actions/checkout`, `actions/upload-artifact` and `softprops/action-gh-release` were updated to their
  Node 24 versions.

**Cleanup**
- Fixed the Kotlin compiler warnings: removed the unused deprecated functions in `PowerSaveModeReceiver`,
  the unused parameter of `RootInstaller` (`appImpl`), the status code of `UserInteractionIsRequiredException`
  is now part of its message, `SessionInstaller` uses `BundleCompat` instead of the deprecated
  `Bundle.getParcelable` and the pointless `inline` of `setVisibleOrGone` is gone.

**Documentation**
- Added a French README ([README.fr.md](README.fr.md)) and a French changelog
  ([CHANGELOG.fr.md](CHANGELOG.fr.md)). Both are linked at the top of the English files, so they are
  reachable from the GitHub repository page.

## 86.0.0 (209)
**Fixes**
- Fixed `FileDownloader.areDownloadsCurrentlyRunning()`: the counter of running downloads was never
  incremented, so the "downloads are running" safeguards (dialog, background workers) never triggered.
- Apps that rotated their signing key (APK Signature Scheme v3 lineage) are no longer rejected with
  "Found multiple signatures". The stored certificate is accepted when it is the signing certificate or part
  of the verified certificate lineage.
- Fixed system-bar margins that grew every time the window insets were dispatched (main, download,
  "update all" and crash report screens). Only the settings screen had been fixed before.
- Fixed the light theme: the toolbar stayed light (Material You dynamic colors override `colorSurface`) while
  its title and icons are white, so they were invisible. The toolbar and the status bar area are now always
  blue with white title, icons and status bar icons. The "update" badge on the app cards has a darker
  background in the light theme for a readable contrast.
- `VersionCompareHelper` no longer offers a downgrade when the installed major version is much newer than the
  available one (e.g. 153.x installed, 140.x available). The "version schema changed" detection
  (Tor Browser 128.x to 14.x) is limited to a drop from a 3-digit to a 1-2 digit major version.
- Added the 39 missing French translations (update-all screens and dialogs, Cromite, TrichromeLibrary,
  Thunderbird, K-9 Mail, FairEmail, IronFox, ...).
- The message shown when JDupdater itself was signed with a different key no longer talks about F-Droid.

**Cleanup**
- Removed the unused `androidx.compose.material3` dependency.
- Removed 6 unused logos (Brave, Brave Beta/Nightly, Firefox Focus Beta, Privacy Browser, Thorium) and two
  unused `<queries>` entries (Thorium, Firefox Rocket).
- `dev/signatures/apk_signature.txt` and `docs/security_measures.md` now describe this fork's signing key
  instead of the upstream one.
- Merged `CORRECTIF-VANADIUM.txt` into this changelog (see 85.x below).

**CI / tests**
- CodeQL: updated to `codeql-action` v4 (v3 is deprecated in December 2026), analyzes only
  `java-kotlin` and `python` (there is no JavaScript code).
- The Android workflow no longer builds or publishes a debug APK.
- Fixed the unit tests, which did not compile since Brave was removed (`App.BRAVE`), and added tests for the
  signing certificate lineage and the version comparison.

## 85.0.0 - 85.1.8 (up to 207)
*(The individual release notes of the 85.x versions were not kept, so the changes are listed together.)*
- Re-added Firefox Klar (removed in 82.0.0), using the original upstream implementation
  (Mozilla archive, package `org.mozilla.klar`) with English and French descriptions.
- Fixed CodeQL alert "Use of implicit PendingIntents": the battery-optimization notification intent is now
  made explicit, and notification PendingIntents are immutable from Android 6 (was Android 12).
- Fixed the Vanadium settings being invisible: the nested `PreferenceScreen` is now a `PreferenceCategory`
  (`root_preferences.xml`).
- Fixed the empty settings screen after confirming/cancelling a dialog: the keyboard broke the
  `RecyclerView`. `SettingsActivity` now uses `adjustNothing`, re-lays out when the dialog closes and the
  window insets no longer accumulate.
- Fixed missing update notifications for Vanadium: GrapheneOS installs Vanadium/TrichromeLibrary itself
  (installer = system apps), so JDupdater ignored them in the background. `wasInstalledByOtherApp()` now
  returns `false` for both (same GrapheneOS signature as the APKs downloaded by JDupdater).
- Fixed the bogus version `0.0.0.0-<sha>` and the permanent "update available": the head commit of the
  GitLab branch is sometimes a config commit without "version X" in its title. `GitLabBranchConsumer` now
  queries `/repository/commits?path=<APK>` and uses the last commit that really changed the APK.
- Fixed temporary downloads that were never cleaned up (upstream #787): the temporary file is now named
  `<UUID>.part` (the dot was missing) and `StorageCleaner.deleteOrphanedTempFiles()` deletes
  `<UUID>[.apk|.zip|.part|apk|zip]` files older than one hour at startup.

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
