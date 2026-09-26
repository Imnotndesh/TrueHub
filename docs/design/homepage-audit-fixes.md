# Homepage Module — Recommended Changes (HCI & Accessibility)

Companion to `homepage-audit-scores.md`. This is the action document: what to change, in what
order, and why. Standards: `docs/design/hci-android-guidelines.md` and
`docs/design/m3-expressive-research.md` (§8–§10).

Finding IDs (`g<group>-<n>`) map to the raw reports in `docs/design/homepage-audit/g1.md` … `g6.md`.

## Priority model

| Level | Meaning | Action |
|---|---|---|
| **P0** | Blocks a user group or risks data loss / lockout | Fix before next release |
| **P1** | Significant usability or accessibility degradation | Fix in the current hardening pass |
| **P2** | Polish, consistency, and code hygiene | Batch into a cleanup pass |

## 1. P0 — fix immediately

### P0-1 · `g3-16` — 2FA renewal can permanently lock the user out
`users/UserDetailScreen.kt:537-542`, `UserSettingsViewModel.kt:180-196`
"Renew 2FA Secret" succeeds, the sheet closes, and `clearRenew2faResult()` discards the
response. That response's `twofactor_config.provisioning_uri` is the **only** copy of the new
TOTP enrolment material, and nothing in the app renders it. The old secret is already
invalidated → the account can no longer produce valid codes.

**Change:** before closing, show the returned `provisioning_uri` in a one-time reveal surface
(QR + selectable text + Copy). Warn *before* renewing that the old secret dies immediately.
Do not clear the result until the user dismisses the reveal.

### P0-2 · `g3-43` — API key reset destroys the only copy of the new key
`apikeys/ApiKeyDetailsScreen.kt:168-173`, `ApiKeyVIewModel.kt:146-158`, `SystemService.kt:891-899`
Reset issues **new key material** (`ApiKeyEntryWithKey.key`), but the ViewModel types the
response as `ApiResult<Any>`, discards it, and re-fetches an entry that only has `keyhash`.
The confirmation even promises "The old key will stop working immediately", so the user
irreversibly kills a working credential and the replacement is thrown away.

**Change:** populate the declared-but-unused `resetResult`, render the new key in the same
one-time reveal used by create, and keep the warning.

### P0-3 · `g4-17` — every outcome message on the alert-service detail screen is invisible
`alertservice/AlertServiceDetailScreen.kt:73, 91, 100, 107`
A `SnackbarHostState` is created and `showSnackbar` is called, but the file composes **no**
`SnackbarHost`. Test/copy/create/update/error messages never render, so a passing and a failing
alert-service test look identical. Secondary effect: `showSnackbar` suspends forever, so
`clearError()` never runs.

**Change:** add a `SnackbarHost` (Scaffold or overlay), move `clearError()` out of the suspend
path, and add the same host to any sibling screen that calls `showSnackbar` without one.

## 2. Systemic themes (highest leverage)

These repeat across many screens. Fixing them in the shared component fixes every call site at
once, which is where most of the 110 P1s live.

### T1 · Unlabelled toggle controls (A2) — ~15 screens, biggest single a11y defect
A `Switch`/`Checkbox` sits in a plain `Row` next to a sibling `Text`; nothing merges or labels
them, so TalkBack announces "Switch, off" with no idea what it controls. On security/ops
toggles (password-disabled, start-on-boot, keep-from-deletion, enable-service) this is
blocking.

*IDs:* `g2-16`, `g2-20`, `g2-30`, `g2-38`, `g3-14`, `g3-20`, `g5-14`, `g5-20`, `g5-40`,
`g5-49`, `g5-54`, `g6-33`, `g6-36`, `g6-55`, `g6-65`

**Fix (once):** in `ui/components/expressive/ExpressiveForm.kt:98` make `ExpressiveToggleRow`
the single pattern:
```kotlin
Row(Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)) {
    Text(label); Switch(checked = checked, onCheckedChange = null)
}
```
Then migrate every screen to it and delete the local `ToggleRow` copies.

### T2 · Hardcoded status colours used as text (A4)
Literal `#2E7D32` / `#F57C00` / `#C62828` / `#6A1B9A` are used for small label text on theme
surfaces. Measured composites fall to ≈1.9:1–4.2:1 depending on theme, below the 4.5:1 floor
for <18sp text; MONOCHROME/DYNAMIC palettes and OLED black are ignored.

*IDs:* `g1-02`, `g1-29`, `g3-01`, `g3-19`, `g3-33`, `g3-46`, `g4-02`, `g4-26`, `g5-35`,
`g5-58`, `g6-32`

**Fix:** route all of them through the existing `StatusTone` helper
(`ui/components/expressive/ExpressiveStatus.kt:36-43`) backed by colour-scheme roles; keep hue
on the status **dot** only and put the label in `onSurface`/`on*Container`.

### T3 · Motion bypasses the theme (H6)
The theme opts into `MotionScheme.expressive()`, but ~19 sites hardcode
`spring(stiffness=…)`/`tween(…)`, so custom animation drifts from the components beside it.

*IDs:* `g1-09`, `g1-17`, `g1-25`, `g1-33`, `g3-05`, `g3-53`, `g4-08`, `g4-15`, `g4-24`,
`g4-30`, `g4-39`, `g5-18`, `g6-04`, `g6-22`, `g6-34`, `g6-39`, `g6-51`, `g6-60`
*Shared too:* `ExpressiveFAB.kt:56-63,75-80` (`g2-26`), `MainScreen.kt:346-354` NavHost
transitions.

**Fix:** replace with `MaterialTheme.motionScheme.defaultSpatialSpec()` /
`fastSpatialSpec()` / `defaultEffectsSpec()` / `fastEffectsSpec()`. Add `Modifier.animateItem()`
to list rows (`g4-08`) and wrap 0 ms content swaps in `AnimatedContent`/`AnimatedVisibility`.

### T4 · No heading semantics anywhere (A3)
`heading()` appears zero times in `ui/`. Neither the screen title (`UnitedScreenHeader`) nor any
section title is navigable, so TalkBack users must linear-scan every screen.

*IDs:* `g1-18`, `g1-34`, `g2-02`, `g3-22`, `g4-38`, `g5-25`, `g6-11`, `g6-16`, `g6-24`
**Fix (once):** add `Modifier.semantics { heading() }` inside `UnifiedScreenHeader`'s title and
inside `ExpressiveSection`'s title — every screen inherits it.

### T5 · Header back button disabled while loading/refreshing (H2)
`UnitedScreenHeader.kt:118` sets `enabled = !isLoading && !isRefreshing`, so the visible escape
hatch greys out for seconds during any fetch — exactly when a hung request makes the user want
it. System back still works, so the control is contradictory.

*IDs:* `g2-11`, `g3-07`, `g4-05`, `g5-46`
**Fix:** keep Back always enabled; gate only re-entrant/destructive work.

### T6 · No max content width and no window-size-class use (H5)
`widthIn` is used in none of the settings screens; `AdaptiveLayoutHelper` is used only in
Apps/Containers/VMs. On medium/expanded windows, rows, cards and buttons stretch to ~1000dp,
violating the ~60-character measure and every adaptive guideline.

*IDs:* `g1-08`, `g1-27`, `g2-03`, `g3-04`, `g4-37`, `g5-11`, `g5-26`, `g6-03`, `g6-13`,
`g6-17`, `g6-41`, `g6-48`, `g6-59`
**Fix:** constrain content (`widthIn(max = 840.dp)`, forms `560–640.dp`) and adopt
`currentWindowAdaptiveInfo()`; see the migration path in `m3-expressive-research.md` §9.2.

### T7 · Error states that dead-end or lie (H1/H3)
Either the failure is swallowed and the screen asserts a false normal state, or it renders an
empty body with no Retry. This is the root of the H3 = 2.6 score.

*False-status examples:* `g1-11` (SMART toggle reported as drive health), `g1-20` (`"Live"`
but `onRefresh = {}`), `g1-21` (28 GB/32 GB memory verdict), `g1-23` ("Normal" with no metrics),
`g2-06` ("Disks: 0" during load), `g4-01` (failure shown as "No alert services configured"),
`g5-19` (load failure shown as "Environment not found"), `g6-19` ("System is up to date" after a
silent refresh failure), `g6-61` (all path errors shown as "Empty folder").
*Blank/no-retry examples:* `g2-07`, `g2-12`, `g2-13`, `g2-24`, `g2-34`, `g4-18`, `g4-33`,
`g4-47`, `g5-01`, `g5-09`, `g5-39`, `g5-45`, `g5-51`, `g6-14`, `g6-26`, `g6-46`, `g6-57`,
`g1-13`.
**Fix:** model `idle / loading / error / loaded` explicitly; render a distinct error body with
**Retry**; never display a derived "healthy/normal/up-to-date" value that was not loaded.

### T8 · Dynamic status and errors are silent to screen readers (A3)
Errors, toasts, load transitions and status changes are not `liveRegion`.

*IDs:* `g1-01`, `g1-18`, `g1-30`, `g3-03`, `g3-28`, `g4-41`, `g5-03`, `g6-23`, `g6-67`
*Shared:* `UnitedScreenHeader.kt:218-240` error card (`g6-09`), `ModernToast.kt:143-160`.
**Fix:** add `liveRegion = LiveRegionMode.Polite` to the shared error card, the toast host, the
action banner, and status/progress rows; use `progressBarRangeInfo` for progress.

### T9 · Form state is not saved across rotation/process death (A5)
Form fields use `remember`, and the manifest declares no `configChanges`, so rotating mid-entry
wipes typed input (including passwords).

*IDs:* `g1-15`, `g3-13`, `g3-21`, `g3-29`, `g3-41`, `g4-11`, `g4-21`, `g5-30`, `g5-44`
**Fix:** `rememberSaveable` (or ViewModel/SavedStateHandle). Add a dirty-state guard on
back/Discard for long forms (`g2-31`, `g4-21`, `g4-28`, `g4-36`, `g5-28`, `g5-44`).

### T10 · `!!` on nullable API data (crash risk)
Non-null assertions on fields the models declare nullable; any omitted field crashes the screen.

*IDs:* `g2-19`, `g2-47`, `g6-25`, `g6-53`
**Fix:** `?.` with "—"/"N/A" fallbacks; make required parameters non-null instead of asserting.

### T11 · Fixed heights clip content at large font scale (A5)
`Modifier.height(N.dp)` prevents M3's minimum-interactive-size expansion and clips text as
`fontScale` grows.

*IDs:* `g1-03`, `g2-10`, `g2-22`, `g2-32`, `g2-40`, `g3-49`, `g3-50`, `g5-10`, `g6-31`
**Fix:** `heightIn(min = …)`, allow wrapping/stacking, cap `maxLines` only where ellipsis is
acceptable.

### T12 · Touch targets below 48dp (A1)
*IDs:* `g1-03` (40/44dp dialog button), `g3-51` (~36dp "Done"), `g4-25` (~24dp expand row),
`g5-02` (28dp dismiss), `g5-13` (40dp icon buttons), `g6-42` (24dp chevron),
`g6-54` (20dp chevron), `g6-63` (~44dp rows)
**Fix:** remove explicit `size()` overrides or wrap in `sizeIn(minWidth = 48.dp, minHeight = 48.dp)`;
use `IconButton`.

### T13 · Clickables without role / selection semantics (A2/A3)
Hand-rolled segmented controls, cards and tree rows expose no `Role`, `selected`, or
`stateDescription`.

*IDs:* `g1-04`, `g4-29`, `g6-01`, `g6-44`, `g6-50`, `g6-55`
**Fix:** `clickable(role = Role.Button)`, `selectable(selected, role = Role.RadioButton)`,
`toggleable(role = Role.Checkbox)`, `expandable(...)`.

### T14 · Sheets/modals without `paneTitle` (A3)
*IDs:* `g2-43`, `g3-22`, `g6-23`, `g6-58`, `g6-67`
**Fix:** add `Modifier.semantics { paneTitle = "…" }` to each sheet; pattern already exists at
`UserListScreen.kt:339` and `AlertServicesListScreen.kt:190`.

### T15 · Destructive actions without confirmation (H3)
*IDs:* `g6-08` (trash icon opens "Image Details" whose Delete is unconfirmed), `g6-18`
("Download & Restart Now" reboots immediately), `g5-57` (Stop/Restart can disconnect clients),
`g1-31` (delete has no undo).
**Fix:** confirm with the target named and the consequence stated; tint the confirm action
`error`; add undo where feasible.

### T16 · Missing success/failure feedback (H1)
*IDs:* `g1-30`, `g3-11`, `g3-17`, `g3-18`, `g3-45`, `g4-12`, `g4-41`, `g5-36`, `g5-41`,
`g6-07`, `g6-37`
**Fix:** emit a toast/snackbar on every mutating outcome (to the destination if navigating);
make the shared toast a live region. Note `g4-12`: show the snackbar **after** navigating, not
before — `showSnackbar` suspends ~4 s and makes a successful create look hung.

### T17 · Decorative icons given redundant `contentDescription` (A2)
The icon repeats adjacent text, so TalkBack announces the value twice (or announces the
username instead of the status).

*IDs:* `g2-01`, `g2-04`, `g2-48`, `g4-44`, `g5-04`, `g5-27`, `g5-50`, `g5-56`, `g6-48`,
`g6-49`
**Fix:** `contentDescription = null` on decorative icons; let the visible `Text` be the label.

### T18 · Duplicated components instead of the shared expressive layer (H4/H6)
Screens re-implement `ExpressiveEntryCard`, `ExpressiveStatusChip`, `PullToRefreshContent`,
`InfoRow`, and two FAB styles with weaker semantics/hardcoded colours.

*IDs:* `g1-16`, `g4-22`, `g5-05`, `g5-10`, `g5-37`, `g6-10`
**Fix:** consolidate on `ui/components/expressive/*`; fixing the shared component fixes all
call sites (and most of T1/T2/T3 at once).

### T19 · Hardcoded strings, no localisation (H5)
`stringResource` appears zero times across `instancesettings/*`; all copy is hardcoded English,
so text expansion cannot be checked and the app cannot be translated.

*IDs:* `g2-05`, plus ungrammatical count strings `g4-07`, `g4-46`
**Fix:** move user-facing copy to `strings.xml` with `plurals` for counts.

## 3. Per-area P1 register

### G1 — Homepage dashboard & details

| ID | Screen | Change |
|---|---|---|
| `g1-01` | `Homepage.kt` | Connectivity is a colour-only 10dp dot: add a labelled "Connected/Offline" chip + icon, `stateDescription` + `liveRegion`, dot decorative |
| `g1-02` | `Homepage.kt` | Orange `#F57C00` text at ≈2.7:1 in light theme → theme role (T2) |
| `g1-03` | `Homepage.kt` | Dialog Cancel pinned to 40/44dp → drop fixed height, `heightIn(min = 48.dp)` (T11/T12) |
| `g1-04` | `Homepage.kt` | Power-action selector has no `selectable`/`Role`/`stateDescription` (T13) |
| `g1-05` | `Homepage.kt` | Shares capped at 5; "+n more" looks tappable but is inert → real action or show all |
| `g1-11` | `DiskInfoScreen.kt` | "Drive Health: Healthy/100%" derived from the SMART **toggle** → use real health data; drop fabricated progress |
| `g1-12` | `DiskInfoScreen.kt` | Three badges in a non-wrapping `Row` clip at 200% font → `FlowRow` |
| `g1-20` | `PerformanceScreen.kt` | "Live"/"current" data is frozen (`onRefresh = {}`) → real refresh or a "snapshot from HH:mm" label |
| `g1-21` | `PerformanceScreen.kt` | Hardcoded 28 GB/32 GB memory verdict → compute % from reported total |
| `g1-22` | `PerformanceScreen.kt` | Chart is an `AndroidView` with no semantics → summary contentDescription / text min-max-avg |
| `g1-23` | `PerformanceScreen.kt` | Null metrics render "CPU · 0 pts / Normal" → explicit empty state |
| `g1-28` | `ShareInfoScreen.kt` | Feature chips in non-wrapping `Row`s clip → `FlowRow` |
| `g1-29` | `ShareInfoScreen.kt` | Green `#2E7D32` text ≈3.3:1 on dark → theme role (T2) |
| `g1-30` | `ShareInfoScreen.kt` | Delete outcome never announced → `liveRegion`/snackbar (T8/T16) |

### G2 — System info & general/advanced settings

| ID | Screen | Change |
|---|---|---|
| `g2-06` | `HardwareInformationScreen.kt` | "Disks: 0" shown during load → gate every card on load state |
| `g2-07` | `HardwareInformationScreen.kt` | `error` never assigned → set from `ApiResult.Error` + Retry |
| `g2-12` | `SoftwareInformationScreen.kt` | `error` never assigned → permanent "—" with no diagnosis |
| `g2-13` | `SoftwareInformationScreen.kt` | No loading branch → skeleton/LoadingScreen |
| `g2-16` | `TrueCommandScreen.kt` | Unlabelled Switch (T1) |
| `g2-17` | `TrueCommandScreen.kt` | >16 chars silently discarded → counter + error; show existing-key state |
| `g2-19` | `TrueNasConnectScreen.kt` | `!!` on nullable fields → `?:` fallback (T10) |
| `g2-20` | `TrueNasConnectScreen.kt` | Unlabelled Switch (T1) |
| `g2-28` | `GeneralSystemSettingsEditScreen.kt` | Invalid port → silent `null` → validate, `isError`, block Save |
| `g2-29` | `GeneralSystemSettingsEditScreen.kt` | Free-text enum fields → dropdown/chips |
| `g2-30` | `GeneralSystemSettingsEditScreen.kt` | 3 unlabelled Switches (T1) |
| `g2-36` | `AdvancedSystemSettingsEditScreen.kt` | Numeric fields silently null → validate + block Save |
| `g2-37` | `AdvancedSystemSettingsEditScreen.kt` | Delete disabled at 1 syslog row with no reason → allow empty list or explain |
| `g2-38` | `AdvancedSystemSettingsEditScreen.kt` | 8 unlabelled Switches + duplicate "Remove" (T1) |
| `g2-41` | `GlobalVirtSettingsScreen.kt` | Editor dismissed before result → keep open / retain draft |

### G3 — Users & API keys

| ID | Screen | Change |
|---|---|---|
| `g3-01` | `UserListScreen.kt` | Hardcoded status palette ≈1.9:1 in dark/OLED → theme tones (T2) |
| `g3-08` | `UserCreateScreen.kt` | Password typed in clear → `PasswordVisualTransformation` + reveal toggle |
| `g3-09` | `UserCreateScreen.kt` | Submit enabled with invalid username → fold regex into `isFormValid` |
| `g3-10` | `UserCreateScreen.kt` | No confirm-password; blank silently accepted → require/confirm |
| `g3-11` | `UserCreateScreen.kt` | Success/failure invisible at the button → snackbar/toast + scroll to error |
| `g3-12` | `UserCreateScreen.kt` | Cancel discards 11-field form silently → dirty confirm/draft |
| `g3-17` | `UserDetailScreen.kt` | Save produces no visible outcome → observe `updateResult`, exit edit, toast |
| `g3-18` | `UserDetailScreen.kt` | Password/2FA sheet outcomes invisible (errors behind sheet) → report in-sheet |
| `g3-19` | `UserDetailScreen.kt` | Hardcoded status palette → theme tones (T2) |
| `g3-20` | `UserDetailScreen.kt` | Unlabelled security switches (Locked, Password Disabled) (T1) |
| `g3-24` | `LocalAdminSetupScreen.kt` | "Dismiss" only nulls the result, never `error` → wire to `clearError()` |
| `g3-25` | `LocalAdminSetupScreen.kt` | 1-char root password allowed, field unmasked → min length + strength + mask |
| `g3-30` | `ApiKeyListScreen.kt` | Empty state is literally `-> Unit` → proper empty/no-match state |
| `g3-31` | `ApiKeyListScreen.kt` | Unlabelled FAB is the only create entry → port UserList FAB semantics |
| `g3-37` | `ApiKeyCreateScreen.kt` | One-time reveal composed in a `Column`, not a real dialog → modal `Dialog` with back handling |
| `g3-38` | `ApiKeyCreateScreen.kt` | Blank name persists `"nobody"` but displays `"UNNAMED"` → make required or align |
| `g3-44` | `ApiKeyDetailsScreen.kt` | Expired key still shown green "Active" → tri-state from `expires_at` |
| `g3-49` | `CreditCardView.kt` | Fixed 340×210dp card → `fillMaxWidth().widthIn(max=340.dp)`, `heightIn` |
| `g3-50` | `CreditCardView.kt` | Key `maxLines = 1` clips most of a ~64-char secret → wrap/scroll |
| `g3-51` | `CreditCardView.kt` | ~36dp hand-rolled "Done" with no Role → `TextButton` (T12) |

### G4 — Alert services & audit

| ID | Screen | Change |
|---|---|---|
| `g4-01` | `AlertServicesListScreen.kt` | Failed load rendered as "No alert services configured" → error branch + Retry |
| `g4-02` | `AlertServicesListScreen.kt` | Hardcoded green text ≈3.3:1 → theme tones (T2) |
| `g4-03` | `AlertServicesListScreen.kt` | List stale after create/delete → reload on resume/nav-result |
| `g4-09` | `AlertServiceCreateScreen.kt` | Only name validated → per-provider required-field validation |
| `g4-10` | `AlertServiceCreateScreen.kt` | Invalid port/chat IDs silently dropped → validate and name the token |
| `g4-11` | `AlertServiceCreateScreen.kt` | 24 fields in `remember` → `rememberSaveable` (T9) |
| `g4-12` | `AlertServiceCreateScreen.kt` | `showSnackbar` before navigating delays 4 s → navigate then confirm |
| `g4-18` | `AlertServiceDetailScreen.kt` | Failed load → blank body → error branch + Retry |
| `g4-19` | `AlertServiceCreateScreen.kt` (Telegram IDs) | Comma parsed on every keystroke, second ID untypeable → local String state |
| `g4-20` | `AlertServiceDetailScreen.kt` | Delete one thumb-width from Edit → overflow/separate destructive action |
| `g4-25` | `AlertClassesConfigScreen.kt` | ~24dp expand target → 48dp, `expandable` semantics (T12/T13) |
| `g4-26` | `AlertClassesConfigScreen.kt` | WARNING `#F57C00` ≈2.7:1 → label in `onSurface`, hue on dot (T2) |
| `g4-27` | `AlertClassesConfigScreen.kt` | Blank body + enabled Save on load failure → error + disable Save |
| `g4-33` | `AuditConfigScreen.kt` | Blank body on load failure → error + Retry |
| `g4-34` | `AuditConfigScreen.kt` | Advertised ranges not enforced; invalid → silent `null` → `isError` + gate Save |
| `g4-40` | `AuditLogsScreen.kt` | Export spinner ≈1.0–1.4:1 → tint `contentColor` |
| `g4-41` | `AuditLogsScreen.kt` | Busy state = unlabelled button that does nothing → keep label + `stateDescription` |
| `g4-42` | `AuditLogsScreen.kt` | Stale rows shown under a new filter chip → list-level loading state |
| `g4-43` | `AuditLogsScreen.kt` | `selectedService` in screen state, refresh target from data → lift to ViewModel |
| `g4-44` | `AuditLogsScreen.kt` | Status icon labelled with username → `null` (T17) |

### G5 — Boot, network, cloud sync & services

| ID | Screen | Change |
|---|---|---|
| `g5-01` | `BootScreen.kt` | Load failures swallowed → record error + inline Retry |
| `g5-02` | `BootScreen.kt` | 28dp dismiss target → remove `size(28.dp)` (T12) |
| `g5-06` | `BootPoolScreen.kt` | "Replace Disk" free text pre-filled with first disk → choice field, no default |
| `g5-07` | `BootPoolScreen.kt` | N identical "Detach" buttons → include disk name |
| `g5-12` | `BootEnvironmentsScreen.kt` | Clone FAB bound to first environment → source picker |
| `g5-13` | `BootEnvironmentsScreen.kt` | 40dp icon buttons → 48dp (T12) |
| `g5-14` | `BootEnvironmentsScreen.kt` | Unlabelled "Keep" switch (T1) |
| `g5-15` | `BootEnvironmentsScreen.kt` | Action row has zero slack → overflow menu at large font |
| `g5-16` | `BootEnvironmentsScreen.kt` | "Clone"/"Destroy" repeat per row → include BE id |
| `g5-19` | `BootEnvironmentDetailScreen.kt` | Load failure shown as "not found" → distinguish error from absence |
| `g5-20` | `BootEnvironmentDetailScreen.kt` | Unlabelled switch (T1) |
| `g5-28` | `NetworkEditScreen.kt` | No dirty guard; back/Discard drop edits → confirm when dirty |
| `g5-29` | `NetworkEditScreen.kt` | Back not blocked during save → include `isSaving` |
| `g5-30` | `NetworkEditScreen.kt` | 8 fields in `remember` → `rememberSaveable` (T9) |
| `g5-34` | `CloudSyncScreen.kt` | Bottom window inset dropped; last row under nav bar/FAB → apply `innerPadding` + bottom padding |
| `g5-35` | `CloudSyncScreen.kt` | Hardcoded green ≈3.2–4.0:1 → `StatusTone` (T2) |
| `g5-39` | `CloudSyncFormScreen.kt` | Failed edit fetch → blank defaulted form that can overwrite → block behind error/Retry |
| `g5-40` | `CloudSyncFormScreen.kt` | Unlabelled Enabled switch (T1) |
| `g5-49` | `ServicesScreen.kt` | Unlabelled service switch → name + state (T1) |
| `g5-54` | `ServiceDetailScreen.kt` | Unlabelled "Start on boot" switch (T1) |

### G6 — App images, update, pools, shares & dataset

| ID | Screen | Change |
|---|---|---|
| `g6-05` | `DockerImageListScreen.kt` | `total = page.size` reported as library total → keep null / label "showing n" |
| `g6-06` | `DockerImageListScreen.kt` | Search only loaded pages → false "no match" → server query or load-all while searching |
| `g6-07` | `DockerImageListScreen.kt` | Image pull shows nothing → pending row/banner + progress + completion |
| `g6-08` | `DockerImageListScreen.kt` | Trash opens "Image Details" with unconfirmed destructive Delete → separate confirm dialog |
| `g6-18` | `SystemUpdateScreen.kt` | "Download & Restart Now" reboots with no confirmation → confirm + safe default |
| `g6-19` | `SystemUpdateScreen.kt` | Refresh fire-and-forget → false "up to date" → load/error state |
| `g6-25` | `PoolDetailsScreen.kt` | `!!` on nullable pool fields → `?.` (T10) |
| `g6-26` | `PoolDetailsScreen.kt` | Dead `isRefreshing`, no `refresh()` call, empty error branch → wire PTR + Retry |
| `g6-27` | `PoolDetailsScreen.kt` | Scrub callbacks/dialog unreachable → render controls or delete dead params |
| `g6-28` | `PoolDetailsScreen.kt` | Invalid threshold silently becomes 35 → `isError`, numeric keyboard, block confirm |
| `g6-29` | `PoolDetailsScreen.kt` | Failed scrub toasted as success → move toast into success branch |
| `g6-35` | `CreateShareScreen.kt` | Create always enabled; `normalizeSharePath("")` → `"/"` → gate on non-blank, fix normalizer |
| `g6-36` | `CreateShareScreen.kt` | Unlabelled switches (T1) |
| `g6-42` | `DatasetExplorerScreen.kt` | 24dp expand chevron, no ripple → 48dp target (T12) |
| `g6-43` | `DatasetExplorerScreen.kt` | Icon-only add-child button, no description → `IconButton` + label |
| `g6-44` | `DatasetExplorerScreen.kt` | Selection colour-only, no `selectable` → role + state (T13) |
| `g6-45` | `DatasetExplorerScreen.kt` | Back exits the screen instead of closing the details panel → `BackHandler` |
| `g6-54` | `DatasetPickerSheet.kt` | 20dp expand target → 48dp (T12) |
| `g6-55` | `DatasetPickerSheet.kt` | Multi-select has no semantics → `toggleable(Role.Checkbox)` (T13) |
| `g6-61` | `ServerPathPickerSheet.kt` | All errors → "Empty folder" → distinct error + Retry |
| `g6-62` | `ServerPathPickerSheet.kt` | Confirm path not verified before acting → show resolved path on the button |

## 4. P2 backlog (batched cleanup)

Grouped; all are consistency/polish/hygiene, none block a user.

- **Motion polish:** list item animations (`g4-08`), 0 ms content swaps (`g4-15`, `g4-24`,
  `g4-39`, `g6-39`), chart re-sweep (`g1-25`), reduce-motion handling (`g3-55`), main-nav
  transitions (`g5` cross-cutting note).
- **Semantics depth:** `collectionInfo`/`collectionItemInfo` on lists (`g1-18`, `g2-49`),
  `stateDescription` on status/selectors (`g1-04`, `g5-17`, `g6-42`), `progressBarRangeInfo`
  (`g5-52`), `liveRegion` on job progress (`g6-23`).
- **Contrast/theme:** alpha-reduced `onSurfaceVariant` text below 4.5:1 (`g1-19`, `g4-06`),
  chart axis greys (`g1-26`), credit-card secondary text (`g3-52`).
- **Adaptivity:** constrain widths (`g1-08`, `g6-41`), unify picker sizing (`g2-46`),
  bottom-sheet vs side-sheet at expanded widths (`g6-59`), `isTablet` magic breakpoint
  (`g6-47`).
- **Component consolidation (T18):** `ExpressiveEntryCard` (`g5-05`), `InfoRow` (`g1-16`,
  `g4-22`, `g5-10`), `PullToRefreshContent` (`g6-10`), FAB style (`g5-37`).
- **Copy & i18n (T19):** hardcoded strings (`g2-05`), plurals (`g4-07`, `g4-46`), unit labels
  (`g2-33`), error wording (`g2-15`, `g5-21`, `g5-24`), raw enum values (`g5-42`), raw
  seconds (`g6-12`), raw ISO timestamps (`g4-46`).
- **Placeholder honesty:** standardise the empty-value glyph and styling (`g5-24`), remove
  `"null..."` rendering (`g4-23`), stop truncating identity values (`g2-08`, `g3-47`,
  `g6-62`).
- **Navigation/hierarchy:** dead disk drill-down (`g1-06`), inert release-notes URL (`g2-14`),
  duplicate section/entry names (`g2-50`, `g5-23`), missing delete on detail (`g5-47`),
  unreachable scrub dialog (`g6-27`), inconsistent icons (`g5-08`).
- **Hygiene:** dead code/comments (`g1-24`, `g2-51`, `g2-35`, `g3-02`, `g4-30`), package/path
  mismatch (`g5-59`), duplicated scroll state (`g2-23`), `?.let` vs `!!` (`g6-25`),
  GlobalVirt overwrite while typing (`g2-45`), auto-copy secret without opt-in (`g3-39`),
  silent clipboard (`g3-39`).

## 5. Suggested sequencing

**Phase 1 — Safety (P0 + T15).** `g3-16`, `g3-43`, `g4-17`; add confirmations to Docker delete,
system update reboot, service Stop/Restart. *Rationale: these cause lockout/data loss.*

**Phase 2 — Shared-component fixes (one change, many screens).**
T1 toggle row, T4 heading semantics, T5 back button, T8 live-region error/toast, T2 status
tones, T3 motion tokens, T17 decorative icons. *Rationale: fixes the largest share of the 110
P1s with the fewest diffs, and lifts A2/A3 — the weakest dimensions.*

**Phase 3 — State honesty (T7/T16).** Introduce the `idle/loading/error/loaded` pattern and
outcome feedback across the screens listed in T7/T16. *Rationale: lifts H1/H3, the weakest HCI
dimensions.*

**Phase 4 — Validation & form safety (T9/T10 + validation P1s).**
`rememberSaveable`, dirty guards, field validation, remove `!!`. *Rationale: prevents data loss
and crashes.*

**Phase 5 — Adaptivity (T6) and i18n (T19).** Constrain widths, adopt
`currentWindowAdaptiveInfo()`, move copy to resources.

**Phase 6 — P2 backlog.** Batch by theme.

## 6. Verification

- **Per fix:** confirm with Accessibility Scanner (contrast, target size, labels).
- **Phase 2:** TalkBack pass over one screen per area — verify switches announce name + state,
  headings navigate, errors/toasts announce.
- **T12/T11:** 360dp device at font scale 200% — the two measurement-dependent P1s
  (`g5-15` row overflow, `g5-34` FAB/inset overlap) must be visually confirmed.
- **T6:** resizable emulator at compact / medium / expanded; confirm no stretch and state is
  preserved across resize.
- **T1/T13:** Compose UI tests asserting the semantics tree (name, role, state) for
  `ExpressiveToggleRow`, `ExpressiveEntryCard`, and the dataset picker — the repo currently has
  no UI tests, so this is the highest-value test investment.
- Re-run the scorecard after Phases 1–3 to measure the delta; target H3 ≥ 3.5 and A3 ≥ 3.5.
