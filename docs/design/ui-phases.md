# UI Phases — Floating Navigation Bar, Search, Collapsing Headers

Execution plan for the floating bottom-bar workstreams. Each phase is independently testable
and committable. References `docs/design/hci-android-guidelines.md` and
`docs/design/m3-expressive-research.md`.

## Phase 0 — Shared bottom-bar metrics
- [x] `FloatingNavMetrics` (bar height, margins, item/search sizes) in `ui/components/`.
- [x] `floatingNavBottomClearance()` = bar height + margins + system navigation-bar inset.
- Single source of truth for all clearance/FAB fixes below.

## Phase 1 — Bottom-bar polish & clearance
- [x] **1c** tighter bar: smaller height, tighter item/pill padding, side margins, search button.
- [x] **1a** FAB clearance: VMs, Containers, Apps lifted above the bar.
- [x] **1b** bottom content padding: VMs/Containers/Apps lists+grids, `ServiceListSkeleton`,
      homepage content + `HomeSkeleton`, marketplace, instance config, system update.

## Phase 2 — Search integration
- [x] **2e-1** `BackHandler` in the search overlay (back closes search, does not exit the app);
      closing no longer force-navigates to Home, so it returns to where search was triggered.
- [x] **2e-2** removed `onSearchClick` + the search `ExpressiveIconButton` from
      `UnifiedScreenHeader`; argument removed end-to-end (screens + nav graph).
- [x] **2d** expanded the search registries with the newer pages (app images, docker images,
      iX volumes, cloud sync, create share, boot pool/environments, global virt, files, app
      logging/update, create container/VM; and editable/creator instance settings screens).

## Phase 3 — Collapsing header rollout — **SKIPPED (reverted)**
Tried twice: in-flow shrink and overlay (content under the floating minimal back). Neither matched the
intended look, so the component and the homepage-details conversion were reverted. `UnifiedScreenHeader`
stays as the only header. Revisit only if a concrete design is specified.

- [x] ~~3a reusable `CollapsingScreenHeader`~~ — reverted
- [x] ~~3b `rememberHeaderCollapsed` helpers~~ — reverted
- [ ] 3c module rollout — not started
- [ ] 3d ad-hoc cleanup — not started

## Decisions
- Clearance model: reserve bar height as bottom padding on scrollables (content still visually
  passes under the bar while scrolling).
- `UnifiedScreenHeader` loses `onSearchClick` entirely; call sites updated.
