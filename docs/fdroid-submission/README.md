# TrueHub - F-Droid & IzzyOnDroid submission guide

Both stores pull your app from the public GitHub repo. Once they accept the initial
entry, updates flow automatically whenever you tag a new release with a matching
static `versionName`/`versionCode` in `app/build.gradle.kts` and attach the signed
APKs to the GitHub release.

Everything in-repo is ready:
- `fastlane/metadata/android/en-US/` - short/full description, title, icon, and
  screenshots under `images/phoneScreenshots/`.

---
## 1) F-Droid (official) - must build from source
Submit a metadata file into the fdroiddata repo (GitLab) as a merge request.

1. Make sure you have a GitLab account. Then fork:
   https://gitlab.com/fdroid/fdroiddata
2. Create a new branch, e.g. `add-truehub`.
3. Add the file `metadata/com.imnotndesh.truehub.yml` with the contents of
   `fdroiddata-com.imnotndesh.truehub.yml` (next to this file).
4. Push the branch and open a merge request into fdroiddata's `master`.
5. F-Droid maintainers review the source + build recipe, then accept. Builds and
   publishes automatically (typically within a few days of acceptance; first run
   requires human keystore steps).

Note: F-Droid builds from source on their servers; no signed APK required from you.

Alternative (no MR): open a request in the F-Droid app submission queue at
https://f-droid.org -> 'submit an app' to have maintainers package it (slower).

---
## 2) IzzyOnDroid - takes your signed release APK
The repo moved to Codeberg. Request inclusion by opening an issue:

1. Have a Codeberg account (or GitHub account that Codeberg issues allow).
2. Open a new issue at: https://codeberg.org/IzzyOnDroid/repo/issues
   (choose the "new app inclusion" template if offered).
3. Fill in details (see suggested body below). Nothing more is needed since the
   signed release APKs and fastlane metadata are pulled automatically from the
   tagged GitHub release.

Suggested issue subject: `[New App] TrueHub - Android client for TrueNAS SCALE`

Suggested issue body:
```
App Name: TrueHub
Author: Imnotndesh
Source: https://github.com/Imnotndesh/TrueHub
License: GPL-3.0-or-later

Description:
TrueHub is an open-source, modern native Android client for managing a TrueNAS
SCALE server - monitor performance, manage apps, containers and VMs, and stay on
top of alerts, all from your phone.

It builds release-signed per-ABI APKs (~11 MB) attached to tagged GitHub releases.
Fastlane metadata (descriptions + icon + screenshots) are present in the repo at
fastlane/metadata/android/en-US/.

Link to latest release APKs:
https://github.com/Imnotndesh/TrueHub/releases/tag/v0.7.2
```

Izzy's tooling/import will pull your GitHub tag APKs and fastlane metadata, verify,
and (usually within ~24h) list the app.

---
## Tips
- Keep `versionName`/`versionCode` static in `app/build.gradle.kts` and bump by
  hand each release; tag must match (e.g. `v0.7.3` -> `0.7.3`/`70300`).
- Attach the release-signed APKs to the matching GitHub release (the CI does this).
- Add a changelog under `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`
  per release for best listing display.
