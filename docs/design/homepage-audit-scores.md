# Homepage Module — HCI & Accessibility Scorecard

Audit of the TrueHub **homepage module** (`ui/homepage/**`) against
`docs/design/hci-android-guidelines.md` and `docs/design/m3-expressive-research.md` (§8–§10).

Companion document: `homepage-audit-fixes.md` (what to change). Raw per-group findings:
`docs/design/homepage-audit/g1.md` … `g6.md`.

## Scope & method

- **49 screens/sheets** audited, split into 6 areas.
- Each screen scored **0–5** (5 = excellent) on 6 HCI dimensions and 5 accessibility
  dimensions, with `file:line` evidence for every finding.
- Severity: **P0** blocks a user group or risks data loss; **P1** significant
  usability/accessibility degradation; **P2** polish/consistency.
- Findings were verified by reading source. Contrast ratios were **computed** from the
  actual theme token values, not measured on device — reconfirm with Accessibility
  Scanner. There are **no instrumented UI tests** for these screens, so everything is
  static analysis.

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
| **HCI average** | **3.1 / 5** |
| **Accessibility average** | **3.3 / 5** |
| **Findings** | **3 P0 · 110 P1 · 203 P2** (group-reported) |

The module is functionally well-structured (navigation and information hierarchy are the
strongest dimensions) but has a consistent, systemic accessibility deficit and a recurring
error-handling/feedback deficit.

### Dimension rollup (average across all 49 screens)

| Dimension | Avg | Read |
|---|---|---|
| H1 Feedback & status | 2.8 | Weak — silent successes/failures are the norm |
| H2 Navigation & mental model | 3.6 | Strong |
| H3 Error prevention & recovery | **2.6** | Weakest HCI dimension |
| H4 Cognitive load & hierarchy | 3.6 | Strong |
| H5 Layout, readability & adaptive | 3.0 | No window-size-class use anywhere |
| H6 Motion & M3 Expressive | 3.0 | Theme opts in, screens bypass it |
| A1 Touch targets | 4.0 | Strongest overall |
| A2 Labels & roles | 3.1 | Dragged down by unlabelled switches |
| A3 Semantics | **2.7** | Weakest accessibility dimension |
| A4 Contrast & colour independence | 3.6 | Hardcoded status colours are the fault line |
| A5 Text scaling & focus | 3.2 | Fixed heights clip at large font scale |

### Group rollup

| Group | Screens | HCI avg | A11y avg | P0 | P1 | P2 |
|---|---|---:|---:|---:|---:|---:|
| G1 — Homepage dashboard & details | 4 | 2.9 | 2.8 | 0 | 14 | 20 |
| G2 — System info & general/advanced settings | 11 | 3.2 | 3.8 | 0 | 15 | 36 |
| G3 — Users & API keys | 8 | 2.8 | **2.9** | **2** | 20 | 33 |
| G4 — Alert services & audit | 6 | 3.0 | 3.2 | **1** | 20 | 27 |
| G5 — Boot, network, cloud sync & services | 11 | **3.3** | 3.1 | 0 | 20 | 39 |
| G6 — App images, update, pools, shares & dataset | 9 | 3.0 | 3.7 | 0 | 21 | 48 |
| **Total** | **49** | **3.1** | **3.3** | **3** | **110** | **203** |

## Per-screen scores

Paths are relative to `app/src/main/java/com/imnotndesh/truehub/ui/homepage/`.
`Findings` = number of rows in that screen's finding table.

### G1 — Homepage dashboard & details

| Screen | H1 | H2 | H3 | H4 | H5 | H6 | HCI | A1 | A2 | A3 | A4 | A5 | A11y | Findings |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| `Homepage.kt` | 3 | 3 | 4 | 4 | 3 | 3 | 3.3 | 3 | 3 | 2 | 2 | 3 | 2.6 | 10 |
| `details/DiskInfoScreen.kt` | 2 | 4 | 3 | 3 | 3 | 2 | 2.8 | 4 | 4 | 2 | 3 | 2 | 3.0 | 9 |
| `details/PerformanceScreen.kt` | 1 | 3 | 2 | 3 | 3 | 2 | **2.3** | 4 | 2 | 2 | 3 | 2 | 2.6 | 8 |
| `details/ShareInfoScreen.kt` | 3 | 4 | 4 | 3 | 3 | 3 | 3.3 | 4 | 4 | 2 | 2 | 2 | 2.8 | 7 |

### G2 — System info & general/advanced settings

| Screen | H1 | H2 | H3 | H4 | H5 | H6 | HCI | A1 | A2 | A3 | A4 | A5 | A11y | Findings |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| `instancesettings/systeminformation/SystemInformationScreen.kt` | 4 | 4 | 4 | 4 | 3 | 3 | 3.7 | 5 | 3 | 3 | 5 | 4 | 4.0 | 5 |
| `instancesettings/systeminformation/HardwareInformationScreen.kt` | 2 | 3 | 2 | 4 | 3 | 4 | 3.0 | 5 | 4 | 3 | 5 | 3 | 4.0 | 6 |
| `instancesettings/systeminformation/SoftwareInformationScreen.kt` | 2 | 3 | 2 | 4 | 3 | 3 | 2.8 | 5 | 4 | 3 | 5 | 3 | 4.0 | 4 |
| `instancesettings/systeminformation/TrueCommandScreen.kt` | 3 | 4 | 2 | 4 | 3 | 3 | 3.2 | 5 | 2 | 2 | 5 | 3 | 3.4 | 3 |
| `instancesettings/systeminformation/TrueNasConnectScreen.kt` | 3 | 3 | 2 | 4 | 3 | 3 | 3.0 | 5 | 2 | 3 | 5 | 3 | 3.6 | 4 |
| `instancesettings/general/GeneralSystemSettingsScreen.kt` | 4 | 3 | 3 | 4 | 3 | 3 | 3.3 | 5 | 4 | 3 | 5 | 3 | 4.0 | 5 |
| `instancesettings/general/GeneralSystemSettingsEditScreen.kt` | 4 | 4 | 2 | 4 | 3 | 3 | 3.3 | 5 | 2 | 3 | 5 | 3 | 3.6 | 5 |
| `instancesettings/advanced/AdvancedSystemSettingsScreen.kt` | 3 | 3 | 3 | 3 | 3 | 3 | 3.0 | 5 | 4 | 3 | 5 | 3 | 4.0 | 3 |
| `instancesettings/advanced/AdvancedSystemSettingsEditScreen.kt` | 4 | 4 | 2 | 3 | 3 | 3 | 3.2 | 5 | 2 | 3 | 5 | 3 | 3.6 | 5 |
| `instancesettings/globalvirt/GlobalVirtSettingsScreen.kt` | 5 | 4 | 2 | 4 | 3 | 3 | 3.5 | 4 | 3 | 3 | 5 | 3 | 3.6 | 6 |
| `instancesettings/InstanceConfigScreen.kt` | 4 | 4 | 3 | 3 | 3 | 3 | 3.3 | 5 | 3 | 3 | 5 | 4 | 4.0 | 5 |

### G3 — Users & API keys

| Screen | H1 | H2 | H3 | H4 | H5 | H6 | HCI | A1 | A2 | A3 | A4 | A5 | A11y | Findings |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| `instancesettings/users/UserListScreen.kt` | 4 | 4 | 4 | 4 | 3 | 3 | 3.7 | 4 | 4 | 4 | 2 | 3 | 3.4 | 7 |
| `instancesettings/users/UserCreateScreen.kt` | 2 | 4 | 2 | 4 | 3 | 2 | 2.8 | 4 | 2 | 3 | 4 | 2 | 3.0 | 8 |
| `instancesettings/users/UserDetailScreen.kt` | 2 | 4 | 2 | 3 | 3 | 3 | 2.8 | 4 | 3 | 3 | 2 | 3 | 3.0 | 8 |
| `instancesettings/users/LocalAdminSetupScreen.kt` | 2 | 4 | 2 | 4 | 3 | 2 | 2.8 | 4 | 4 | 3 | 4 | 3 | 3.6 | 6 |
| `instancesettings/apikeys/ApiKeyListScreen.kt` | 1 | 3 | 3 | 3 | 3 | 3 | 2.7 | 4 | 1 | 2 | 2 | 3 | **2.4** | 7 |
| `instancesettings/apikeys/ApiKeyCreateScreen.kt` | 2 | 3 | 2 | 3 | 3 | 2 | 2.5 | 3 | 2 | 2 | 3 | 2 | 2.4 | 6 |
| `instancesettings/apikeys/ApiKeyDetailsScreen.kt` | 2 | 3 | 1 | 3 | 3 | 3 | 2.5 | 4 | 3 | 3 | 2 | 3 | 3.0 | 6 |
| `instancesettings/apikeys/CreditCardView.kt` | 3 | 3 | 2 | 3 | 1 | 2 | **2.3** | 2 | 3 | 2 | 2 | 2 | **2.2** | 7 |

### G4 — Alert services & audit

| Screen | H1 | H2 | H3 | H4 | H5 | H6 | HCI | A1 | A2 | A3 | A4 | A5 | A11y | Findings |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| `instancesettings/alertservice/AlertServicesListScreen.kt` | 3 | 4 | 3 | 4 | 3 | 3 | 3.3 | 4 | 4 | 3 | 2 | 4 | 3.4 | 8 |
| `instancesettings/alertservice/AlertServiceCreateScreen.kt` | 3 | 3 | 2 | 4 | 3 | 2 | 2.8 | 4 | 3 | 3 | 4 | 4 | 3.6 | 8 |
| `instancesettings/alertservice/AlertServiceDetailScreen.kt` | 1 | 4 | 3 | 4 | 3 | 3 | 3.0 | 4 | 3 | 3 | 3 | 3 | 3.2 | 8 |
| `instancesettings/alertservice/AlertClassesConfigScreen.kt` | 3 | 4 | 2 | 4 | 3 | 2 | 3.0 | 2 | 3 | 3 | 2 | 3 | 2.6 | 8 |
| `instancesettings/audit/AuditConfigScreen.kt` | 2 | 4 | 2 | 4 | 4 | 3 | 3.2 | 5 | 4 | 3 | 3 | 4 | 3.8 | 7 |
| `instancesettings/audit/AuditLogsScreen.kt` | 2 | 2 | 3 | 3 | 3 | 3 | 2.7 | 4 | 2 | 3 | 2 | 3 | 2.8 | 9 |

### G5 — Boot, network, cloud sync & services

| Screen | H1 | H2 | H3 | H4 | H5 | H6 | HCI | A1 | A2 | A3 | A4 | A5 | A11y | Findings |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| `instancesettings/boot/BootScreen.kt` | 3 | 4 | 3 | 4 | 3 | 4 | 3.5 | 2 | 3 | 2 | 4 | 3 | 2.8 | 5 |
| `instancesettings/boot/BootPoolScreen.kt` | 3 | 4 | 3 | 3 | 3 | 4 | 3.3 | 4 | 4 | 3 | 4 | 3 | 3.6 | 6 |
| `instancesettings/boot/BootEnvironmentsScreen.kt` | 4 | 3 | 2 | 3 | 3 | 4 | 3.2 | 2 | 3 | 2 | 4 | 2 | 2.6 | 7 |
| `instancesettings/boot/BootEnvironmentDetailScreen.kt` | 3 | 4 | 3 | 4 | 3 | 4 | 3.5 | 4 | 2 | 3 | 4 | 3 | 3.2 | 4 |
| `instancesettings/network/NetworkScreen.kt` | 3 | 3 | 4 | 2 | 3 | 4 | 3.2 | 4 | 4 | 2 | 4 | 3 | 3.4 | 5 |
| `instancesettings/network/NetworkEditScreen.kt` | 4 | 3 | 2 | 4 | 3 | 4 | 3.3 | 4 | 4 | 2 | 4 | 3 | 3.4 | 6 |
| `instancesettings/cloudsync/CloudSyncScreen.kt` | 3 | 4 | 3 | 4 | 2 | 3 | 3.2 | 4 | 3 | 2 | 2 | 3 | 2.8 | 5 |
| `instancesettings/cloudsync/CloudSyncFormScreen.kt` | 2 | 3 | 2 | 4 | 3 | 4 | 3.0 | 4 | 2 | 3 | 4 | 3 | 3.2 | 6 |
| `instancesettings/cloudsync/CloudSyncDetailScreen.kt` | 3 | 3 | 3 | 4 | 3 | 4 | 3.3 | 4 | 4 | 3 | 4 | 3 | 3.6 | 4 |
| `instancesettings/service/ServicesScreen.kt` | 4 | 3 | 3 | 4 | 3 | 3 | 3.3 | 4 | 2 | 2 | 3 | 3 | 2.8 | 5 |
| `instancesettings/service/ServiceDetailScreen.kt` | 4 | 4 | 3 | 4 | 3 | 3 | 3.5 | 4 | 2 | 3 | 3 | 4 | 3.2 | 6 |

### G6 — App images, update, pools, shares & dataset

| Screen | H1 | H2 | H3 | H4 | H5 | H6 | HCI | A1 | A2 | A3 | A4 | A5 | A11y | Findings |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| `instancesettings/appimages/AppImageManagementScreen.kt` | 4 | 5 | 5 | 4 | 3 | 3 | **4.0** | 5 | 4 | 4 | 5 | 5 | **4.6** | 4 |
| `instancesettings/appimages/DockerImageListScreen.kt` | 2 | 3 | 2 | 3 | 3 | 3 | 2.7 | 5 | 4 | 3 | 4 | 4 | 4.0 | 9 |
| `instancesettings/appimages/IxVolumeListScreen.kt` | 3 | 4 | 3 | 3 | 3 | 3 | 3.2 | 5 | 4 | 3 | 4 | 5 | 4.2 | 4 |
| `systemupdate/SystemUpdateScreen.kt` | 2 | 4 | 2 | 3 | 3 | 3 | 2.8 | 5 | 4 | 3 | 4 | 4 | 4.0 | 7 |
| `pools/PoolDetailsScreen.kt` | 2 | 4 | 2 | 4 | 3 | 2 | 2.8 | 5 | 4 | 3 | 2 | 4 | 3.6 | 10 |
| `shares/CreateShareScreen.kt` | 3 | 4 | 2 | 3 | 3 | 3 | 3.0 | 4 | 3 | 3 | 4 | 5 | 3.8 | 7 |
| `dataset/DatasetExplorerScreen.kt` | 2 | 3 | 2 | 4 | 3 | 2 | 2.7 | 2 | 2 | 2 | 3 | 4 | 2.6 | 12 |
| `dataset/DatasetPickerSheet.kt` | 2 | 4 | 3 | 4 | 3 | 3 | 3.2 | 2 | 3 | 2 | 3 | 4 | 2.8 | 7 |
| `dataset/ServerPathPickerSheet.kt` | 2 | 3 | 3 | 3 | 3 | 3 | 2.8 | 3 | 3 | 3 | 4 | 4 | 3.4 | 7 |

## Worst and best screens

| | Screen | Why |
|---|---|---|
| **Worst HCI** | `details/PerformanceScreen.kt` (2.3) | Frozen data labelled "Live", hardcoded 28 GB/32 GB memory verdict, chart invisible to TalkBack |
| **Worst HCI (tie)** | `apikeys/CreditCardView.kt` (2.3) | Fixed 340×210dp card clips the one-time API key; <48dp "Done" control |
| **Worst Accessibility** | `apikeys/CreditCardView.kt` (2.2) | Clipped secret, contrast failures on the gradient, no state description for reveal |
| **Worst Accessibility (tie)** | `apikeys/ApiKeyListScreen.kt` (2.4) / `apikeys/ApiKeyCreateScreen.kt` (2.4) | Unlabelled FAB is the only create entry point; `A2` = 1 |
| **Best overall** | `instancesettings/appimages/AppImageManagementScreen.kt` (HCI 4.0 / A11y 4.6) | Clean two-choice hub, correct decorative semantics, theme-safe contrast |

## P0 findings (details in `homepage-audit-fixes.md`)

| ID | Screen | Impact |
|---|---|---|
| `g3-16` | `users/UserDetailScreen.kt` | Renewing 2FA discards the only copy of the new `provisioning_uri` → permanent 2FA lockout |
| `g3-43` | `apikeys/ApiKeyDetailsScreen.kt` | Resetting an API key discards the new key material → key unrecoverable |
| `g4-17` | `alertservice/AlertServiceDetailScreen.kt` | No `SnackbarHost`; test/create/update/error messages are never rendered |

## Dimension leaders and laggards

- **Consistently strong:** navigation structure (H2 3.6), content hierarchy (H4 3.6), touch
  target sizing (A1 4.0), contrast in read-only G2 surfaces (A4 5.0 across 11 screens).
- **Consistently weak:** error prevention (H3 2.6), screen-reader semantics (A3 2.7), and
  outcome feedback (H1 2.8).
- **Most improved by a single fix:** A2/A3 would move sharply if the unlabelled-switch
  pattern were fixed once in the shared `ExpressiveToggleRow` (see fixes doc, theme 1).
