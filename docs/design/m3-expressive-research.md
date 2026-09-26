# Material 3 Expressive — Research Notes

Scope: research digest used to plan the Containers UI restyle. Sources are the official
Material Design 3 guidance and the Android/Compose reference, cross-checked against the
actual `material3` artifact used in this project.

## Sources

- `https://m3.material.io/get-started`
- `https://m3.material.io/blog/building-with-m3-expressive`
- `https://m3.material.io/styles/motion/overview/how-it-works`
- `https://m3.material.io/styles/shape/overview-principles` and `/shape/shape-morph`
- `https://m3.material.io/styles/typography/type-scale-tokens`
- `https://m3.material.io/components/cards`, `/buttons`, `/button-groups`, `/split-button`, `/loading-indicator`
- `https://developer.android.com/develop/ui/compose/designsystems/material3`
- `https://developer.android.com/reference/kotlin/androidx/compose/material3/MaterialExpressiveTheme.composable`
- `https://design.google/library/expressive-material-design-google-research`
- `https://developer.android.com/develop/ui/compose/animation/introduction` and `/animation/shared-elements`
- `https://developer.android.com/develop/adaptive-apps` and `/develop/adaptive-apps/guides/use-window-size-classes`
- `https://developer.android.com/develop/adaptive-apps/guides/adaptive-dos-and-donts`
- `https://developer.android.com/design/ui/mobile/guides/layout-and-content/adapt-layout`
- `https://developer.android.com/guide/topics/ui/accessibility/apps` and `/accessibility/principles` and `/accessibility/composables`
- `https://developer.android.com/develop/ui/compose/accessibility` (API defaults, semantics, merging and clearing)
- `https://developer.android.com/distribute/aep/aep-req-physics-based-motion`
- Local artifact: `androidx.compose.material3:material3:1.5.0-alpha23` sources (verified API surface)

## 1. What M3 Expressive is

M3 Expressive is an expansion of Material 3, not a new version ("not M4"). It adds new
components, a physics-based motion system, an expanded shape library, emphasized typography,
and vibrant color guidance. It is backed by 46 studies / 18k+ participants. Reported outcomes:

- Expressive designs rated higher for playful, energetic, creative, friendly.
- Key UI elements spotted up to 4x faster in expressive layouts.
- Better perceived usability for users with varied movement/visual abilities.

Guiding caution from the research: expressive design must not break established UI patterns.
Removing labels or restructuring familiar navigation hurt usability scores. Expression is
applied on top of familiar structure, not instead of it.

## 2. The seven expressive tactics

These are the practical levers. They map directly onto the Containers restyle.

1. **Use a variety of shapes.** Mix corner radii and shape silhouettes for tension/cohesion.
   Break from the surrounding shape style to draw attention. Avoid making essential actions
   visually smaller.
2. **Apply rich, nuanced color.** Layer primary/secondary/tertiary containers to prioritize
   actions and clarify hierarchy. Avoid low-contrast surfaces where things blend together.
3. **Guide attention with typography.** Use emphasized styles for headlines, selected states,
   and primary actions. Heavier weight + size + color + spacing create editorial moments.
4. **Contain content for emphasis.** Group related content into containers; give the most
   important content the brightest surface and the most space.
5. **Add fluid, natural motion.** Spring-based motion and shape morph; expressive springs for
   key interactions.
6. **Leverage component flexibility.** Adapt components to context/screen size (compact vs
   medium vs expanded).
7. **Combine tactics into hero moments.** One or two per product, not everywhere. Over-use
   becomes noise.

## 3. Shape system

The shape scale defines container corner style from square to fully circular. Compose's
`Shapes` class now carries a wider scale than the classic five roles (verified in
`material3:1.5.0-alpha23`):

| Token | Role |
|---|---|
| `extraSmall` | menus, text fields, snackbars |
| `small` | chips |
| `medium` | cards, small FABs |
| `large` | extended FABs, FABs, drawers |
| `largeIncreased` | slightly larger than `large` |
| `extraLarge` | large FABs |
| `extraLargeIncreased` | between `extraLarge` and `extraExtraLarge` |
| `extraExtraLarge` | near-circular containers |

Defaults in the artifact: `extraSmall = 4dp`, `small = 8dp`, `medium = 12dp`,
`large = 16dp`, `extraLarge = 24dp` (plus the new increased/extra roles).

The **Material shape library** exposes 35 named `RoundedPolygon` shapes for decorative
containers and avatars, with built-in morphing between any two via `Morph` / `toShape()`.
Names available in `MaterialShapes` (verified): `Circle`, `Square`, `Slanted`, `Arch`,
`Fan`, `Arrow`, `SemiCircle`, `Oval`, `Pill`, `Triangle`, `Diamond`, `ClamShell`,
`Pentagon`, `Gem`, `Sunny`, `VerySunny`, `Cookie4Sided`, `Cookie6Sided`, `Cookie7Sided`,
`Cookie9Sided`, `Cookie12Sided`, `Ghostish`, `Clover4Leaf`, `Clover8Leaf`, `Burst`,
`SoftBurst`, `Boom`, `SoftBoom`, `Flower`, `Puffy`, `PuffyDiamond`, `PixelCircle`,
`PixelTriangle`, `Bun`, `Heart`.

Guidance:
- Shapes are decoration/attention, not a replacement for labels or affordances.
- Morphing uses the expressive motion scheme by default.
- Don't shrink the shape of essential actions.

## 4. Motion physics system

M3 Expressive replaces easing+duration with **springs** governed by stiffness (speed),
damping (bounce decay), and initial velocity. Two preset schemes:

- **Expressive** — overshoots and bounces into place; recommended for prominent UI and hero
  interactions.
- **Standard** — minimal bounce, linear; for utilitarian/recurring interactions.

Two categories of tokens, each at three speeds (`default`, `fast`, `slow`):

- **Spatial** — position, rotation, size, **rounded corners**. Overshoots (bouncy).
- **Effects** — color and opacity. Never overshoots.

Rules of thumb: most motion uses `default`; small components use `fast`; large/full-screen
transitions use `slow`. Exit transitions are faster than enter transitions.

Compose API (`material3:1.5.0-alpha23`, verified):
- `MotionScheme.expressive()` / `MotionScheme.standard()`
- `MaterialTheme.motionScheme` with:
  `defaultSpatialSpec()`, `fastSpatialSpec()`, `slowSpatialSpec()`,
  `defaultEffectsSpec()`, `fastEffectsSpec()`, `slowEffectsSpec()`
- `MaterialTheme(motionScheme = ...)` and `MaterialExpressiveTheme(motionScheme = ...)`

**Finding for this repo:** `MaterialTheme.Values.motionScheme` defaults to `standard()`.
`TrueHubAppTheme` already opts in with `motionScheme = MotionScheme.expressive()` (see
`ui/theme/AdaptiveTheme.kt`), so every M3 component that reads the theme's motion scheme
inherits expressive springs automatically. The remaining gap is **custom/screen-level
animations**: several screens still hardcode local `spring(stiffness = ...)` values instead of
reading `MaterialTheme.motionScheme`, which makes their motion drift from the component
motion around them. Migrating those call sites to the theme tokens (Section 8) is the
highest-leverage follow-up.

Application levels:
1. Use a default scheme (expressive or standard) globally.
2. Provide a custom `MotionScheme`.
3. Override the scheme for a single composable/screen via the CompositionLocal.

## 5. Typography

M3 has one type scale with **15 baseline + 15 emphasized** styles (Display Large → Label
Small). Emphasized styles have higher weight and slight metric adjustments. They are intended
for selection, actions, headlines, unread/important states, and editorial moments. Baseline
and emphasized are meant to be used together.

Compose exposes them directly on `Typography` (verified): `titleLargeEmphasized`,
`titleMediumEmphasized`, `bodyMediumEmphasized`, `labelLargeEmphasized`, ... down to
`labelSmallEmphasized`. Material components do **not** use emphasized styles by default — you
swap the baseline style for the emphasized variant where you want emphasis.

## 6. Components relevant to this app

- **Cards** — the container is the only required element; layouts are flexible. Cards can
  carry one primary action or several action areas (buttons, icon buttons, chips). Reserve
  container-transform transitions for hero moments.
- **Buttons** — now support round/square shapes, five sizes (XS–XL), toggle behavior, and
  shape-morph on press/select. Icons default to 20dp; default height 40dp with full rounding.
- **Button groups** — standard and connected variants; connected replaces segmented buttons
  (now deprecated). Shape morphs on press/select; supports single/multi/required selection.
  Compose: `ButtonGroup { ... }`.
- **Split button** — a primary action plus a connected menu button that spins/morphs when
  activated. Compose: `SplitButtonLayout(leadingButton, trailingButton)`.
- **Loading indicator** — the recommended replacement for indeterminate circular spinners,
  especially for pull-to-refresh. Can be contained or uncontained, and morphs between a list
  of `RoundedPolygon` shapes. Compose: `LoadingIndicator(progress)`,
  `LoadingIndicator()`, `ContainedLoadingIndicator(...)`.
- **Toolbars / FAB menu** — expressive containers for frequent actions; can pair a toolbar
  with a FAB. Compose: `HorizontalFloatingToolbar`, `FloatingActionButtonMenu`,
  `ToggleFloatingActionButton`.
- **Progress indicators** — expressive variants use shape and motion; `LinearWavyProgressIndicator`
  / `CircularWavyProgressIndicator` exist in this artifact.

## 7. Architecture takeaways for TrueHub

1. **Theme-level expressive motion** is the cleanest way to make every screen feel alive
   without per-screen animation hacks. It is a one-line theme change with broad reach.
2. **A shared expressive component layer** (section header, info card, info row, status chip,
   action button, entry/navigation card, badge, filter bar, loading/refresh indicator) removes
   the current Apps/Containers duplication and drift, and is exactly the "generic/modular
   building blocks" the repo rules ask for.
3. **Shape + color + containment** create the expressive look; motion adds the feel. Components
   should mix radii (28dp hero cards, 20dp sub-cards, 16dp action pills, 100dp status pills,
   decorative `MaterialShapes` for leading icons/status dots).
4. **Preserve familiar structure.** Keep the existing list/grid, split-pane, form, and sheet
   flows exactly as they are. Restyle surfaces, hierarchy, motion, and states only.
5. **Emphasized typography** should be used sparingly: screen/detail titles, selected card
   titles, primary action labels, and status numbers.

---

## 8. Expressive animations — implementation guide

Source: `developer.android.com/develop/ui/compose/animation/*`, `m3.material.io/styles/motion/*`,
AEP physics-based-motion requirements, verified against `material3:1.5.0-alpha23`.

### 8.1 The one-line baseline (already in place)

Expressive motion is a **theme-level** concern, not a per-screen one. `TrueHubAppTheme`
sets `motionScheme = MotionScheme.expressive()`, so the ~21 Material components that read
the theme scheme (buttons, switches, FABs, sheets, navigation, etc.) already animate with
expressive springs. Do **not** re-specify springs for those components.

```kotlin
MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography(),
    motionScheme = MotionScheme.expressive(),   // expressive across all M3 components
    content = content
)
```

### 8.2 Consume the scheme tokens in custom animations

For any animation you write by hand, pull the spec from the theme instead of hardcoding
`spring(...)`. This keeps custom motion identical to component motion and makes the whole app
re-pointable by swapping one scheme.

```kotlin
val motion = MaterialTheme.motionScheme

// Position / size / rotation / corner radius — may overshoot (spatial)
val offset by animateDpAsState(
    targetValue = if (expanded) 24.dp else 0.dp,
    animationSpec = motion.defaultSpatialSpec(),
    label = "offset"
)

// Color / opacity — must NOT overshoot (effects)
val alpha by animateFloatAsState(
    targetValue = if (visible) 1f else 0f,
    animationSpec = motion.fastEffectsSpec(),
    label = "alpha"
)
```

Token selection:

| Need | Token | Overshoots? |
|---|---|---|
| x/y position, size, rotation, rounded corners | `defaultSpatialSpec()` | yes (bounce) |
| same, small components (switch, button, chip) | `fastSpatialSpec()` | yes |
| same, full-screen / large container transitions | `slowSpatialSpec()` | yes |
| color, opacity, tint | `defaultEffectsSpec()` | no |
| color/opacity on small components | `fastEffectsSpec()` | no |
| full-screen content refresh (color/opacity) | `slowEffectsSpec()` | no |

Rule of thumb: **most motion = default**; small elements = fast; large/full-screen = slow.
Exit transitions should resolve faster than enter transitions. Spatial springs overshoot to
bounce into place; effects springs are damped so color/opacity never overshoot.

### 8.3 Spring token values (for non-Compose targets / web parity)

From the M3 motion specs. Compose gets these via the tokens above; use this table only when
porting to a platform without spring tokens.

| Spring | Curve (x1,y1,x2,y2) | Duration |
|---|---|---|
| Expressive fast spatial | 0.42, 1.67, 0.21, 0.90 | 350 ms |
| Expressive default spatial | 0.38, 1.21, 0.22, 1.00 | 500 ms |
| Expressive slow spatial | 0.39, 1.29, 0.35, 0.98 | 650 ms |
| Expressive fast effects | 0.31, 0.94, 0.34, 1.00 | 150 ms |
| Expressive default effects | 0.34, 0.80, 0.34, 1.00 | 200 ms |
| Expressive slow effects | 0.34, 0.88, 0.34, 1.00 | 300 ms |
| Standard fast spatial | 0.27, 1.06, 0.18, 1.00 | 350 ms |
| Standard default spatial | 0.27, 1.06, 0.18, 1.00 | 500 ms |
| Standard slow spatial | 0.27, 1.06, 0.18, 1.00 | 750 ms |
| Standard fast/default/slow effects | 0.31/0.34/0.34, 0.94/0.80/0.88, 0.34, 1.00 | 150/200/300 ms |

### 8.4 The animation toolkit (when to reach for what)

| Goal | API | Notes |
|---|---|---|
| Show/hide content | `AnimatedVisibility` | enter/exit specs from the motion scheme |
| Size change | `Modifier.animateContentSize()` | expanding cards, text reveal |
| Swap whole composables | `AnimatedContent` | state-driven content changes |
| Animate one value | `animate*AsState` (float, dp, color, offset, ...) | most common building block |
| Animate many values together | `Transition` / `updateTransition` | keeps properties in sync |
| Continuous/looping | `InfiniteTransition` | loading, ambient motion — use sparingly |
| Match an element across screens | `SharedTransitionLayout` + `Modifier.sharedElement()` | hero transitions (see 8.5) |
| Morph a container's bounds | `Modifier.sharedBounds()` | when start/end content differs |
| Shape morph | `MaterialShapes` + `Morph` + `toShape(progress)` | expressive containers, avatars |

### 8.5 Shared element transitions

Use for list → detail hero moments (e.g. an app icon or container card expanding into the
detail screen). The high-level pieces:

- `SharedTransitionLayout` — outer scope that provides `SharedTransitionScope`.
- `Modifier.sharedElement(state, animatedVisibilityScope)` — element that is visually
  *identical* between screens.
- `Modifier.sharedBounds(...)` — bounds container when the content differs.
- `rememberSharedContentState(key = "...")` — unique key matching the two ends.

```kotlin
SharedTransitionLayout {
    AnimatedContent(showDetails, label = "detail") { detail ->
        if (!detail) {
            ListItem(
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedVisibilityScope = this@AnimatedContent
            )
        } else {
            DetailItem(...)
        }
    }
}
```

Gotchas: modifier order matters (put `sharedElement` before size/clip you do not want shared);
the two ends **must** use the same key; wrap in `AnimatedContent`/`AnimatedVisibility`/`NavHost`
so the transition is driven for you.

### 8.6 Shape morphing

The M3 shape library exposes named `RoundedPolygon` shapes on `MaterialShapes` and a `Morph`
utility that interpolates between any two, driven by a progress value. Animate the progress
with an expressive spatial spring so the morph inherits the product's motion feel.

```kotlin
val morph = remember { Morph(MaterialShapes.Cookie9Sided, MaterialShapes.Circle) }
val progress by animateFloatAsState(
    targetValue = if (selected) 1f else 0f,
    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
    label = "shapeMorph"
)
Box(Modifier.size(48.dp).clip(morph.toShape(progress)))
```

### 8.7 Rules, accessibility, and anti-patterns

- **Never 0 ms.** Every primary navigation / state change needs a visible transition
  (AEP `AEP-PM-GAA`). Snapping to a new screen loses user context (`AEP-PM-GAC`).
- **Interruptible.** Prefer springs over fixed-duration easing so a gesture can retarget the
  animation mid-flight; this is the core reason M3 moved to physics.
- **Respect reduced motion.** Compose's `animate*AsState` and the motion-scheme specs honor
  the system animator duration scale; when a user disables animations the duration collapses.
  Do not bypass this with manually driven `withFrameNanos` loops for decorative motion.
- **Motion must not gate input.** Users should never wait for an animation before they can act.
- **Don't over-animate.** One or two hero moments per product. Animating everything reads as
  noise and slows task completion.
- **Don't animate essential affordances away.** A spring that overshoots a touch target can
  move it under the finger; keep hit areas stable while visuals move.
- **Don't hardcode `spring(dampingRatio = ..., stiffness = ...)`** in new code. If a token does
  not fit, add a named spec to the shared expressive component layer instead of scattering
  magic numbers.

### 8.8 Migration checklist for TrueHub

1. `motionScheme = MotionScheme.expressive()` — **done** in `AdaptiveTheme.kt`.
2. Replace local `spring(stiffness = ...)` in screens with `MaterialTheme.motionScheme` tokens.
3. Route `AnimatedVisibility` / `AnimatedContent` enter/exit specs through the scheme.
4. Add shared-element transitions for the app/container list → detail hero moment.
5. Reserve shape morph for status icons, avatars, and loading states.
6. Keep one identifiable expressive motion budget per screen; do not add motion per component.

---

## 9. Designing for adaptive screens

Source: `developer.android.com/develop/adaptive-apps/*`, `design/ui/mobile/.../adapt-layout`,
`guide/topics/large-screens/*`.

### 9.1 Window size classes are the unit of adaptation

Adapt to the **app window**, not the device. Window size classes classify the current window
as compact / medium / expanded / large / extra-large (width) and compact / medium / expanded
(height). Width is the primary signal; height matters for landscape phones and open flip
phones, where width is medium but height is compact and a two-pane layout is impractical.

| Width class | Breakpoint | Typical |
|---|---|---|
| Compact | < 600dp | phones portrait (~99.96%) |
| Medium | 600–840dp | tablets/foldables portrait |
| Expanded | 840–1200dp | tablets landscape |
| Large | 1200–1600dp | large tablets |
| Extra-large | ≥ 1600dp | desktop |

Key implications:

- A physical device does **not** guarantee a size class. Split-screen, desktop windowing,
  fold/unfold, and rotation all change the available window while the app runs.
- Never branch on `isTablet` / physical screen size. Branch on the window size class.
- The size class is **dynamic**: re-evaluate and re-layout when it changes.

### 9.2 Read the class from the Material 3 Adaptive library

The current recommended API is `currentWindowAdaptiveInfo()` from
`androidx.compose.material3.adaptive:adaptive`, not the older
`calculateWindowSizeClass(activity)`.

```kotlin
@Composable
fun MyApp(
    windowSizeClass: WindowSizeClass =
        currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true).windowSizeClass
) {
    val showTopBar = windowSizeClass
        .isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)
    MyScreen(showTopBar = showTopBar)
}
```

**Current state of this repo.** `ui/utils/AdaptiveLayoutHelper.kt` uses the older
`material3-window-size-class` artifact and `calculateWindowSizeClass(activity)`. It works but
is deprecated in favour of the adaptive library and only models compact/medium/expanded. It is
re-created per call site and only exposes width. Migration path:

1. Add the adaptive dependencies: `androidx.compose.material3.adaptive:adaptive`,
   `:adaptive-layout`, `:adaptive-navigation`.
2. Replace `AdaptiveLayoutHelper.getWindowSizeClass()` with `currentWindowAdaptiveInfo()`.
3. Delete `material3-window-size-class` once no call sites remain.
4. Keep the helper's ergonomic wrappers (`getColumnCount`, `getContentPadding`) as a thin
   facade over the new API so screen code does not churn — this respects the existing flow
   while removing the deprecated dependency.

### 9.3 Layered architecture: decide once at the top, pass state down

Compute the size class at the top of the app (content-level) and pass the **result** down as
data. Do not read global window metrics inside leaf composables — it makes them
non-reusable, wrong under padding/insets, and hard to test. When a component must adapt to its
own slot (e.g. a card that shows a description only when wide), use `BoxWithConstraints` and
branch on the constraints it was actually given.

Always pass a component enough content for every size (e.g. always pass `description` to a
`Card`), even if it is unused at the current size. This keeps layout changes stateless and
preserves user state across resize/fold/orientation changes.

### 9.4 Canonical layouts

Use the Material 3 Adaptive scaffolds rather than hand-rolling panes:

| Canonical layout | Use for | API |
|---|---|---|
| List-detail | list + selected item detail | `ListDetailPaneScaffold` |
| Supporting pane | primary content + tools/options/settings | `SupportingPaneScaffold` |
| Feed | content-rich grid, featured + regular items | grid composition |
| Navigation | bottom bar ↔ rail ↔ drawer by size | `NavigationSuiteScaffold` |

`NavigationSuiteScaffold` is the clean way to satisfy "bottom navigation on compact, rail on
medium/expanded" without duplicating navigation graphs. Panes: compact = one pane, medium =
1–2, expanded/large = multi-pane.

### 9.5 Responsive patterns

When space increases, choose deliberately:

- **Reflow / reposition** — move a FAB to the nav rail, reflow cards vertically → horizontally.
- **Reveal** — show a detail pane, extra metadata, or a side sheet that was hidden.
- **Presentation change** — a bottom sheet becomes a side sheet; a component switches to a
  functionally equivalent larger-screen variant.
- **Constrain** — cap content width (≈ 60-character line length) instead of stretching.

### 9.6 Do / Don't

**Do**
- Base layout decisions on window size class.
- Organize content into panes; use list-detail / supporting-pane scaffolds.
- Set a max content width; keep buttons and inputs at a usable fixed width.
- Swap navigation components at breakpoints (bar ↔ rail).
- Support resizable windows, split-screen, and all orientations.
- Test at compact / medium / expanded breakpoints and in resizable emulators.

**Don't**
- Don't lock orientation (`screenOrientation`) — it causes letterboxing and breaks on large screens.
- Don't set `resizeableActivity="false"` or `min/maxAspectRatio`.
- Don't stretch UI with `fillMaxWidth()` without considering the resulting line length / target size.
- Don't use deprecated `Display.getSize()` / `getMetrics()` / `getRealSize()` APIs.
- Don't design a separate screen per form factor; adapt at the component/pane level.

### 9.7 Testing adaptive layouts

- Android Studio resizable virtual device + foldable/posture emulators.
- Test the dynamic transitions (rotate, resize, fold) — not just the static end states.
- Verify state is preserved across a size-class change.
- Check large and extra-large widths explicitly if the app targets desktop/ChromeOS.

---

## 10. Accessibility on Android

Source: `developer.android.com/guide/topics/ui/accessibility/*`,
`developer.android.com/develop/ui/compose/accessibility/*`.

Accessibility is a first-class HCI requirement, not a compliance afterthought. Material 3
components ship accessible defaults; the work is preserving them and filling the gaps.

### 10.1 Visual: contrast, size, and color independence

- Text < 18sp (or bold < 14sp): contrast ratio **≥ 4.5:1**. All other text: **≥ 3:1**.
- Interactive targets: **≥ 48×48dp** focusable area. `Button`, `IconButton`, `ListItem`
  enforce this; custom `clickable` elements must set it explicitly
  (`Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)` or a larger invisible hit area).
- For interactive icons that are smaller than 48dp, keep the icon at 24dp but expand the
  touch target to 48dp — do not enlarge the icon itself.
- Never use color alone to convey state/meaning; pair it with shape, text, icon, or pattern.
- Respect user font scaling: use `sp` for text and avoid fixed-height text containers that
  clip at large font scales.

### 10.2 Labels and descriptions

- `contentDescription` describes purpose/result, not appearance.
- `Text` needs no description — TalkBack reads it. Passing one duplicates it.
- Use `Role` (`Role.Button`, `Role.Switch`, …) to expose the element type so it is announced
  as interactive.
- Don't repeat the type in the label: "Submit", not "Submit button".
- Every description must be unique per element; list items should reflect their own content.
- Purely decorative `Icon`/`Image` → `contentDescription = null`.

### 10.3 Semantics: merging, clearing, hiding

| Situation | API |
|---|---|
| Related children form one logical unit (e.g. song metadata) | `Modifier.semantics(mergeDescendants = true) {}` |
| Interactive parent already merges (buttons, `clickable`, `toggleable`, `ListItem`) | built-in — do nothing |
| Complex custom component needs one clean semantics node | `clearAndSetSemantics { ... }` |
| Purely decorative / redundant but still testable | `hideFromAccessibility()` |

Notes: `clearAndSetSemantics` wipes the element *and its descendants* and is order-sensitive
(it clears everything after it in the modifier chain) — use sparingly. Prefer merging when
children belong together; prefer `hideFromAccessibility` when content is decorative but you
still want it addressable in tests.

### 10.4 Rich semantics for non-trivial UI

- Headings: `Modifier.semantics { heading() }` enables heading-based navigation.
- Live updates (alerts, toasts, status): `liveRegion = LiveRegionMode.Polite` (or `Assertive`).
- Modals/sheets: `paneTitle` so the window/pane change is announced.
- Errors: `error("...")` with actionable message text.
- Progress: `progressBarRangeInfo` communicates value, range, and step.
- Collections: `collectionInfo` / `collectionItemInfo` so lists announce "item i of n".
- Custom gestures: `customActions = listOf(CustomAccessibilityAction(...))`.
- Toggle state: `stateDescription` ("On"/"Off"/"Checked") plus correct `Role`.
- Custom components: mirror a comparable Material component's semantics, or use Foundation
  modifiers (`clickable`, `toggleable`, `triStateToggleable`) that populate semantics for you.

### 10.5 Traversal and focus

- Structure the UI hierarchy logically; accessibility services traverse it in order.
- If the default reading order is wrong, adjust it explicitly.
- For keyboard/D-pad, control focus order and ensure interactive drawables are focusable and
  show a visible focus indicator (large-screen Tier 2 requirement).

### 10.6 Testing and tooling

- **Accessibility Scanner** app — automated checks (contrast, target size, labels).
- **TalkBack** — manual pass through the primary flows with the screen reader on.
- **Android Lint** — catches common issues (missing descriptions, small targets).
- **Compose UI tests** — assert on the semantics tree (labels, roles, actions, state).
- Switch to a large display size / font scale and verify nothing clips or overlaps.
- Android 16 (API 36): note the platform now ignores orientation/aspect-ratio/resizability
  restrictions on windows with smallest width ≥ 600dp — adaptive + accessible layouts are the
  supported path.

### 10.7 Audit targets for TrueHub

~88 files already reference `contentDescription`/`semantics`, which is a good base. Focus an
audit on:

1. Custom interactive composables under `ui/components` — confirm 48dp targets and roles.
2. Decorative icons accidentally carrying a non-null `contentDescription`.
3. Custom status chips / gauges (`HalfCircleGauge`, status dots) — ensure state is conveyed by
   more than color and exposes `stateDescription`.
4. Sheets, toasts, and banners — `paneTitle` / `liveRegion`.
5. Dynamic content (container/VM status) — `liveRegion` so changes are announced.
6. Run Accessibility Scanner over Home, Apps, Containers, VM, and Settings.
