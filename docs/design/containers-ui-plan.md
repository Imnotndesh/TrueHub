# Containers UI Restyle — Implementation Plan

Status: **Phases 0–4 implemented**; compile verified (`:app:compileGithubDebugKotlin`).

Companion docs:
- `docs/design/m3-expressive-research.md` — M3 Expressive research digest
- `docs/design/containers-ui-audit.md` — Apps vs Containers divergence audit

## Implementation status

Approved decisions: app-wide expressive motion; shared components; M3 `LoadingIndicator`
and manual refresh buttons removed (pull-to-refresh only); decorative `MaterialShapes`;
multi-select with bulk actions on the containers list.

Delivered:
- `TrueHubAppTheme` now uses `MotionScheme.expressive()`.
- Shared layer `ui/components/expressive/` (section, info card/row, badge, entry card, shape
  box, status chip/dot, action buttons, filter bar, form fields, choice sheet).
- `AppsScreen`, `AppInfoScreen` re-pointed to the shared layer; duplicate/dead components
  removed.
- `PullToRefreshContent` uses the M3 `LoadingIndicator`.
- `UnifiedScreenHeader` refresh button and `onRefresh` parameter removed.
- Containers list/detail/create/edit/devices/device-add/volumes restyled.

Open item (follow-up): none — the six screens that previously relied on the header refresh
button (`ShareInfoScreen`, `CloudSyncDetailScreen`, `CloudSyncScreen`, `SystemUpdateScreen`,
`DatasetExplorerScreen`, `AppResourceUsageScreen`) now use `PullToRefreshContent`.

Companion docs: see below.

Objective: translate the Applications design language (`AppsScreen`, `AppInfoScreen`,
`AppInfoPane`) onto `ContainersScreen` and every page it links to, using M3 Expressive
principles and the real APIs available in `material3:1.5.0-alpha23`. Structure, flows, and
ViewModels stay as they are.

## 1. Design principles applied

1. Preserve structure, restyle surfaces. Lists stay lists, forms stay forms, split panes stay
   split panes.
2. One shared expressive component layer instead of per-feature private copies.
3. Contain content: group related fields and info into section cards.
4. Shape + color create hierarchy; motion adds feel. No motion-only changes.
5. Emphasized typography only for titles, selected states, and primary actions.
6. Expressive motion comes from `MaterialTheme.motionScheme`, not ad-hoc springs.

## 2. Shell decisions (need confirmation)

These touch more than Containers and are the only broad changes proposed.

### D1. Adopt expressive motion + shapes at the theme level

`TrueHubAppTheme` currently calls `MaterialTheme(colorScheme, typography, content)`. The
default `motionScheme` is `standard()`, so no component gets expressive springs.

Proposed: pass an expressive motion scheme at the theme root:

```kotlin
MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography(),
    motionScheme = MotionScheme.expressive(),
    content = content
)
```

The M3 default shape scale in this artifact is already expressive-friendly:
`extraSmall 4`, `small 8`, `medium 12`, `large 16`, `largeIncreased 20`,
`extraLarge 28`, `extraLargeIncreased 32`, `extraExtraLarge 48` (dp). The Apps 28dp card
radius equals `MaterialTheme.shapes.extraLarge`, so no custom `Shapes` override is required;
component recipes can use `MaterialTheme.shapes.*` tokens instead of hardcoded dp. If we want
slightly softer sub-cards, the only optional override is `largeIncreased` (20dp default)
which already matches the 20dp sub-cards.

- Pros: every component (cards, dialogs, menus, sheets, FABs, chips) gets consistent
  expressive motion and the new radius scale; removes most hand-rolled `spring(...)` code.
- Cons: affects all screens app-wide (visually subtle, behaviorally springier).
- Alternatives: (a) `MaterialExpressiveTheme` wrapper; (b) leave theme untouched and apply
  `MotionScheme.expressive()` only to container subtrees; (c) leave motion standard and only
  change shapes.

**Recommendation: D1a** (theme-level, app-wide). It is the intended M3 Expressive adoption and
is low-risk.

### D2. Extract shared expressive components (used by both Apps and Containers)

Create `ui/components/expressive/` with the extracted, de-duplicated building blocks. Point
existing Apps/AppDetails surfaces at the shared versions (behaviour-preserving) and use them
for Containers.

- Pros: removes the duplication catalogued in the audit; guarantees Apps and Containers cannot
  drift again; satisfies the repo's modular-code rule.
- Cons: touches the currently-good Apps screens (behaviour must stay identical).
- Alternative: keep Apps untouched and create container-local copies. Rejected: it perpetuates
  the drift the task is fixing.

**Recommendation: D2 accept**, with a "no visual change" constraint on Apps screens.

### D3. Expressive pull-to-refresh / loading indicator

`PullToRefreshContent` already has a custom morphing indicator. Proposed: replace its inner
`CircularProgressIndicator` with the M3 `LoadingIndicator` (shape morph) and drive the
container size morph with `MaterialTheme.motionScheme`. Optionally add a contained variant.

**Recommendation: D3 accept (containers only at first)**, keeping the existing public API of
`PullToRefreshContent` unchanged.

## 3. Target design spec (Containers)

Locked to match Apps, with container-specific accents.

### Radii
- Hero / list / info cards: 28dp
- Sub-cards (device/volume/network rows, port cards): 20dp
- Action buttons & fields: 16dp
- Icon boxes: 12dp (small), 16dp (compact list), 20dp (expanded list)
- Status/badge pills: 100dp (full)
- Decorative leading shape: one `MaterialShapes` polygon per type (see below)

### Color
- Card surface: `surface`, border `outlineVariant` α0.4 (1dp); selected border `primary` 2dp.
- Leading icon box: container type → `primaryContainer` (container) / `tertiaryContainer`
  (VM) / `secondaryContainer` (default), content = matching `on*Container`.
- Status: RUNNING → `primary`; STOPPED → `outline`; ERROR → `error`; transitional →
  `tertiary`; UNKNOWN → `outlineVariant`. Shared semantic mapper.
- Danger actions: `errorContainer` / `onErrorContainer`, or `error`/`onError` for the
  destructive confirm button.

### Motion
- Card expand: `motionScheme.defaultSpatialSpec()` + `expandVertically`/`shrinkVertically`.
- Pane enter/exit: `motionScheme.defaultSpatialSpec()` slide + `fastEffectsSpec()` fade.
- Press scale: `motionScheme.fastSpatialSpec()`.
- Progress bars: `fastEffectsSpec()` for color, keep determinate progress animated.
- Status changes: `fastEffectsSpec()` color animation.

### Typography
- Screen/detail title: `headlineMedium` ExtraBold (header) / `titleLargeEmphasized` (hero).
- List card title: `titleMedium` bold (compact) / `titleLarge` bold (expanded).
- Section header: `titleMedium` bold.
- Primary action label: `labelLargeEmphasized`.
- Status chip: `labelMedium` SemiBold.
- Info row: label `bodyMedium onSurfaceVariant`, value `bodyMedium` SemiBold.

### Decorative shapes (leading visual)
Use `MaterialShapes` to give each container type a distinct decorative silhouette while
keeping a real icon on top:
- Container → `MaterialShapes.Cookie4Sided` (or `ClamShell`)
- VM → `MaterialShapes.ClamShell`
- Device rows → `MaterialShapes.Cookie6Sided`
- Volume rows → `MaterialShapes.Cookie7Sided`
- Status dot → `MaterialShapes.Circle`
Filled with the type/status container color; icon centered. Shape morphs on
selected/expanded if cheap to do.

## 4. Shared component layer (new)

Proposed package `com.imnotndesh.truehub.ui.components.expressive`:

| File | Public API | Replaces |
|---|---|---|
| `ExpressiveSection.kt` | `ExpressiveSection(title, icon, content)` | 3 variants |
| `ExpressiveInfoCard.kt` | `ExpressiveInfoCard(content)`, `ExpressiveInfoRow(label, value)` | 3 variants |
| `ExpressiveEntryCard.kt` | `ExpressiveEntryCard(icon, title, subtitle, onClick, enabled)` | `AppDetailEntryCard` |
| `ExpressiveBadge.kt` | `ExpressiveBadge(label, containerColor, contentColor)` | `AppBadge` |
| `ExpressiveStatus.kt` | `ExpressiveStatusChip(label, color, icon)`, `ExpressiveStatusDot(color)`, `StatusTone` + mappers | `StatusChip`, `getStatusColor/Icon` |
| `ExpressiveActionButton.kt` | `ExpressiveActionButton(text, icon, enabled, isPrimary, isDanger, onClick)`, `ExpressiveIconActionButton(icon, ...)` | `ActionButton`, `CompactActionButton` |
| `ExpressiveFilterBar.kt` | `ExpressiveFilterBar(items, selected, onSelect, label)` | `AppFilterBar`, `ContainerFilterBar` |
| `ExpressiveShapeBox.kt` | `ExpressiveShapeBox(shape, containerColor, content)` | "C"/"VM" glyph box |

Notes:
- Keep the APIs generic (no `Apps` / `Virt` types) so both features can use them.
- Move, don't duplicate: after extraction, delete the private copies in `AppsScreen.kt` and
  `AppInfoScreen.kt` and import the shared ones. Delete the dead public
  `MorphingRefreshIndicator` in `AppsScreen.kt`.
- Form primitives (`ChoiceField`, `ToggleSetting`, `ChoiceSheet`) also get a shared home:
  `ui/components/expressive/FormFields.kt` with `ExpressiveTextField`, `ExpressiveChoiceField`,
  `ExpressiveToggleRow`, `ExpressiveChoiceSheet`. These are currently duplicated across
  create/edit/device-add/volumes.

## 5. Per-file plan

### `services/containers/ContainersScreen.kt`
- Replace `ContainerCard` chrome with the Apps `ServiceCard` chrome: radius 28dp, elevation 0,
  surface + 1dp border, primary border when selected, `combinedClickable` if selection is added
  later (keep current tap behaviour).
- Replace the "C"/"VM" glyph with an `ExpressiveShapeBox` + type icon.
- Keep the compact/expanded split (chip on compact, dot on expanded) but route both through
  shared status components.
- Replace local `DetailItem` with a designed spec row inside the card: 3 cells using
  `labelSmall`/`bodyMedium`, aligned to the section spec; keep CPU/Memory/Autostart data.
- Replace local `ActionButton`/`CompactActionButton`/`StatusChip` with shared components.
- Replace `ContainerFilterBar` with `ExpressiveFilterBar`.
- Replace hardcoded springs with `MaterialTheme.motionScheme` specs.
- Keep `ExpressiveActionFab` and split-pane structure.

### `services/containers/details/ContainerInfoScreen.kt`
- Replace `Scaffold + UnifiedScreenHeader` with the Apps detail chrome: `Box` +
  `Scaffold(background)` + `MinimalBackHeader` overlay, content top padding ~96dp.
- Add a hero summary card (radius 28dp, border): type shape box, name `titleLargeEmphasized`,
  status `ExpressiveBadge`, and start/stop + edit `ExpressiveIconActionButton`s.
- Add navigation `ExpressiveEntryCard`s for Devices, Volumes, and Edit (replacing
  `ContainerActionsCard`'s plain buttons).
- Keep `ContainerInfoPane` as the body with `showInlineHeader = false`.

### `services/containers/details/ContainerInfoPane.kt`
- Delete local `ExpressiveSection`/`ExpressiveInfoCard`/`InfoRow`; use shared versions.
- Add a gradient pane header (like `AppInfoPaneHeader`) with the container shape box, name,
  status badge, and a close button; show it when `showInlineHeader = true`.
- Keep all existing sections (Status, System, Resources, Storage, VNC, Environment).
- Route status colors through the shared status tone mapper.
- Keep `bottomActions` slot API.

### `services/containers/create/ContainerCreateScreen.kt`
- Group fields into `ExpressiveSection`s: Identity (name, image), Resources (pool, CPU,
  memory, root disk, I/O bus), Behaviour (autostart, privileged).
- Wrap each group in `ExpressiveInfoCard`; use `ExpressiveTextField` / `ExpressiveChoiceField`
  / `ExpressiveToggleRow` from the shared layer.
- Keep the image/pool/io-bus sheets; restyle sheet rows as expressive selection cards.
- Keep FAB + pull-to-refresh + progress card; adopt theme motion.

### `services/containers/edit/ContainerEditScreen.kt`
- Same grouped-section treatment as create.
- Reuse the shared form primitives and the shared I/O bus choice sheet.

### `services/containers/devices/ContainerDevicesScreen.kt`
- Replace `DeviceRow` card with surface + border styling.
- Make the row an `ExpressiveEntryCard` (navigates to edit); move delete to a trailing
  overflow/`ExpressiveIconActionButton` with the danger treatment and the existing confirm
  dialog. Preserve `canEdit` and `isDeleting` behaviour.
- Keep empty state; optionally unify with a shared empty-state component.

### `services/containers/devices/ContainerDeviceAddScreen.kt`
- Replace the `FilterChip` type row with an expressive `ButtonGroup`/connected selection
  (or a styled `SingleChoiceSegmentedButtonRow` equivalent) while preserving the same
  `ContainerDeviceType` selection semantics.
- Group the type-specific fields in `ExpressiveSection`/`ExpressiveInfoCard`.
- Reuse shared form primitives and choice sheets.

### `services/containers/volumes/ContainerVolumesScreen.kt`
- Restyle `VolumeRow` to surface + border; use `ExpressiveShapeBox` + icon.
- Group `CreateVolumeSheet` fields into an expressive form with shared primitives.
- Keep the delete confirm dialog and `usedBy` guard.

### Cross-cutting
- `data/helpers/NavbarDestination.kt` / `Screen.kt`: no changes expected.
- `MainScreen.kt`: no navigation changes; only wiring already exists.

## 6. Phased rollout

**Phase 0 — Theme + shared layer (D1, D2)**
- Add expressive motion/shapes to `TrueHubAppTheme`.
- Create `ui/components/expressive/*`.
- Re-point Apps/AppDetails to shared components; delete duplicates/dead code.
- Verify Apps screens look/behave identically.

**Phase 1 — Containers list**
- Restyle `ContainersScreen.kt` (card, status, spec row, filter bar, motion).
- Verify compact, medium, expanded, split pane, selection, all status states, job progress.

**Phase 2 — Container detail**
- Restyle `ContainerInfoScreen.kt` + `ContainerInfoPane.kt`.
- Verify full-page and split-pane entry, refresh, actions, all optional sections.

**Phase 3 — Create / Edit**
- Group and restyle both forms; shared primitives; sheets.

**Phase 4 — Devices / Device add / Volumes**
- Restyle rows, type selector, forms, sheets.

**Phase 5 — Polish**
- Loading indicator in pull-to-refresh (D3), shape morph on selection/expand, empty states,
  a11y labels, contrast pass in all 7 themes + OLED black mode.

## 7. Decisions requested

Please confirm before implementation:

1. **D1** theme-level expressive motion + shapes (recommended) — app-wide, or containers-only?
2. **D2** extract shared expressive components and re-point Apps screens (recommended), or
   keep Apps untouched and duplicate for containers?
3. **D3** adopt the M3 `LoadingIndicator` for pull-to-refresh?
4. Container type visual: decorative `MaterialShapes` silhouette (recommended) or a plain
   rounded icon box like Apps?
5. Card interaction: add long-press multi-select to container cards (Apps has it; Containers
   currently does not), or keep single-action only?

## 8. Verification

- Build only the `github` variant (`:app:compileGithubDebugKotlin`) after each phase.
- Manual matrix: compact/medium/expanded; light/dark; OLED black; 7 themes; long names and
  long IDs; empty/populated; RUNNING/STOPPED/ERROR/transitional; job progress; split pane
  open/close; all bottom sheets and dialogs.
- No new compiler warnings; no unused imports left from removed local components.
