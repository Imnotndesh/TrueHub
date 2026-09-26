# Services Module — Recommended Changes (HCI & Accessibility)

Companion to `services-audit-scores.md`. This is the action plan to follow. Standards:
`docs/design/hci-android-guidelines.md` and `docs/design/m3-expressive-research.md` (§8–§10).

Finding IDs (`s<group>-<n>`) map to the raw reports in `docs/design/services-audit/s1.md` … `s7.md`.

## Priority model

| Level | Meaning | Action |
|---|---|---|
| **P0** | Blocks a user group, security, or data loss | Fix before the next release |
| **P1** | Significant usability/accessibility degradation | Fix in the hardening pass |
| **P2** | Polish, consistency, code hygiene | Batch into a cleanup pass |

## 1. P0 — fix immediately

### P0-1 · `s6-31` — every TLS error is silently accepted (security)
`vm/details/VmConsoleScreen.kt:78-87` overrides `onReceivedSslError { handler?.proceed() }`
unconditionally on a WebView with `javaScriptEnabled` + `domStorageEnabled`. Self-signed,
expired, wrong-host and attacker-substituted certificates are all accepted with no prompt,
log or indication. This removes the user's trust decision and lets a network attacker serve
script into the app.

**Change:** render the certificate as an error state ("Proceed once / Cancel") and require an
explicit decision; if a pinned/known-good host must auto-proceed, scope it to that host only.

### P0-2 · `s6-01` — large-window users lose the entire VM detail screen
`vm/VmsScreen.kt:580` routes the card tap **and** "View Info" to the side pane whenever
`isCompact == false` (which also happens on a landscape phone). The pane
(`VmInfoPane`, `VmsScreen.kt:441`) has no `topContent`/`bottomActions`, so `VmInfoScreen` —
the only place with **Devices, Edit config, Open console, Clone, Export, Delete**
(`VmInfoScreen.kt:230-270`) — becomes unreachable from the list. Only the search overlay
still reaches it.

**Change:** either pass the primary actions + entry cards + danger zone into the pane, or add
an "Open full details" affordance. Same defect exists in containers (`s4-03`, `s4-26`).

### P0-3 · `s7-23` — editing a device can re-save it as the wrong type
`vm/devices/VmDeviceAddScreen.kt:69` defaults `type` to `Disk`; the edit effect
(`:96-120`) branches on `device.dtype` but **never restores `type`**, so a NIC/CD-ROM/PCI/display
device opens in the Disk form under the title "Edit device / Disk" and Save sends
`dtype = DISK` with Disk-branch attributes (`:159`, `:490-537`). Two related round-trip losses:
`nicType` resets to "E1000", and `createZvol` is forced `true` for an existing `path`,
hiding it and sending a default 10 GB zvol.

**Change:** derive `type` from `device.dtype`, restore every field from the loaded device,
disable the type selector while editing.

## 2. Systemic themes (highest leverage)

### T1 · Inert shared picker field (`ExpressiveChoiceField`) — ~15 sites
`components/expressive/ExpressiveForm.kt:73-96` is a `readOnly` text field whose only handler
is the trailing `IconButton`. Tapping the field body (the largest target) does nothing, and
TalkBack announces an editable text field rather than a picker.

*IDs:* `s4-19`, `s4-40`, `s5-03`, `s7-03`; choice **sheets** also fail to expose selection
(`s5-11`, `s6-21`, `s7-12`).
**Fix (once):** make the field surface `clickable` with `role = Role.Button` +
`stateDescription = value` (or use `ExposedDropdownMenuBox`); mark sheet rows
`selectable(selected, role = Role.RadioButton)` and give sheets a `paneTitle`. Every picker
(Image, Storage pool, I/O bus, Disk source, NIC, GPU, USB, PCI, TPM path, CPU mode, NIC type,
timezone) improves at once.

### T2 · Unlabelled switches/checkboxes — 5 screens
A `Switch`/`Checkbox` sits in a plain `Row` next to a sibling `Text`, so TalkBack announces
"Switch, off" for security/data decisions.

*IDs:* `s3-12` (Rollback snapshot), `s3-26` (Take snapshot before upgrade), `s6-06` (Delete
Zvols / Force delete), `s6-18` (Overcommit / Force shutdown / Force delete), `s2-06` (app
removal options).
**Fix:** use the existing correct pattern — `Row(Modifier.toggleable(value, role = Role.Switch/Checkbox))`
with `Switch(onCheckedChange = null)` (see `ExpressiveToggleRow`).

### T3 · Status conveyed by an unlabelled colour dot — grid/non-compact layouts
In grid mode the container/VM/service state is a 12dp `ExpressiveStatusDot`
(`ExpressiveStatus.kt:85-99`) with no text and no semantics node, while the compact branch
has a labelled chip.

*IDs:* `s1-02`, `s4-01`, `s6-07`.
**Fix:** in the shared dot add `Modifier.semantics { stateDescription = … }` and use the
labelled `ExpressiveStatusChip` (or a status icon) in grid mode too.

### T4 · Destructive actions unguarded or mis-described
*IDs:* `s1-23` (uninstall one tap, unlike the hub's dialog), `s3-16` (rollback discards data
since the snapshot, no confirm), `s6-03` + `s6-17` (hard **Power Off** one tap while Stop is
confirmed), `s6-04` / `s4-02` (dialog names a count/set that is not what is acted on).
**Fix:** confirm with the target + consequence named; make the confirmation set match the
action (compute the deletable subset first); demote Cancel and label the primary
"Rollback to <version>" etc.

### T5 · Silent failures and stale data (H1/H3 driver)
Operation/refresh failures are dropped or hidden, so the UI can show success or stale data
when nothing happened.

*IDs:* `s1-18`, `s1-24`, `s1-25`, `s2-01`, `s2-19`, `s2-20`, `s2-31`, `s3-15`, `s3-28`,
`s4-13`, `s4-14`, `s4-16`, `s4-21`, `s4-36`, `s5-13`, `s5-15`, `s6-05`, `s6-16`, `s6-32`,
`s7-14`, `s7-16`, `s7-25`.
**Fix:** route every `ApiResult.Error` / `JobRepository` FAILED/ABORTED state to a visible
`ErrorState` (reuse `components/ErrorState.kt`) or toast; surface stale state with a
"Couldn't refresh — showing last known state" banner; never claim success for an unconfirmed
result.

### T6 · Progress/status not announced (A3)
*IDs:* `s1-36`, `s2-07`, `s3-14`, `s3-27`, `s4-07`, `s4-20`, `s5-10`, `s6-25`, `s6-34`.
**Fix:** add `liveRegion = LiveRegionMode.Polite` (Assertive for failures/overlays) to
`components/expressive/JobProgressView.kt` and status rows, `progressBarRangeInfo` for
progress, and `paneTitle` on overlays; announce terminal states before auto-navigation
(`s3-14`, `s3-27` also auto-pop the screen).

### T7 · No max content width and no window-size-class use (H5)
*IDs:* `s1-08`, `s1-19`, `s1-31`, `s1-41`, `s2-29`, `s2-34`, `s3-09`, `s3-21`, `s3-33`,
`s4-18`, `s4-25`, `s4-35`, `s5-08`, `s5-20`, `s5-30`, `s5-39`, `s6-02`, `s6-23`, `s7-05`,
`s7-22`, `s7-26`.
**Fix:** constrain content (`widthIn(max = 640–840.dp)`) and adopt
`currentWindowAdaptiveInfo()`; branch grid columns on the **slot** the list actually gets,
not the window (`s6-02`). Migration path in `m3-expressive-research.md` §9.2.

### T8 · Pane/screen parity gaps on large windows (H2)
*IDs:* `s2-12` (pane loses Start/Stop/Edit/Uninstall), `s4-03`, `s4-26`, `s6-01` (P0),
`s6-30` (pane dead end).
**Fix:** give panes the same primary actions via `topContent`/`bottomActions`, or an explicit
"Open full details".

### T9 · Soft-keyboard / navigation-bar insets ignored (H5)
`enableEdgeToEdge()` is on and there is **no** `imePadding`/`WindowInsets.ime` anywhere in the
app, so the keyboard covers bottom bars and primary actions.

*IDs:* `s4-31`, `s6-35`, `s7-01`, `s7-26`.
**Fix:** add `Modifier.imePadding()` on forms/console and `navigationBarsPadding()` on bottom
bars; verify on device with the keyboard open.

### T10 · Fixed heights clip at large font scale (A5)
*IDs:* `s1-13`, `s1-15`, `s2-14`, `s2-24`, `s3-22`, `s3-34`, `s4-24`, `s5-07`, `s5-29`,
`s6-24`, `s7-09`.
**Fix:** `heightIn(min = …)`; allow labels to wrap / rows to stack at large `fontScale`.

### T11 · Motion bypasses the theme (H6)
*IDs:* `s1-07`, `s1-39`, `s2-33`, `s3-20`, `s3-32`, `s4-38`, `s5-09`, `s7-31`; shared
`components/expressive/JobProgressView.kt:44-50`.
**Fix:** replace hardcoded `spring(...)`/`tween(...)` with `MaterialTheme.motionScheme.*Spec()`;
wrap 0 ms state swaps in `AnimatedContent`/`AnimatedVisibility`.

### T12 · Hardcoded colours / low-contrast surfaces (A4)
*IDs:* `s1-11` + `s1-17` (marketplace greens/whites), `s2-16` + `s2-37` (app status green),
`s6-15` (OLED-black light-mode green), `s4-09` + `s7-13` (cards use `surface` on `background`
→ ~1.1:1 containment), `s1-32` + `s3-30` + `s1-29` + `s3-02` (alpha-reduced text/axis below
4.5:1).
**Fix:** route status through `StatusTone`/`ExpressiveStatusChip`; use `surfaceContainerLow`
for cards; drop alpha on small text and chart axes.

### T13 · Missing `paneTitle` on sheets/dialogs (A3)
*IDs:* `s2-06`, `s5-22`, `s5-40`, `s6-21`, `s7-12`.
**Fix:** `Modifier.semantics { paneTitle = … }` on every sheet/dialog (pattern already in
`SecretRevealDialog.kt:51`).

### T14 · Duplicate/identical accessibility labels (A2)
*IDs:* `s1-27` (all screenshots "App Screenshot"), `s2-18` (identical "Open URL"),
`s5-14` + `s5-33` + `s7-19` (identical "Remove device"/"Edit/Delete volume" per row).
Decorative chevrons repeating the title: `s5-21`, `s6-19`, `s7-20`.
**Fix:** make descriptions unique/per-row and set decorative glyphs to `null`.

### T15 · Selection state not exposed (A3)
*IDs:* `s1-03` (same check glyph both states), `s3-13`, `s4-12`, `s5-11`, `s6-11`.
**Fix:** `selectable(selected, role = Role.RadioButton/Checkbox)` or
`semantics { selected = … }`; use distinct checked/unchecked glyphs.

### T16 · Cross-server draft corruption — data integrity (P1)
`s2-22` `AppConfigEditCache` is process-global and keyed by **app name only**; switching
server/account in-process restores a draft typed on another server and can submit it there.
**Fix:** key by `(serverId, accountId, appName)` and clear on session/profile change; show
"Restored unsaved changes".

### T17 · Progress-indicator churn on silent polls (H1/H6)
*IDs:* `s4-08` (header grows ~20dp every 30 s), `s6-05`.
**Fix:** drive `isRefreshing` only for user-initiated refresh, or overlay the indicator without
changing header height.

### T18 · Dead / duplicated code (H4)
*IDs:* `s1-42` (`InstallHeroHeader` unused), `s2-08` (~620 lines of duplicate renderers),
`s2-13` (no-op chips), `s2-36` (`AdvancedPortalCard` unused), `s3-24` (~130 lines unreachable
orbit code).
**Fix:** delete; keep one shared renderer per data shape.

## 3. Per-area P1 register

### S1 — Apps hub & marketplace

| ID | Screen | Change |
|---|---|---|
| `s1-01` | `AppsScreen.kt` | Empty state only renders on error; branch on `filteredApps.isEmpty()` + CTA |
| `s1-02` | `AppsScreen.kt` | Grid status is a colour-only dot → label/`stateDescription` (T3) |
| `s1-03` | `AppsScreen.kt` | Check glyph identical selected/unselected → distinct icons + role (T15) |
| `s1-12` | `MarketplaceScreen.kt` | Featured pager auto-advances forever → pause on interaction + reduced-motion guard |
| `s1-13` | `MarketplaceScreen.kt` | Install button `height(38.dp)` → ≥48dp (T10) |
| `s1-22` | `MarketplaceAppDetailsScreen.kt` | `healthy ?: true` reports unknown as healthy → tri-state |
| `s1-23` | `MarketplaceAppDetailsScreen.kt` | Uninstall one tap → confirm (T4) |
| `s1-24` | `MarketplaceAppDetailsScreen.kt` | Uninstall success callback is a no-op → refresh/pop + confirm |
| `s1-25` | `MarketplaceAppDetailsScreen.kt` | Uninstall failure swallowed → surface `ApiResult.Error`/FAILED job |
| `s1-32` | `MarketplaceAppInstallScreen.kt` | `onSurfaceVariant` @0.5 alpha ≈2.5:1 → full opacity (T12) |
| `s1-33` | `MarketplaceAppInstallScreen.kt` | Disabled-textfield picker → exposed dropdown/role |
| `s1-34` | `MarketplaceAppInstallScreen.kt` | Fields unlabelled → `label=`/contentDescription; bind switch |
| `s1-35` | `MarketplaceAppInstallScreen.kt` | Deploy always enabled, no min/max validation → validate + gate |
| `s1-36` | `MarketplaceAppInstallScreen.kt` | Failure overlay not announced / form still focusable → liveRegion + clear semantics |
| `s1-37` | `MarketplaceAppInstallScreen.kt` | Wizard state `remember`-only → ViewModel/saver (T9-class) |

### S2 — App details core

| ID | Screen | Change |
|---|---|---|
| `s2-01` | `AppInfoScreen.kt` | Uninstall failure is haptic-only → visible error + retry (T5) |
| `s2-02` | `AppInfoScreen.kt` | `app.metadata!!` crash on Edit → `?.` |
| `s2-11` | `AppInfoPane.kt` | Raw HTML markup shown → shared description renderer |
| `s2-12` | `AppInfoPane.kt` | Pane loses Start/Stop/Edit/Uninstall → parity (T8) |
| `s2-19` | `AppConfigScreen.kt` | Job FAILED/ABORTED leaves overlay forever → clear + failure panel |
| `s2-20` | `AppConfigScreen.kt` | Load error still renders defaulted form + enabled Save → block + Retry |
| `s2-21` | `AppConfigScreen.kt` | Field labels not associated → pass `label=` in shared renderer |
| `s2-22` | `AppConfigScreen.kt` | Draft cache keyed by app name only → `(server, account, app)` (T16) |
| `s2-31` | `AppAdvancedInfoScreen.kt` | Dead-end error, no Retry → body error state + Retry |

### S3 — App resource usage, rollback & upgrade

| ID | Screen | Change |
|---|---|---|
| `s3-01` | `AppResourceUsageScreen.kt` | Charts have no semantics → summary contentDescription (T6) |
| `s3-02` | `AppResourceUsageScreen.kt` | Axis text ≈2.9:1 → full-opacity (T12) |
| `s3-03` | `AppResourceUsageScreen.kt` | No liveness/stale signal; header flags hardcoded → status chip from `isLive` |
| `s3-12` | `RollbackVersionScreen.kt` | Snapshot switch unlabelled → toggleable (T2) |
| `s3-13` | `RollbackVersionScreen.kt` | Version selection not in semantics → `selectable` (T15) |
| `s3-14` | `RollbackVersionScreen.kt` | Outcome not announced + auto-pop → liveRegion + user-confirmed exit (T6) |
| `s3-15` | `RollbackVersionScreen.kt` | Dismiss is a dead control → real callback or remove |
| `s3-16` | `RollbackVersionScreen.kt` | Rollback executes with no confirm → confirm + consequence (T4) |
| `s3-17` | `RollbackVersionScreen.kt` | 400dp illustration hides status/recovery on short windows → size from height + scroll |
| `s3-25` | `UpgradeSummaryScreen.kt` | Shows internal version id → use `human_version` |
| `s3-26` | `UpgradeSummaryScreen.kt` | Snapshot checkbox unlabelled → toggleable (T2) |
| `s3-27` | `UpgradeSummaryScreen.kt` | Outcome not announced + auto-pop → liveRegion + confirmed exit (T6) |

### S4 — Containers core

| ID | Screen | Change |
|---|---|---|
| `s4-01` | `ContainersScreen.kt` | Grid status dot colour-only → label/semantics (T3) |
| `s4-02` | `ContainersScreen.kt` | Delete dialog promises all selected, deletes STOPPED subset → state the real set (T4) |
| `s4-03` | `ContainersScreen.kt` | Pane makes `ContainerInfoScreen` unreachable → parity (T8, P0-2 analogue) |
| `s4-14` | `ContainerInfoScreen.kt` | Operation failures invisible once loaded → surface `state.error` (T5) |
| `s4-15` | `ContainerInfoScreen.kt` | Late delete callback pops an extra destination → guard on lifecycle |
| `s4-29` | `ContainerCreateScreen.kt` | Cancel/back discards form → dirty confirm/draft |
| `s4-30` | `ContainerCreateScreen.kt` | "Dismiss" fires a reload → wire `dismissError()` |
| `s4-31` | `ContainerCreateScreen.kt` | IME covers bottom bar → `imePadding()` (T9) |

### S5 — Container edit, devices & volumes

| ID | Screen | Change |
|---|---|---|
| `s5-01` | `ContainerEditScreen.kt` | VNC password in clear + saved in state → secret field support (T1-adjacent, shared) |
| `s5-02` | `ContainerEditScreen.kt` | VNC port silently null/out-of-range → validate 5900–65535 + numeric keyboard |
| `s5-03` | `ContainerEditScreen.kt` | Inert choice field → shared fix (T1) |
| `s5-12` | `ContainerDevicesScreen.kt` | Upload locks user in sheet with no escape → cancellable/backgroundable |
| `s5-13` | `ContainerDevicesScreen.kt` | Prepare failure = silent no-op → show error |
| `s5-14` | `ContainerDevicesScreen.kt` | Identical "Remove device" per row → include name (T14) |
| `s5-23` | `ContainerDeviceAddScreen.kt` | Add always enabled, empty payloads → validate/gate |
| `s5-24` | `ContainerDeviceAddScreen.kt` | Port `?: 0` → validate 1–65535 |
| `s5-25` | `ContainerDeviceAddScreen.kt` | Stale GPU id after type change → clear + reload |
| `s5-33` | `ContainerVolumesScreen.kt` | Identical "Edit/Delete volume" → include name (T14) |
| `s5-34` | `ContainerVolumesScreen.kt` | Import ISO without pool while Create requires it → require/preselect pool |

### S6 — VM core

| ID | Screen | Change |
|---|---|---|
| `s6-02` | `VmsScreen.kt` | Grid columns from window while list is 55% → branch on slot (T7) |
| `s6-03` | `VmsScreen.kt` | Power Off one tap → confirm (T4) |
| `s6-04` | `VmsScreen.kt` | Delete confirmation count ≠ deleted set → state real set (T4) |
| `s6-05` | `VmsScreen.kt` | Swallowed refresh error + 30s poll → stale banner/last-updated (T5/T17) |
| `s6-06` | `VmsScreen.kt` | Unlabelled delete switches → toggleable (T2) |
| `s6-07` | `VmsScreen.kt` | Grid status dot colour-only → label/semantics (T3) |
| `s6-16` | `VmInfoScreen.kt` | All primary-action failures dropped → surface `state.error` (T5) |
| `s6-17` | `VmInfoScreen.kt` | Power Off one tap → confirm (T4) |
| `s6-18` | `VmInfoScreen.kt` | Unlabelled switches in dialogs → toggleable (T2) |
| `s6-25` | `VmInfoPane.kt` | Status/progress not announced → liveRegion (T6) |
| `s6-31` | `VmConsoleScreen.kt` | **P0** TLS auto-accept (P0-1) |
| `s6-32` | `VmConsoleScreen.kt` | No error/loading state for failed page → page callbacks + Retry |
| `s6-33` | `VmConsoleScreen.kt` | "Retry" cannot succeed → state-specific action ("Start VM") |
| `s6-34` | `VmConsoleScreen.kt` | Console region unlabelled → description + liveRegion (T6) |
| `s6-35` | `VmConsoleScreen.kt` | IME covers console → `imePadding()` (T9) |

### S7 — VM form & devices

| ID | Screen | Change |
|---|---|---|
| `s7-01` | `VmFormScreen.kt` | IME covers bottom bar → `imePadding()` (T9) |
| `s7-02` | `VmFormScreen.kt` | Cancel/back discards ~30 fields → dirty confirm |
| `s7-03` | `VmFormScreen.kt` | 11 inert choice fields → shared fix (T1) |
| `s7-04` | `VmFormScreen.kt` | Numeric fields silent drops → numeric keyboard + validation |
| `s7-14` | `VmDevicesScreen.kt` | Import/convert no feedback; `isImporting` unused → JobProgressView + disable |
| `s7-15` | `VmDevicesScreen.kt` | FAB overlaps last row's action → bottom padding ≈88dp |
| `s7-16` | `VmDevicesScreen.kt` | Stale list after add/edit → refresh on return |
| `s7-24` | `VmDeviceAddScreen.kt` | Add enabled with blank required fields → validate/gate |
| `s7-25` | `VmDeviceAddScreen.kt` | Invalid numerics silently dropped → numeric keyboard + inline error |
| `s7-26` | `VmDeviceAddScreen.kt` | IME/insets + no max width → `imePadding()` + constrain (T9/T7) |

## 4. P2 backlog (batched cleanup)

- **Accessibility depth:** headings missing on some native section titles
  (`s2-15`, `s3-23`, `s3-31`); `mergeDescendants` for comparison rows (`s3-31`); screenshot
  `collectionInfo` (`s2-32`); chip `Role` (`s1-16`, `s2-13`).
- **Contrast/theme:** chart axis/gauge labels (`s1-29`, `s3-02`), disabled tints
  (`s3-30`, `s5-35`), card-vs-background separation (`s4-09`, `s7-13`), status-tone in
  OLED/monochrome (`s6-15`).
- **Adaptivity:** constrain widths and add columns on expanded (`s2-34`, `s3-09`, `s3-21`,
  `s3-33`, `s4-18`, `s4-25`, `s5-20`, `s5-39`, `s6-23`, `s7-22`); split-pane height gating
  (`s4-11`).
- **Motion tokens:** remaining sites (`s1-07`, `s1-39`, `s2-33`, `s3-20`, `s3-32`, `s4-38`,
  `s5-09`, `s7-31`); 0 ms swaps (`s6-10`, `s6-22`, `s6-27`, `s7-10`).
- **Reduced-motion / autoplay:** pager (`s1-12`), orbit loops (`s1-39`, `s3-24`).
- **Copy & consistency:** raw wire codes vs friendly labels (`s5-27`, `s7-18`), unit/format
  consistency (`s6-14`, `s5-06`), selection count copy (`s6-09`), empty-vs-error contradiction
  (`s6-08`), grammatically wrong counts (`s4-02`, `s6-04`).
- **State persistence:** `remember` → `rememberSaveable` (`s1-10`, `s4-37`, `s5-16`, `s7-21`).
- **`paneTitle`/sheet semantics:** `s5-22`, `s5-40`, `s6-21`, `s7-12`.
- **Dead/duplicate code:** `s1-42`, `s2-08`, `s2-13`, `s2-36`, `s3-24`.
- **Inert/no-op affordances:** `s1-16`, `s2-13`, `s1-06`, `s3-11` (dead chart touch), `s2-25`
  (overlay doesn't block input), `s2-26` (no dirty state).
- **Refresh churn:** `s4-08`, `s6-05`.

## 5. Suggested sequencing

**Phase A — Safety.** P0-1 (TLS), P0-3 (device type), P0-2 (pane parity), T4
(destructive confirmations), T16 (cross-server draft). *Rationale: security + data loss.*

**Phase B — Shared-component fixes (one change, many screens).** T1 (choice field + sheets),
T2 (toggleable), T3 (status dot), T6 (JobProgressView/status liveRegion), T12 (status tone +
card surface), T13 (`paneTitle`). *Rationale: fixes the largest share of the 80 P1s and lifts
A2/A3 — the weakest dimensions — with the fewest diffs.*

**Phase C — State honesty.** T5 (surface every failure/stale state), T17 (no poll churn),
T14 (unique labels), T15 (selection semantics). *Rationale: lifts H1/H3.*

**Phase D — Adaptive & inset correctness.** T7 (max width + size class), T8 (pane parity),
T9 (IME/nav insets), T10 (fixed heights). *Rationale: large-screen/large-font failures.*

**Phase E — Motion & hygiene.** T11 (motion tokens), T18 (dead code), P2 backlog.

## 6. Verification

- **Per fix:** Accessibility Scanner (contrast, target size, labels).
- **T1/T2/T3/T6:** TalkBack pass over one screen per area — pickers announce value, switches
  announce name+state, progress announced.
- **T9:** device with the keyboard open — primary actions reachable.
- **T7/T10:** 360dp @ fontScale 200% and an expanded-window emulator — no clipping/stretch.
- **T8:** landscape phone and tablet — the full detail screen (console/devices/delete) reachable.
- **P0-1:** verify a bad certificate is rejected/confirmed.
- **T16:** switch server mid-draft and confirm the draft does not follow.
- Re-run the scorecard after Phases A–C; target H3 ≥ 3.5 and A3 ≥ 3.5.
