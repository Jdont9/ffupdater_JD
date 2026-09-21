# Security measures

- The signature fingerprint of every downloaded APK file is validated against an internal allowlist. This
  prevents the installation of malicious apps that do not originate from the original developers.
- Only HTTPS connections are used because unencrypted HTTP traffic can be manipulated.
- Only system certificate authorities are trusted. But this can be disabled in the settings to allow other
  apps to inspect the application's network traffic.
- Prevent command injection in the RootInstaller.kt by validating and sanitizing commands.
- The APK of this fork (JDupdater) is signed with the key whose SHA-256 fingerprint is listed in
  [apk_signature](../dev/signatures/apk_signature.txt). It is the same fingerprint that the app uses to verify
  its own updates (see `FFUpdater.kt`).
