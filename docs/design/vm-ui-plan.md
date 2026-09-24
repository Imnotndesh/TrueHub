# VM UI Plan — Porting `vm.*` to the Expressive UI

Status: **Phases 0–5 implemented** (`:app:compileGithubDebugKotlin` green).
49 of 54 `VmService` wrappers are wired. Verified against the live server
(v25.10.5).

Implemented:
- `ContainerJobTracker` generalised to `VirtualizationJobTracker` (`VirtualizationOperation`,
  `virtualizationOperationLabel`) with VM operation labels; containers unchanged.
- `VmVisuals.kt`: VM shape + status tone/icon/label helpers.
- `VmsScreen` restyled to mirror `ContainersScreen` (shared cards/status/filter, multi-select +
  bulk Start/Stop/Resume/Suspend/Delete, shared tracker progress) + Create VM FAB.
- `VmDetailViewModel` (cache-seeded, fresh load, lifecycle actions).
- `VmInfoPane` / `VmInfoScreen` restyled to mirror the container detail (hero actions, shared
  sections, gradient pane header, entry cards for Devices/Edit, danger-zone delete).
- `VmFormViewModel` + `VmFormScreen` for **create and edit** (Identity/Resources/Firmware & CPU/
  Behaviour) using `bootloader_options`, `bootloader_ovmf_choices`, `cpu_model_choices`,
  `guest_architecture_and_machine_choices`, `maximum_supported_vcpus`, `get_available_memory`,
  `supports_virtualization`. Routes `vm_create`, `vm_edit/{vmId}`.
- `VmDevicesViewModel` + `VmDevicesScreen` (list, delete, import disk image) and
  `VmDeviceAddViewModel` + `VmDeviceAddScreen` (all device types + every `vm.device.*_choices`
  endpoint, `iommu_enabled`, `port_wizard`, `random_mac`, create/update/get_instance). Routes
  `vm_devices/{vmId}`, `vm_devices/{vmId}/add`.
- Fixed mis-typed `VmService` wrappers (`maximum_supported_vcpus`→Int, `supports_virtualization`
  →Boolean, `get_available_memory`→Long, `guest_architecture_and_machine_choices`→`Map<String,
  List<String>>`, `random_mac`→String, `flags`→`Map<String,Boolean>`, `virtualization_details`
  →`Map<String,Any?>`, `device.iommu_enabled`→Boolean, `device.convert`→Int, `export/import
  _disk_image`→Int, `port_wizard`→`Map<String,Int>`, passthrough choices→`Map<String,
  PassthroughInfo>`).

Companion: `docs/design/vm-api-research.md`.

Added in Phases 4–5:
- Console: `VmConsoleScreen` (in-app WebView, self-signed tolerant) via `get_display_web_uri`,
  plus `get_console` / `get_display_devices` in diagnostics. Route `vm_console/{vmId}`.
- Clone: `vm.clone` from the detail (name dialog).
- Export boot image: `vm.export_disk_image` (format / dataset-dir picker / zvol picker) from the
  detail, job-tracked; entry card above the Danger Zone.
- Diagnostics section: `get_memory_usage`, `get_vm_memory_info`, `get_vmemory_in_use`, `flags`,
  `virtualization_details`, `log_file_path`, `get_display_devices`, `get_console`, `vm.status`.
- Devices: `vm.device.convert` (image ↔ ZFS volume, job) entry card; `vm.device.virtual_size`
  ("Calculate virtual size") on RAW devices.
- Log download: `vm.log_file_download` is surfaced via **`core.download`** and saved to the
  Android device through the Storage Access Framework (`CreateDocument`) with a suggested
  `vm-<name>.log` filename; success/failure shown as a toast.

Intentionally not wired (redundant/legacy):
- `vm.device` (base namespace method, no feature)
- `vm.device.passthrough_device` / `vm.device.usb_passthrough_device` (legacy helpers; superseded by
  `vm.device.create` with the PCI/USB device types)
Goal: surface **all 53 `vm.*` calls** as UI, using the same expressive design language and shared
plumbing as Containers so the two features feel like one product. VMs stay on the `vm.*` namespace
(`virt.instance.create` is container-only in v25.10.5).

## 1. Principles

1. Reuse, don't copy. The container work produced the canonical components; VMs consume them.
2. One job tracker, one status/shape helper set, parameterised by instance kind.
3. Identical screen shapes: list → detail → edit / devices / export; forms use bottom
   Cancel/Save bars and the animated `JobProgressView`.
4. Type-specific accents only where meaningful (VM `MaterialShapes.ClamShell`, VM icon, VM status
   words like `SUSPENDED`).
5. Preserve existing navigation/architecture; add routes alongside the container ones.

## 2. Shared infrastructure (decisions)

**D1 — Generalise the job tracker.** Replace `ContainerJobTracker` with
`VirtualizationJobTracker` (same file or `data/helpers/VirtualizationJobTracker.kt`) keyed by
`String` instance id, with `operation` labels covering both:
`STARTING/STOPPING/RESTARTING/DELETING` (containers) and the VM equivalents
`STARTING/STOPPING/RESTARTING/SUSPENDING/RESUMING/POWERING_OFF/CLONING/EXPORTING/IMPORTING/UPLOADING`.
`ContainerJobTracker` becomes a typealias or thin wrapper so containers are untouched.
- Pros: one poller, one progress surface, VM jobs visible on the list even when triggered from the
  detail screen (same requirement the containers had).
- Alternative: a separate `VmJobTracker` (duplication; rejected).

**D2 — Generalise status/shape helpers.** Extend `containers/ContainerVisuals.kt` into a small
`VirtualizationVisuals` API:
- `instanceShape(kind, type)`, `instanceTypeIcon`, `instanceTypeContainerColor/ContentColor`
- `instanceStatusTone(status)` and `instanceStatusIcon(status)` accepting both `Virt.Status` and
  `Vm.VmStatus`/string. Prefer a `VirtualInstanceKind` enum + string status to avoid two enums.

**D3 — Reuse the pickers and forms.** `ServerPathPickerSheet` (files mode for ISO), 
`DatasetPickerSheet` (zvol/dataset selection), `JobProgressView`, `Expressive*` components,
`PullToRefreshContent` (M3 loading indicator), bottom Cancel/Save bars, `_upload` ISO uploader.

**D4 — Console.** `vm.get_display_web_uri` returns a noVNC/SPICE web URI. Present an explicit
"Open console" action on the detail screen; render in an in-app WebView screen (preferred for
seamlessness) with an "open externally" fallback. Confirm during implementation.

## 3. Screens

| Screen | Kind | Mirrors |
|---|---|---|
| `VmsScreen` (restyle) | list + split pane | `ContainersScreen` |
| `VmInfoScreen` (restyle) | full detail | `ContainerInfoScreen` |
| `VmInfoPane` (restyle) | split-pane detail | `ContainerInfoPane` |
| `VmCreateScreen` (new) | create form | `ContainerCreateScreen` |
| `VmEditScreen` (new) | edit form | `ContainerEditScreen` |
| `VmDevicesScreen` (new) | device list | `ContainerDevicesScreen` |
| `VmDeviceAddScreen` (new) | device add/edit | `ContainerDeviceAddScreen` |
| `VmConsoleScreen` (new) | WebView console | new |
| Export/import | dialogs/sheets | container export/import sheets |
| Diagnostics (optional) | info sheet | new |

## 4. Full API → UI matrix

**Lifecycle**
| Call | Where |
|---|---|
| `vm.query` | `VmsScreen` list + `VmsScreenViewModel` |
| `vm.get_instance` | `VmInfoScreen`/`VmInfoPane` fresh load (seed from `AppCache.cachedVms`) |
| `vm.create` | `VmCreateScreen` |
| `vm.update` | `VmEditScreen` |
| `vm.delete` | `VmInfoScreen` Danger Zone (options: delete zvols, force) |
| `vm.start` | list card + detail hero (overcommit option) |
| `vm.stop` | list card + detail hero (force / force-after-timeout options) |
| `vm.restart` | list card + detail hero |
| `vm.suspend` | list card + detail hero |
| `vm.resume` | list card + detail hero |
| `vm.poweroff` | detail hero (danger, confirms) |
| `vm.clone` | detail entry card (name prompt) + list bulk clone |
| `vm.status` | detail state refresh (or fold into `get_instance`) |

**Devices**
| Call | Where |
|---|---|
| `vm.device.query` | `VmDevicesScreen` |
| `vm.device.create` | `VmDeviceAddScreen` |
| `vm.device.update` | `VmDeviceAddScreen` (edit mode) |
| `vm.device.delete` | `VmDevicesScreen` row delete (confirm) |
| `vm.device.get_instance` | `VmDeviceAddScreen` prefill |
| `vm.device.convert` | device edit: change disk `iotype` |
| `vm.device.virtual_size` | disk-device add: compute size from path/zvol |
| `vm.device.iommu_enabled` | device add: gate PCI/USB passthrough + hint |
| `vm.device.passthrough_device` | add PCI passthrough device |
| `vm.device.usb_passthrough_device` | add USB passthrough device |
| disk/nic/iotype/bind/usb-controller/usb-passthrough/passthrough-device/pptdev choices | device-add fields (choice pickers/sheets) |

**Disk images**
| Call | Where |
|---|---|
| `vm.export_disk_image` | detail "Export boot image" (reuse container export sheet: format / dataset-dir picker / zvol picker), job-tracked |
| `vm.import_disk_image` | `VmDevicesScreen` "Import disk image" (reuse container import sheet), job-tracked |

**Console / display**
| Call | Where |
|---|---|
| `vm.get_display_web_uri` | detail "Open console" → `VmConsoleScreen` (WebView) |
| `vm.get_console` | detail "Console" info row + console screen |
| `vm.get_display_devices` | devices screen DISPLAY-type rendering / count |
| `vm.port_wizard` | display-device add: suggest free port |
| `vm.resolution_choices` | display-device add: resolution picker |

**Create/edit choices**
| Call | Where |
|---|---|
| `vm.bootloader_options` | create/edit: bootloader picker (UEFI / UEFI_CSM) |
| `vm.bootloader_ovmf_choices` | create/edit: OVMF picker |
| `vm.cpu_model_choices` | create/edit: CPU model picker (CUSTOM mode) |
| `vm.guest_architecture_and_machine_choices` | create/edit: arch/machine picker |
| `vm.random_mac` | device add (NIC): generate MAC |
| `vm.maximum_supported_vcpus` | create/edit: vCPU upper bound/hint |

**Metrics / diagnostics**
| Call | Where |
|---|---|
| `vm.get_memory_usage` | detail "Resource usage" card |
| `vm.get_vm_memory_info` | detail resource card (usage/total) |
| `vm.get_vmemory_in_use` | detail resource card / create memory hint |
| `vm.get_available_memory` | create: max memory hint + validation |
| `vm.supports_virtualization` | create gating + settings/diagnostics |
| `vm.virtualization_details` | "Virtualization details" diagnostics sheet |
| `vm.flags` | diagnostics sheet |
| `vm.log_file_path` | detail diagnostics: log path row |
| `vm.log_file_download` | detail diagnostics: download logs (reuse downloader) |

## 5. Per-screen plan

### 5.1 `VmsScreen` (restyle)
- Replace private card/action/status/chip/filter copies with the shared expressive components,
  matching `ContainersScreen`: 28dp surface + border cards, `MaterialShapes.ClamShell` type badge,
  `ExpressiveStatusChip`/`Dot` with a VM `StatusTone` mapper (RUNNING/UAS…), shared
  `ExpressiveFilterBar`, `MaterialTheme.motionScheme` animations.
- Action rows: primary Start/Stop (or Resume/Suspend when suspended), Info, Restart; keep the
  container card interaction (tap → detail/pane, long-press → multi-select).
- Multi-select + bulk actions (Start/Stop/Suspend/Resume/Delete/Clone), FAB menu, `BackHandler`.
- Job/progress from `VirtualizationJobTracker` with operation-aware labels.
- Keep split-pane for medium/expanded.

### 5.2 `VmInfoScreen` + `VmInfoPane`
- Same chrome as containers: `MinimalBackHeader` overlay, scrolling hero card + pane gradient
  header, shared `ExpressiveSection`/`ExpressiveInfoCard`/`InfoRow`, pane `contentTopPadding`.
- Hero: shape badge, name, status chip, `State` action group —
  Start/Stop/Restart + Suspend/Resume + Power off — with progress while a job runs.
- Entry cards: **Devices**, **Edit configuration**, **Open console**, **Clone**, **Export boot
  image**, plus a **Resource usage** card (memory usage / vmemory in use / memory info).
- Danger Zone: **Delete** (options: delete zvols, force) → confirm; job-tracked, navigate back on
  success.
- Diagnostics section (collapsible): log path, download logs, flags, virtualization details.

### 5.3 `VmCreateScreen` (new)
- Sections mirroring containers: **Identity** (name, description), **Resources** (memory MB,
  vCPUs, cores, threads, min memory, cpuset/nodeset), **Firmware & CPU** (bootloader + OVMF from
  choices, CPU mode/model, arch/machine from choices, secure boot, TPM, Hyper-V, topology, pin
  vCPUs), **Behaviour** (autostart, time zone, shutdown timeout, hide-from-MSR, ensure display,
  suspend on snapshot, command-line args), **First device** (create a disk: create_zvol + size or
  existing zvol; optional CDROM from ISO; NIC from `nic_attach`).
- Memory hint from `vm.get_available_memory`; vCPU max from `vm.maximum_supported_vcpus`;
  gating/hint from `vm.supports_virtualization`.
- Bottom **Cancel / Create** bar; `JobProgressView` while creating (operation `CREATING`).

### 5.4 `VmEditScreen` (new)
- `vm.get_instance` prefill; same grouped sections as create (minus immutable bootloader device
  nuances); bottom **Cancel / Save**; `vm.update` job-tracked.

### 5.5 `VmDevicesScreen` (new)
- Mirrors `ContainerDevicesScreen`: entry cards **Import disk image** + **Add device**, rows for
  each `vm.device.query` entry (type, order, summary), edit (→ device add in edit mode) and delete
  (confirm). Render `DISPLAY` devices and their ports; PCI/USB passthrough rows.

### 5.6 `VmDeviceAddScreen` (new)
- Device-type selector (`FilterChip`/`ExpressiveFilterBar` over `DISK, CDROM, RAW, NIC, DISPLAY,
  PCI, USB`) with type-specific fields and the corresponding choice endpoints:
  - DISK: existing zvol (`vm.device.disk_choices`) or create zvol (`zvol_name`, `zvol_volsize`,
    size via `vm.device.virtual_size`), `iotype` (`vm.device.iotype_choices`), serial, sectors.
  - CDROM: ISO path (`ServerPathPickerSheet` files mode) or upload from device (reuse `_upload`).
  - NIC: `nic_attach` (`vm.device.nic_attach_choices`), NIC type, MAC (`vm.random_mac`).
  - DISPLAY: type, resolution (`vm.resolution_choices`), port (`vm.port_wizard`), bind, password.
  - PCI: `pptdev` (`vm.device.passthrough_device_choices`/`pptdev_choices`) + IOMMU gate.
  - USB: controller (`vm.device.usb_controller_choices`) + device (`vm.device.usb_passthrough_choices`).
  - Boot `order` field.
- Bottom **Cancel / Add device**; `JobProgressView` while saving (indeterminate where the call is
  synchronous, determinate where it returns a job).

### 5.7 `VmConsoleScreen` (new)
- In-app WebView for `vm.get_display_web_uri`, toolbar with reload + open-external, error state,
  and the `vm.get_console` details.

## 6. Phases

- **Phase 0 — Shared infra**: generalise job tracker (D1) + visuals (D2); restyle `VmsScreen`.
- **Phase 1 — Detail**: restyle `VmInfoScreen`/`VmInfoPane`, hero actions, resource usage, danger
  zone delete/power-off/clone, export sheet, console entry.
- **Phase 2 — Create/Edit**: `VmCreateScreen`, `VmEditScreen`, all create/edit choice endpoints,
  memory/vCPU hints.
- **Phase 3 — Devices**: `VmDevicesScreen`, `VmDeviceAddScreen`, import disk image, all device
  choices, IOMMU/passthrough, boot order.
- **Phase 4 — Console + diagnostics**: `VmConsoleScreen`, log download, flags/details.
- **Phase 5 — Polish**: empty states, contrast, a11y, VM-specific decorative shapes, parity pass
  against the container screens.

## 7. Open questions / decisions requested

1. **Shared tracker**: generalise `ContainerJobTracker` → `VirtualizationJobTracker` (recommended)
   or add a separate `VmJobTracker`?
2. **Console**: in-app WebView (recommended for seamlessness) or open in the external browser?
3. **Devices screen**: one `VmDeviceAddScreen` with a type selector (recommended, mirrors
   containers) or separate screens per device type?
4. **Create scope**: include the first-disk/CDROM/NIC in the create form (recommended) or always
   create a bare VM and add devices afterwards?
5. **Diagnostics**: surface flags/virtualization details/logs in the detail screen (recommended,
   collapsible) or a separate diagnostics screen?
6. **`vm.status` polling**: fold into `vm.get_instance` refresh (recommended) or poll `vm.status`
   separately for live state?
