# Services Module — HCI & Accessibility Scorecard

Audit of the TrueHub **services module** (`ui/services/**` — apps, containers, VMs) against
`docs/design/hci-android-guidelines.md` and `docs/design/m3-expressive-research.md` (§8–§10).

Companion document: `services-audit-fixes.md` (the change plan to follow). Raw per-group
findings: `docs/design/services-audit/s1.md` … `s7.md`.

## Scope & method

- **26 screens/sheets** audited, split into 7 areas.
- Each screen scored **0–5** (5 = excellent) on 6 HCI dimensions and 5 accessibility
  dimensions, with `file:line` evidence for every finding.
- Severity: **P0** blocks a user group / security / data loss; **P1** significant
  usability/accessibility degradation; **P2** polish/consistency.
- Verified by reading source. Contrast ratios were **computed** from the actual theme token
  values, not measured on device. No builds or tests were run.

### Score dimensions

| Code | HCI dimension | Code | Accessibility dimension |
|------|---------------|------|-------------------------|
| H1 | Feedback & system status | A1 | Touch targets (≥48dp) |
| H2 | Navigation & mental model | A2 | Labels & roles |
| H3 | Error prevention & recovery | A3 | Semantics (headings, live regions, state) |
| H4 | Cognitive load & hierarchy | A4 | Contrast & colour independence |
| H5 | Layout, readability & adaptive | A5 | Text scaling & focus/traversal |
| H6 | Motion & M3 Expressive adherence | | |

## Headline result

| | Score |
|---|---|
| **HCI average** | **3.0 / 5** |
| **Accessibility average** | **3.4 / 5** |
| **Findings** | **3 P0 · 80 P1 · 179 P2** |

The module is structurally sound (navigation, hierarchy and touch sizing are its strengths),
but has three blocking defects and two recurring weaknesses: **error prevention/recovery is
the lowest HCI dimension (H3 = 2.5)** and **screen-reader semantics is the lowest
accessibility dimension (A3 = 2.6)**. Both are driven by a small number of repeated patterns
(inert picker fields, unlabelled switches, silent failures) rather than by many unique bugs.

### Dimension rollup (average across all 26 screens)

| Dimension | Avg | Read |
|---|---|---|
| H1 Feedback & status | 3.2 | Silent refresh/operation failures recur |
| H2 Navigation & mental model | 3.2 | Pane/screen parity gaps on large windows |
| H3 Error prevention & recovery | **2.5** | Weakest — invalid input accepted, destructive actions unguarded |
| H4 Cognitive load & hierarchy | 3.4 | Strong |
| H5 Layout, readability & adaptive | 2.8 | No max content width; inset/IME gaps |
| H6 Motion & M3 Expressive | 3.1 | Theme tokens used in some files, hardcoded in others |
| A1 Touch targets | **4.4** | Strongest |
| A2 Labels & roles | 3.0 | Inert choice fields, unlabelled switches |
| A3 Semantics | **2.6** | Weakest overall |
| A4 Contrast & colour independence | 3.5 | Status dots with no label/text |
| A5 Text scaling & focus | 3.4 | Fixed heights still clip |

### Group rollup

| Group | Screens | HCI avg | A11y avg | P0 | P1 | P2 |
|---|---|---:|---:|---:|---:|---:|
| S1 — Apps hub & marketplace | 4 | 3.1 | 3.2 | 0 | 15 | 28 |
| S2 — App details core | 4 | 3.0 | 3.2 | 0 | 9 | 28 |
| S3 — App resource usage, rollback & upgrade | 3 | 3.1 | 3.2 | 0 | 12 | 22 |
| S4 — Containers core | 4 | 3.6 | 3.6 | 0 | 8 | 32 |
| S5 — Container edit, devices & volumes | 4 | 3.3 | 3.6 | 0 | 11 | 29 |
| S6 — VM core | 4 | **2.6** | 3.2 | **2** | 15 | 20 |
| S7 — VM form & devices | 3 | **2.5** | **3.7** | **1** | 10 | 20 |
| **Total** | **26** | **3.0** | **3.4** | **3** | **80** | **179** |

## Per-screen scores

Paths are relative to `app/src/main/java/com/imnotndesh/truehub/ui/services/`.
`Findings` = number of rows in that screen's finding table.

### S1 — Apps hub & marketplace

| Screen | H1 | H2 | H3 | H4 | H5 | H6 | HCI | A1 | A2 | A3 | A4 | A5 | A11y | Findings |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| `apps/AppsScreen.kt` | 4 | 4 | 4 | 4 | 4 | 4 | **4.0** | 5 | 3 | 3 | 3 | 4 | 3.6 | 11 |
| `apps/details/marketplace/MarketplaceScreen.kt` | 2.5 | 3 | 3 | 3 | 2.5 | 2.5 | 2.8 | 2 | 3 | 2 | 3 | 3 | **2.6** | 10 |
| `apps/details/marketplace/MarketplaceAppDetailsScreen.kt` | 3 | 3 | 2 | 4 | 3 | 2.5 | 2.9 | 5 | 3 | 2.5 | 4 | 4 | 3.7 | 10 |
| `apps/details/marketplace/MarketplaceAppInstallScreen.kt` | 4 | 3 | 2 | 3 | 1.5 | 2.5 | 2.7 | 5 | 2 | 2 | 2 | 3 | 2.8 | 11 |

### S2 — App details core

| Screen | H1 | H2 | H3 | H4 | H5 | H6 | HCI | A1 | A2 | A3 | A4 | A5 | A11y | Findings |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| `apps/details/appdetails/AppInfoScreen.kt` | 3 | 4 | 3 | 4 | 3 | 3 | 3.3 | 4 | 3 | 3 | 4 | 3 | 3.4 | 10 |
| `apps/details/appdetails/AppInfoPane.kt` | 3 | 2 | 3 | 3 | 3 | 3 | 2.8 | 3 | 3 | 3 | 3 | 3 | 3.0 | 8 |
| `apps/details/appdetails/AppConfigScreen.kt` | 3 | 3 | 2 | 3 | 3 | 3 | 2.8 | 4 | 2 | 2 | 4 | 3 | 3.0 | 12 |
| `apps/details/appdetails/AppAdvancedInfoScreen.kt` | 3 | 4 | 2 | 3 | 3 | 3 | 3.0 | 4 | 3 | 3 | 3 | 4 | 3.4 | 7 |

### S3 — App resource usage, rollback & upgrade

| Screen | H1 | H2 | H3 | H4 | H5 | H6 | HCI | A1 | A2 | A3 | A4 | A5 | A11y | Findings |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| `apps/details/appdetails/AppResourceUsageScreen.kt` | 3 | 3 | 3 | 4 | 3 | 2.5 | 3.1 | 4.5 | 3.5 | 2 | 3 | 3 | 3.2 | 11 |
| `apps/details/rollback/RollbackVersionScreen.kt` | 3.5 | 3.5 | 2 | 3.5 | 2 | 3 | 2.9 | 4.5 | 2.5 | 2 | 4 | 3.5 | 3.3 | 13 |
| `apps/details/upgrade/UpgradeSummaryScreen.kt` | 3.5 | 2.5 | 4 | 4 | 3 | 2.5 | 3.3 | 4 | 2.5 | 2.5 | 3 | 3.5 | 3.1 | 10 |

### S4 — Containers core

| Screen | H1 | H2 | H3 | H4 | H5 | H6 | HCI | A1 | A2 | A3 | A4 | A5 | A11y | Findings |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| `containers/ContainersScreen.kt` | 4.0 | 3.0 | 3.0 | 4.0 | 3.5 | 4.0 | 3.6 | 4.0 | 3.5 | 2.5 | 2.5 | 4.0 | 3.3 | 13 |
| `containers/details/ContainerInfoScreen.kt` | 3.0 | 4.0 | 4.0 | 4.0 | 3.5 | 3.5 | 3.7 | 4.5 | 4.0 | 3.0 | 3.5 | 4.0 | 3.8 | 10 |
| `containers/details/ContainerInfoPane.kt` | 3.0 | 3.0 | 4.0 | 4.5 | 3.5 | 4.0 | 3.7 | 5.0 | 4.5 | 3.0 | 4.0 | 3.5 | **4.0** | 5 |
| `containers/create/ContainerCreateScreen.kt` | 4.0 | 3.5 | 2.5 | 4.5 | 2.5 | 3.0 | 3.3 | 3.5 | 3.0 | 3.0 | 4.5 | 3.0 | 3.4 | 12 |

### S5 — Container edit, devices & volumes

| Screen | H1 | H2 | H3 | H4 | H5 | H6 | HCI | A1 | A2 | A3 | A4 | A5 | A11y | Findings |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| `containers/edit/ContainerEditScreen.kt` | 4 | 4 | 2 | 4 | 3 | 3 | 3.3 | 4 | 3 | 3 | 4 | 3 | 3.4 | 11 |
| `containers/devices/ContainerDevicesScreen.kt` | 3 | 4 | 3 | 3 | 3 | 4 | 3.3 | 5 | 3 | 3 | 4 | 4 | 3.8 | 11 |
| `containers/devices/ContainerDeviceAddScreen.kt` | 4 | 3 | 2 | 3 | 3 | 4 | 3.2 | 5 | 3 | 3 | 4 | 3 | 3.6 | 9 |
| `containers/volumes/ContainerVolumesScreen.kt` | 4 | 3 | 3 | 3 | 3 | 4 | 3.3 | 5 | 3 | 3 | 3 | 3 | 3.4 | 9 |

### S6 — VM core

| Screen | H1 | H2 | H3 | H4 | H5 | H6 | HCI | A1 | A2 | A3 | A4 | A5 | A11y | Findings |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| `vm/VmsScreen.kt` | 3 | 3 | 2 | 3 | 2 | 4 | 2.8 | 4 | 3 | 2 | 2 | 3 | 2.8 | 15 |
| `vm/details/VmInfoScreen.kt` | 2 | 3 | 2 | 3 | 3 | 3 | 2.7 | 5 | 2 | 2 | 4 | 4 | 3.4 | 8 |
| `vm/details/VmInfoPane.kt` | 3 | 3 | 3 | 3 | 3 | 3 | 3.0 | 5 | 4 | 3 | 4 | 3 | 3.8 | 7 |
| `vm/details/VmConsoleScreen.kt` | 2 | 3 | 1 | 2 | 2 | 2 | **2.0** | 4 | 2 | 1 | 3 | 3 | **2.6** | 7 |

### S7 — VM form & devices

| Screen | H1 | H2 | H3 | H4 | H5 | H6 | HCI | A1 | A2 | A3 | A4 | A5 | A11y | Findings |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| `vm/form/VmFormScreen.kt` | 3 | 3 | 2 | 3 | 2 | 2 | 2.5 | 5 | 3 | 3 | 4 | 3 | 3.6 | 13 |
| `vm/devices/VmDevicesScreen.kt` | 2 | 3 | 3 | 3 | 2 | 4 | 2.8 | 5 | 3 | 3 | 5 | 4 | **4.0** | 9 |
| `vm/devices/VmDeviceAddScreen.kt` | 3 | 2 | 1 | 3 | 2 | 2 | **2.2** | 5 | 3 | 3 | 4 | 3 | 3.6 | 11 |

## Worst and best screens

| | Screen | Why |
|---|---|---|
| **Worst HCI** | `vm/details/VmConsoleScreen.kt` (2.0) | Accepts every TLS error (P0), console region unlabelled, no error/loading state for a failed page, no IME inset |
| **Worst HCI (tie)** | `vm/devices/VmDeviceAddScreen.kt` (2.2) | Editing never restores the device type → can save a wrong `dtype` (P0); no validation |
| **Worst Accessibility** | `MarketplaceScreen.kt` (2.6) / `VmConsoleScreen.kt` (2.6) | Auto-advancing pager, unlabelled charts/console, colour-only state |
| **Best overall** | `containers/details/ContainerInfoPane.kt` (HCI 3.7 / A11y 4.0) | Labelled status, heading sections, honest delete guard |
| **Best HCI** | `apps/AppsScreen.kt` (4.0) | Complete loading/empty/error triad, reasons on disabled actions, per-card progress |
| **Best Accessibility** | `vm/devices/VmDevicesScreen.kt` (4.0) | Clean labels, ≥48dp targets, check-icon selection |

## P0 findings (details in `services-audit-fixes.md`)

| ID | Screen | Impact |
|---|---|---|
| `s6-31` | `vm/details/VmConsoleScreen.kt` | Auto-accepts **every** TLS/SSL error on a JS-enabled WebView — removes the trust decision and enables MITM script injection |
| `s6-01` | `vm/VmsScreen.kt` | On non-compact windows the side pane makes `VmInfoScreen` (console, devices, edit, clone, export, delete) unreachable |
| `s7-23` | `vm/devices/VmDeviceAddScreen.kt` | Editing a device never restores its type → a NIC/CD-ROM/PCI device is re-saved as a Disk with the wrong payload |

## Dimension leaders and laggards

- **Consistently strong:** touch-target sizing (A1 4.4), content hierarchy (H4 3.4),
  contrast (A4 3.5), text scaling (A5 3.4).
- **Consistently weak:** error prevention (H3 2.5), semantics (A3 2.6), adaptive layout (H5 2.8),
  labels/roles (A2 3.0).
- **Highest single-leverage fixes:** making the shared `ExpressiveChoiceField` body tappable
  (fixes ~15 picker defects), adding `liveRegion`/`stateDescription` to the shared
  `ExpressiveStatusDot` + `JobProgressView`, and handling IME insets once at the app level.
