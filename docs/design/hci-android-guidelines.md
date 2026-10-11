# HCI Guidelines for Android Applications (with Material 3 Expressive)

Scope: a research digest on Human-Computer Interaction (HCI) principles as they apply to
Android apps, and how Material 3 Expressive operationalises them. Companion to
`m3-expressive-research.md`, which covers the M3 Expressive APIs in detail.

The goal is not to restate Material Design, but to explain *why* the guidelines exist so that
design and implementation decisions can be reasoned about instead of copied.

## Sources

- `https://developer.android.com/design/ui/mobile` — Android UI design hub (foundations,
  behaviors & patterns, accessibility)
- `https://developer.android.com/design/ui/mobile/guides/layout-and-content/adapt-layout`
- `https://developer.android.com/guide/topics/ui/accessibility/apps`
- `https://developer.android.com/guide/topics/ui/accessibility/principles`
- `https://developer.android.com/develop/ui/compose/accessibility` (API defaults, semantics,
  merging and clearing)
- `https://developer.android.com/develop/adaptive-apps`
- `https://developer.android.com/distribute/aep/aep-req-physics-based-motion`
- `https://m3.material.io/styles/motion/overview/how-it-works`
- `https://m3.material.io/blog/building-with-m3-expressive`
- `https://design.google/library/expressive-material-design-google-research`
- `https://developer.android.com/docs/quality-guidelines/build-for-billions/ui`
- Nielsen, *10 Usability Heuristics for User Interface Design*
- Stony Brook CSE 390 lecture notes on mobile HCI (traditional HCI guidelines)
- `https://www.itm-conferences.org/articles/itmconf/pdf/2025/07/itmconf_icsice2025_05003.pdf`
  (HCI design principles for mobile apps: ability-based design, gestures, personalization)

## 1. Why HCI matters for Android specifically

Android users differ from a desktop user in ways that change the design problem:

- **Touch, not a cursor.** Targets are big and fingers are imprecise (Fitts's Law). No hover,
  so affordances must be visible without a pointer.
- **Interruption is the norm.** Calls, notifications, and task switching mean state must be
  preserved and screens must be re-enterable at any point.
- **Context varies wildly.** One-handed use on a bus, two-handed on a couch, keyboard+mouse on
  a Chromebook, D-pad on a TV, 10-foot viewing on a tablet stand.
- **A huge ability range.** Vision, hearing, dexterity, and cognitive differences are common;
  accessibility is a mainstream requirement, not an edge case (Google's framing: design for the
  ~15% of the world with a disability, benefit everyone).

HCI supplies the predictive models (Fitts, Hick, Miller, Gestalt) and the evaluative heuristics
(Nielsen) that explain whether an interface will be usable before user testing confirms it.

## 2. Foundational HCI principles, mapped to Android

### 2.1 Nielsen's 10 heuristics → Android/M3 practice

| Heuristic | Android / M3 expression |
|---|---|
| Visibility of system status | Loading indicators, progress bars, ripple/press feedback, `liveRegion` announcements, sync/state chips |
| Match between system and real world | Real-world metaphors (trash, inbox, folders); plain-language labels; localised units/dates |
| User control and freedom | Back gesture + predictive back, cancel in dialogs, undo/snackbar actions, dismissible sheets |
| Consistency and standards | M3 components and tokens; platform nav patterns (top bar, nav bar/rail/drawer); `Role` semantics |
| Error prevention | Disable invalid actions, constrain input, confirm destructive actions, sensible defaults |
| Recognition rather than recall | Persistent labels + icons, selected-state highlighting, breadcrumbs/headers, don't hide actions in deep menus |
| Flexibility and efficiency of use | FABs and floating toolbars for frequent actions, shortcuts, adaptive panes for power users |
| Aesthetic and minimalist design | Containment and hierarchy; one primary action per surface; remove non-essential copy |
| Help users recognise, diagnose, recover from errors | Specific, action-oriented error text; state the fix, not the fault code; keep user input on failure |
| Help and documentation | Contextual empty states, inline hints, onboarding "starter" content, searchable settings |

### 2.2 Predictive models worth knowing

- **Fitts's Law** — time to hit a target grows with distance and shrinks with target size.
  Rationale for 48dp minimum touch targets and for placing primary actions in the lower reach
  zone rather than the top corners.
- **Hick's Law** — decision time grows with the number and complexity of choices. Rationale for
  one primary action per surface, progressive disclosure, and small, curated menus over giant ones.
- **Miller's chunking** — working memory holds a small number of items. Rationale for grouping
  related fields, chunked lists, and short linear task sequences.
- **Gestalt principles** (proximity, similarity, common region, continuity) — rationale for
  containment: whitespace grouping (implicit) or cards/dividers (explicit) to show what belongs
  together.
- **Jakob's Law** — users spend most time in *other* apps; match established patterns. Rationale
  for not reinventing navigation or gesture semantics.
- **Mental models / metaphors** — new UI should attach to what users already know; M3 Expressive
  research showed that breaking familiar patterns lowered usability even when it looked better.

### 2.3 Feedback and direct manipulation

Three feedback layers, all expected on Android:

1. **Articulatory** — confirms the physical action (ripple, haptic, focus ring).
2. **Semantic** — confirms the meaning/intent (item highlights as selected, button enters
   loading state).
3. **Status / progress** — communicates duration (determinate progress, loading indicator).

Direct manipulation means the object stays visible while acted on and the result is immediate.
This is the HCI justification for the M3 physics motion system: springs preserve momentum from
a gesture and can be interrupted/retargeted, so the UI feels attached to the finger.

### 2.4 Error handling that respects the user

- Prevent first (constrain inputs, disable impossible actions).
- Use specific, constructive wording: "Enter an email address", not "Invalid input".
- Take the blame: "We couldn't reach the server", not "Fatal error".
- Never destroy work silently; warn before irreversible actions and offer undo/recovery.
- Avoid preemptive/irreversible modes and provide clear escape routes (back, close, cancel).

### 2.5 Cognitive load and attention

- Minimise mental transformations: show what is needed where it is needed; use meaningful icons
  with labels.
- Preserve display inertia — keep layout stable between related screens so users don't re-scan.
- Use colour sparingly and semantically; more saturated colour draws attention, so reserve it
  for the active/primary element.
- Don't overwhelm: progressive disclosure beats a dense wall of controls.

## 3. Mobile-specific HCI constraints

- **Thumb reach zones.** Primary actions belong in the lower/centre reachable zone; destructive
  or rare actions can live higher. The M3 floating toolbar and FAB menus exist for frequent
  actions near the thumb.
- **One-handed posture.** Avoid requiring two simultaneous precise touches for core flows.
- **Orientation and posture.** Support portrait and landscape; handle fold/unfold and tabletop
  posture. Locking orientation is an HCI failure on large screens (letterboxing).
- **Interruptibility.** Persist state; make every screen resumable. Compose `rememberSaveable`
  and hoisted state are the implementation counterpart.
- **Variable input.** Support touch, keyboard, mouse/trackpad, and D-pad; show a focus indicator.
- **Network/attention variance.** Optimistic UI, retry, and offline states are usability features,
  not just technical ones.
- **Localisation.** Allow for text expansion, RTL, and varied number/date formats; never bake
  text into images.

## 4. How M3 Expressive maps to HCI goals

M3 Expressive is best understood as an HCI optimisation layered on familiar structure:

| Expressive tactic | HCI purpose |
|---|---|
| Variety of shapes | Pre-attentive grouping and salience — draws attention to key elements |
| Rich, layered colour | Hierarchy and action prioritisation (with contrast maintained for legibility) |
| Emphasised typography | Scannability and editorial hierarchy for headlines/selected states |
| Containment | Gestalt grouping; clarifies what belongs together |
| Fluid, physical motion | Feedback + direct manipulation; explains spatial relationships |
| Component flexibility | Fitts/Hick-friendly adaptation to size and input |
| Hero moments | Controlled attention budget — one or two per product |

Research-backed outcomes reported for expressive designs: higher ratings for playful/energetic/
friendly, key elements spotted up to 4× faster, and better perceived usability for users with
varied movement/visual abilities. The critical caveat from the same research: expression applied
by removing labels or restructuring navigation **hurt** usability. Expression must decorate and
clarify familiar structure, never replace it.

## 5. Evaluation methods

- **Heuristic evaluation** — walk the app against Nielsen's 10 heuristics; cheap, catches most
  obvious issues before testing.
- **Cognitive walkthrough** — for each primary task, ask: will the user know what to do, notice
  the control, interpret the feedback, and understand the result?
- **Usability testing** — 5 users on core flows; measure task success, time, and errors.
- **Accessibility review** — Accessibility Scanner + TalkBack pass + large-font pass.
- **Fitts check** — are primary actions reachable one-handed, and are all targets ≥ 48dp?
- **Hick check** — does any screen present an excessive number of equally-weighted choices?
- **Motion review** — is every primary transition visible, interruptible, and non-blocking?

## 6. Applied checklist for TrueHub

Structure (HCI)
- [ ] Every primary flow has one obvious primary action in a reachable zone.
- [ ] Persistent labels accompany icons for all non-universal actions.
- [ ] Destructive actions confirm, then offer undo where feasible.
- [ ] Empty/loading/offline states all explain what is happening and what to do next.
- [ ] Navigation uses standard patterns; no custom gesture-only affordances.

Feedback & motion
- [ ] All primary navigation/state changes animate (no 0 ms transitions).
- [ ] Motion uses `MaterialTheme.motionScheme` tokens, not hardcoded springs.
- [ ] Progress is shown for operations that can take time; failures are recoverable.
- [ ] Touch feedback (ripple/press state) on every interactive element.

Accessibility
- [ ] All interactive targets ≥ 48×48dp.
- [ ] Text contrast ≥ 4.5:1 (small) / ≥ 3:1 (large).
- [ ] State is never conveyed by colour alone.
- [ ] `contentDescription` present and meaningful where needed; `null` for decorative icons.
- [ ] Sheets/toasts/status changes announce via `paneTitle` / `liveRegion`.
- [ ] Works at large system font scale and with TalkBack.

Adaptive
- [ ] Layout based on window size class, not device type.
- [ ] Orientation not locked; resizable/split-screen supported.
- [ ] Content constrained to a readable max width on large screens.
- [ ] Navigation adapts (bar ↔ rail) and content uses panes where it helps.

## 7. Further reading

- Material 3 Expressive research: `design.google/library/expressive-material-design-google-research`
- Android accessibility principles: `developer.android.com/guide/topics/ui/accessibility/principles`
- Adaptive apps hub: `developer.android.com/develop/adaptive-apps`
- AEP physics-based motion requirement: `developer.android.com/distribute/aep/aep-req-physics-based-motion`
- Practical M3 API details for this project: `docs/design/m3-expressive-research.md`
