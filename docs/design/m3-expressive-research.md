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

**Important finding for this repo:** `MaterialTheme.Values.motionScheme` defaults to
`standard()`, and `TrueHubAppTheme` currently calls `MaterialTheme(...)` without a
`motionScheme`. So no component is using expressive springs today; screens compensate with
locally hardcoded `spring(stiffness = ...)` values. Adopting `MotionScheme.expressive()` at
the theme level (or via `MaterialExpressiveTheme`) is the single highest-leverage change for
a consistent expressive feel across cards, dialogs, menus, FABs, and panes.

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
