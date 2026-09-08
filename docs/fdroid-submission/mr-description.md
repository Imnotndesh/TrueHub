## Summary

New app submission: **TrueHub** — a native Android client for managing a self-hosted TrueNAS SCALE server (applications, containers, VMs, and alerts) from your phone.

- **App ID:** `com.imnotndesh.truehub`
- **License:** GPL-3.0-or-later
- **Source:** https://github.com/Imnotndesh/TrueHub

### Requirements

- [x] The app complies with the inclusion criteria
- [x] The original app author has been notified (I am the author, see `@Imnotndesh` / slateinbox)
- [x] All related fdroiddata and RFP issues have been referenced in this merge request
- [x] Builds with `fdroid build` and all pipelines pass
- [x] There is an issue tracker (https://github.com/Imnotndesh/TrueHub/issues) and contact info of the author

### Strongly Recommended

- [x] The upstream app source code repo contains the app metadata (summary/description/images/screenshots) in a Fastlane folder structure at `fastlane/metadata/android/en-US/`. Summary & description are therefore maintained upstream and not duplicated here.
- [x] Releases are tagged and auto update is enabled (`AutoUpdateMode: Version`, `UpdateCheckMode: Tags`)

### Notes for reviewers

- Gradle: `./gradlew assembleGithubRelease` (JDK 17), run in `app/` subdir.
- Release static version: `0.7.2` / `70200`, matching tag `v0.7.2`.
- No proprietary dependencies; no GMS/Firebase. `github` flavor only (fully free).
- No `disable:` line present.

No related RFP/fdroiddata issue to close — removing the `Closes` lines.
