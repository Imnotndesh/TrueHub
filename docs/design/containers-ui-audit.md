# Containers UI Audit — Apps vs Containers

Goal: document exactly how the (well-designed) Applications surfaces differ from the
Containers surfaces, so the restyle is a concrete translation rather than a guess.

Reference "good" surfaces:
- `ui/services/apps/AppsScreen.kt` (list + split pane)
- `ui/services/apps/details/appdetails/AppInfoScreen.kt` (full detail page)
- `ui/services/apps/details/appdetails/AppInfoPane.kt` (side pane)
- Shared: `ui/components/UnitedScreenHeader.kt`, `ui/components/ExpressiveFAB.kt`,
  `ui/components/PullToRefreshContent.kt`

Target surfaces:
- `ui/services/containers/ContainersScreen.kt`
- `ui/services/containers/details/ContainerInfoScreen.kt`
- `ui/services/containers/details/ContainerInfoPane.kt`
- `ui/services/containers/create/ContainerCreateScreen.kt`
- `ui/services/containers/edit/ContainerEditScreen.kt`
- `ui/services/containers/devices/ContainerDevicesScreen.kt`
- `ui/services/containers/devices/ContainerDeviceAddScreen.kt`
- `ui/services/containers/volumes/ContainerVolumesScreen.kt`

## 1. The Apps design language (target)

### Token summary

| Element | Apps value |
|---|---|
| List card | `RoundedCornerShape(28.dp)`, `elevation 0dp`, `surface`, `BorderStroke(1dp, outlineVariant α0.4)` |
| Selected card | border `primary` 2dp (3dp in multi-select) |
| Compact leading icon | 52dp box, radius 16dp, `AsyncImage` |
| Expanded leading icon | 64dp box, radius 20dp, `secondaryContainer`, icon 36dp |
| Expanded status | 12dp `CircleShape` dot, semantic color |
| Compact status | `StatusChip` pill, `RoundedCornerShape(100.dp)`, tint α0.12 bg, icon 14dp, label 12dp bold |
| Card title | compact `titleMedium` bold; expanded `titleLarge` bold |
| Secondary line | `bodySmall` `onSurfaceVariant` |
| Update pill | `RoundedCornerShape(100.dp)`, `primaryContainer`, icon 16dp, `labelMedium` bold |
| Progress | height 8dp, `CircleShape`, `primary` on `primaryContainer` |
| Action buttons | `RoundedCornerShape(16.dp)`; primary `primary/onPrimary`; text `labelLarge` SemiBold; icon 18dp |
| Compact icon button | `RoundedCornerShape(16.dp)`, icon 24dp, vertical 14dp |
| "More options" toggle | `Surface` radius 20dp, `surfaceContainerHigh` α0.5 |
| Detail hero card | radius 28dp, border 1dp `outlineVariant` α0.4, `surface`, padding 20dp |
| Section header | 36dp `secondaryContainer` box radius 10dp, icon 20dp, `titleMedium` bold |
| Section info card | radius 28dp, `surface`, border 1dp `outlineVariant` α0.4, padding 20dp |
| Sub-card (network/port/volume) | radius 20dp, border 1dp `outlineVariant` α0.4 |
| Info row | label `bodyMedium onSurfaceVariant` weight .4; value `bodyMedium` SemiBold weight .6, end-aligned |
| Divider | `outlineVariant` α0.5 |
| Badge chip | `RoundedCornerShape(100.dp)`, `labelSmall` SemiBold, padding h10 v4 |
| Navigation entry card | radius 20dp, border 1dp; 44dp `primaryContainer` icon box radius 12dp, icon 22dp; title `titleMedium` SemiBold; subtitle `bodySmall`; trailing chevron |
| Detail chrome | `Box` + `Scaffold(background)` + `MinimalBackHeader` overlay; content scrolls behind header |
| Pane chrome | `Surface(surface, tonalElevation 2dp)`, 180dp `WavyGradientBackground` header, scroll padding 24dp, spacedBy 24dp |
| FAB | Material `FloatingActionButton` / `FloatingActionButtonMenu` + `ToggleFloatingActionButton` |

### Patterns

- Compact list = `LazyColumn`; medium/expanded = `LazyVerticalGrid`, column count via
  `AdaptiveLayoutHelper` (1/2/3), content padding 12/20/28dp, spacing 12/16/20dp.
- Card is `combinedClickable` (tap → detail/pane, long-press → selection mode). Selection
  state is border-driven, not a filled container.
- Expand/collapse uses `AnimatedVisibility` + `expandVertically/shrinkVertically` + fade.
- Detail page uses overlaid `MinimalBackHeader` and a scrim-free scrolling column, so the
  hero gradient is a hero moment.
- Split pane animates in with `slideInHorizontally` + fade.

## 2. Containers divergences (what looks "cured")

### `ContainersScreen.kt`

- `ContainerCard`: radius 24dp and `elevation 2dp` (apps: 28dp, elevation 0 + border).
  Selected state fills with `secondaryContainer` α0.4 instead of a primary border.
- Leading visual is a text glyph "C"/"VM" in a `primaryContainer`/`secondaryContainer` box,
  not an icon. Apps uses a real app icon; containers should use a type icon
  (`ViewInAr`/`Dns`/`WavingHand` style) plus `MaterialShapes` for decoration.
- Status is inconsistent: compact uses `StatusChip`, expanded uses a bare 12dp dot; apps
  uses the chip on compact and dot on expanded deliberately. Keep that split, but unify colors.
- Expanded title uses `lineHeight = 24.sp` hardcoded and `titleLarge`; apps uses `titleLarge`
  bold without a hardcoded line height.
- `DetailItem` (CPU/Memory/Autostart) has no equivalent in apps and reads as a cramped
  three-column row; needs a designed "spec row" treatment.
- Local private `ActionButton`, `CompactActionButton`, `StatusChip`, `getStatusColor`,
  `getStatusIcon`, `DetailItem`, `DeleteConfirmationDialog` duplicate the apps versions and
  have drifted (danger variant, `enabled` handling, colors).
- `ContainerFilterBar` is a byte-for-byte duplicate of `AppFilterBar`.
- Raw status colors (`Color(0xFF2E7D32)`, `0xFF757575`, `0xFF1976D2`, `0xFFF57C00`) instead
  of a semantic mapping shared with apps.

### `details/ContainerInfoScreen.kt`

- Uses `UnifiedScreenHeader` in `Scaffold.topBar` (a utilitarian bar), whereas
  `AppInfoScreen` uses a hero card + overlaid `MinimalBackHeader`. The container detail is
  therefore visually a different product.
- `ContainerActionsCard` uses plain `OutlinedButton` + `Button` in a `surfaceContainerLow`
  card. Apps uses `ExpressiveIconButton`s in the hero and `AppDetailEntryCard` navigation
  rows. Devices/Volumes/Edit are navigation destinations and should be entry cards.
- Status is only shown inside the pane, not in a hero summary.

### `details/ContainerInfoPane.kt`

- Local `ExpressiveSection` uses a 32dp box radius 8dp, icon 18dp, `titleMedium` title.
  Apps pane `ServiceInfoSection` uses a 24dp primary-tinted icon and `titleLarge` bold title.
- Local `ExpressiveInfoCard` uses `surfaceContainerLow`, radius 24dp, no border. Apps
  `ExpressiveInfoCard` uses `surface`, radius 28dp, border 1dp `outlineVariant` α0.4.
- Local `InfoRow` splits weight .4/.6 like apps — this one is already aligned.
- No pane header (pane only gets a close row); apps pane has a gradient header with icon,
  title, and version.

### `create/ContainerCreateScreen.kt`

- Bare `OutlinedTextField`s stacked with `Arrangement.spacedBy(14.dp)`, radius 14dp, no
  section grouping, no cards, no icons.
- `ToggleSetting` is a plain row. Apps groups related fields into `ExpressiveSection` +
  `ExpressiveInfoCard`.
- Bottom sheets are functional but plain (`headlineSmall` + `DropdownMenuItem`s). Could adopt
  the expressive card-row selection used by the image chooser.
- No `ExperimentalMaterial3ExpressiveApi`; no expressive motion/shape.

### `edit/ContainerEditScreen.kt`

- Same bare-form issue as create; duplicated `SettingSwitch`/`ChoiceField`/`ModalBottomSheet`
  picker code.

### `devices/ContainerDevicesScreen.kt`

- `DeviceRow` uses `Card(surfaceContainerLow, radius 20dp)`, leading 24dp icon, trailing
  edit/delete `IconButton`s. Apps uses `surface` + border, and `AppDetailEntryCard` for
  navigational rows. Edit should be an entry card; delete should be a deliberate action, not a
  bare icon that competes with the row tap.
- Empty state is a simple centered column; apps uses the same shape (acceptable) but could be
  unified.

### `devices/ContainerDeviceAddScreen.kt`

- Plain form; `FilterChip` row used as a stand-in for a segmented control — M3 Expressive
  provides `ButtonGroup`/connected button group for exactly this.
- Duplicate `ChoiceField`, `ToggleField`, `SimpleChoiceSheet`, `DeviceMapChoiceSheet`.

### `volumes/ContainerVolumesScreen.kt`

- `VolumeRow` uses `surfaceContainerLow` card, trailing delete `IconButton`.
- `CreateVolumeSheet` is a raw text-field form, not a grouped expressive form.

## 3. Duplication map (extraction candidates)

These exist in near-identical form in both apps and containers, or privately per file:

| Component | Locations | Note |
|---|---|---|
| `ActionButton` (text + icon) | AppsScreen (private), ContainersScreen (private + `isDanger`) | Merge, keep `isDanger`, `enabled` |
| `CompactActionButton` | AppsScreen, ContainersScreen | Identical intent |
| `StatusChip` | AppsScreen, ContainersScreen | Different data types (String vs enum) |
| `getStatusColor` / `getStatusIcon` | AppsScreen, ContainersScreen | Semantic mapping by status |
| `ExpressiveSection` | AppInfoScreen (public), AppInfoPane (private variant), ContainerInfoPane (private variant) | 3 variants |
| `ExpressiveInfoCard` | AppInfoScreen (public), ContainerInfoPane (private variant) | 2 variants |
| `InfoRow` | AppInfoScreen, AppInfoPane (`ServiceInfoRow`), ContainerInfoPane | 3 variants |
| Filter bar | `AppFilterBar`, `ContainerFilterBar` | Identical |
| `AppDetailEntryCard` | AppInfoScreen (private) | Needed by containers |
| `AppBadge` | AppInfoScreen (private) | Needed by containers |
| `MorphingRefreshIndicator` | AppsScreen (public, unused) + PullToRefreshContent (private) | Dead public duplicate |
| ChoiceField / ToggleSetting / ToggleField / SimpleChoiceSheet / MapChoiceSheet | Create, Edit, DeviceAdd, Volumes | Repeated picker infrastructure |

## 4. Non-goals

- No changes to ViewModels, API services, navigation graph, or data models beyond what the UI
  needs.
- No restructuring of the create/edit/device/volume flows.
- No changes to the playstore flavor.
- No new runtime permissions or dependencies.
