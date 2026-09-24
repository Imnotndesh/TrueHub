# TrueNAS VM (`vm.*`) API & UI Research

Research for porting the VM surface into the app using the same expressive UI language as the
Containers work. Sources: live server (TrueNAS SCALE v25.10.5) via `core.get_methods` + the REST
OpenAPI, the server docs under `/api/docs/current`, and the current app code.

## 1. Which API do VMs use?

- `virt.instance.create` in v25.10.5 is **container-only** (`instance_type` is `const "CONTAINER"`,
  enum `["CONTAINER"]`); `virt.instance.query` returned only the container. (Image choices list
  `instance_types: ["VM","CONTAINER"]`, but VM creation is not exposed through `virt.instance` here.)
- Therefore the VM UI must stay on the legacy **`vm.*`** namespace, which the app already wraps in
  `VmService`. Containers stay on `virt.instance.*`.

Consequence: the two features share the *design language and plumbing* but not the transport
namespace. The plan focuses on making the shared pieces genuinely shared.

## 2. Server API surface — 53 `vm.*` methods

Grouped:

**VM lifecycle**
`vm.query`, `vm.get_instance`, `vm.create`, `vm.update`, `vm.delete`, `vm.start`, `vm.stop`,
`vm.restart`, `vm.suspend`, `vm.resume`, `vm.poweroff`, `vm.clone`, `vm.status`

**Devices**
`vm.device.query`, `vm.device.create`, `vm.device.update`, `vm.device.delete`,
`vm.device.get_instance`, `vm.device.convert`, `vm.device.virtual_size`,
`vm.device.iommu_enabled`, `vm.device.passthrough_device`, `vm.device.usb_passthrough_device`
plus choice endpoints: `vm.device.disk_choices`, `vm.device.nic_attach_choices`,
`vm.device.iotype_choices`, `vm.device.bind_choices`, `vm.device.usb_controller_choices`,
`vm.device.usb_passthrough_choices`, `vm.device.passthrough_device_choices`,
`vm.device.pptdev_choices`

**Disk images**
`vm.export_disk_image`, `vm.import_disk_image`

**Console / display**
`vm.get_display_web_uri`, `vm.get_console`, `vm.get_display_devices`, `vm.port_wizard`,
`vm.resolution_choices`

**Create/edit choices**
`vm.bootloader_options`, `vm.bootloader_ovmf_choices`, `vm.cpu_model_choices`,
`vm.guest_architecture_and_machine_choices`, `vm.random_mac`, `vm.maximum_supported_vcpus`

**Metrics / diagnostics**
`vm.get_memory_usage`, `vm.get_available_memory`, `vm.get_vmemory_in_use`,
`vm.get_vm_memory_info`, `vm.flags`, `vm.virtualization_details`, `vm.supports_virtualization`,
`vm.log_file_path`, `vm.log_file_download`

## 3. Key request/response shapes (verified)

**`vm.create` / `vm.update`** (same field set): `name`, `description`, `vcpus`, `cores`,
`threads`, `cpuset`, `nodeset`, `memory` (MB), `min_memory` (MB, ballooning), `cpu_mode`
(`CUSTOM` / `HOST-MODEL` / `HOST-PASSTHROUGH`), `cpu_model`, `enable_cpu_topology_extension`,
`pin_vcpus`, `suspend_on_snapshot`, `trusted_platform_module`, `hyperv_enlightenments`,
`bootloader` (`UEFI` / `UEFI_CSM`), `bootloader_ovmf`, `autostart`, `hide_from_msr`,
`ensure_display_device`, `time` (`LOCAL`/`UTC`), `shutdown_timeout`, `arch_type`,
`machine_type`, `uuid`, `enable_secure_boot`, `command_line_args`.

**`vm.query` / `vm.get_instance`** → `VmQueryResponse` (already modelled): all of the above plus
`status { state, pid, domain_state }`, `display_available`, `devices`, `id`.

**`vm.clone`** → `{ name?: String? }`.

**`vm.export_disk_image`** → `{ format, directory, zvol }` (job).
**`vm.import_disk_image`** → `{ diskimg, zvol }` (job).
These are the same shape as the `virt.device.*` ones already surfaced in the container UI, so the
existing dialogs/pickers can be reused.

**Device types** (`attributes.dtype` discriminator): `CDROM`, `DISK`, `RAW`, `NIC`, `DISPLAY`,
`PCI`, `USB`.
- `DISK`: `path`, `type`, `iotype`, `create_zvol`, `zvol_name`, `zvol_volsize`,
  `logical_sectorsize`, `physical_sectorsize`, `serial`
- `CDROM`: `path`
- `NIC`: `type` (E1000/VirtIO), `mac`, `nic_attach`, `trust_guest_rx_filters`
- `DISPLAY`: `type` (SPICE), `resolution`, `port`, `web_port`, `bind`, `password`, `wait`, `web`
- `PCI`: `pptdev`
- `RAW`: `path`, `type`, `boot`, `exists`, `size`, `iotype`, `serial`
- `USB`: `controller_type`, `device`, `usb`
- `vm.device.update` also sets `order` (boot priority); `vm.device.convert` changes a disk's
  `iotype`; `vm.device.virtual_size` computes a disk size from a path/zvol.

**Choice endpoints** all return `Map<String,String>` (key → human label) per the app wrappers.

## 4. Current app state

Files: `ui/services/vm/VmsScreen.kt` (1178 lines), `VmsScreenViewModel.kt`,
`ui/services/vm/details/VmInfoPane.kt`, `VmInfoScreen.kt`, `VmDataHolder.kt`,
`data/api/VmService.kt`, `data/models/Vm.kt`.

**Used (8 of 54):** `vm.query`, `vm.start`, `vm.stop`, `vm.restart`, `vm.delete`, `vm.suspend`,
`vm.resume`, `vm.poweroff`.

**Unwired (46):** everything else — create/update, all device management, console/display,
clone, export/import disk image, all choice endpoints, metrics/diagnostics.

Current UI is the "before" version of the container screens:
- `VmsScreen` uses plain elevated cards, a text-glyph/`ActionButton` layout, private
  `ActionButton`/`CompactActionButton`/`StatusChip`/`getStatusColor`/`getStatusIcon`/
  `MorphingRefreshIndicator` copies, a duplicated `VmFilterBar`, and its own
  `trackContainerOperation` job poller keyed by VM id.
- `VmInfoPane`/`VmInfoScreen` use private `ExpressiveSection`/`ExpressiveInfoCard`/`InfoRow`
  copies and a `UnifiedScreenHeader` top bar rather than the container/App detail chrome.
- `VmInfoScreen` receives the whole `VmQueryResponse` via `VmDataHolder` (no fresh fetch, no
  cache seeding, no delete/clone/export actions).

This is exactly the same divergence the Containers audit found; the container work already
established the canonical replacements, so most of the VM effort is *reuse*, not new design.

## 5. Design principle

One visualization feature, two namespaces. Containers and VMs should feel like the same product:
- identical card chrome, status pills/dots, section headers, info cards, entry cards, action
  buttons, filter bars, dialogs, sheets, job progress, pickers, upload flow;
- identical navigation shapes (list → detail → edit / devices / export) and bottom
  Cancel/Save bars on forms;
- a **single** job tracker and a **single** set of status/shape helpers parameterised per type.

Prefer generalising existing container components over copying them for VMs. Where a shape/colour
is type-specific (container vs VM), pass it in rather than duplicating.
