# Container (virt) API Completion Plan

Status: implemented except the two volume-import calls and the optional bridge-detail call.

## Implemented

1. Dead duplicate wrappers removed (`genAnInstanceWithResult`,
   `getVirtInstanceDeviceListWithResult`, `addInstanceDeviceWithResult`,
   `getInstanceDeviceListWithResult`, `updateInstanceDeviceWithResult`).
2. Volume create fixed: `content_type = "BLOCK"`, `size` MB (min 512).
3. Volume edit/resize in the volumes page (`virt.volume.get_instance` + `virt.volume.update`),
   with an edit action on each row.
4. CDROM device type added (`source`, `boot_priority`, `readonly`); device `dev_type` now sent
   uppercase via `wireName` (fixes a latent casing bug).
5. Set bootable disk: **VM-only**, reverted from containers (server returns `[EFAULT]`).
6. Global Virt settings: hub entry + screen (`virt.global.config`) with edit sheet
   (`pool`, `storage_pools`, `bridge`, `v4/v6_network`) → `virt.global.update` job tracked with
   progress. Bridge choices from `virt.global.bridge_choices`, pools from `pool_choices`.
   Fixed `updateGlobalConfigWithResult` to return the job id.
7. Export boot image in `ContainerInfoScreen` above the Danger Zone
   (`virt.device.export_disk_image`), format/directory/zvol dialog, job tracked. Target directory
   uses the reusable `DatasetPickerSheet` (same explorer picker as share creation); source zvol
   is populated from the container's DISK/CDROM device `source` values (with a manual field only
   when the container exposes none).
8. Import disk image in the devices screen (`virt.device.import_disk_image`), diskimg/zvol
   dialog, job tracked.
9. Volume ISO import in the volumes page (`virt.volume.import_iso`, job): entry card → sheet
   with volume name, ISO path **on the server**, and pool; live progress while importing.
   The ISO path now uses **`ServerPathPickerSheet`** (see below); verified the endpoint reads
   `iso_location` from the server filesystem.
10. Extended the custom picker (`ui/homepage/dataset/ServerPathPickerSheet.kt`): a directory/file
    browser over `filesystem.listdir` with a **“Show files” mode that is off by default** and an
    optional extension filter. Used for the ISO import path and the CDROM device source.
11. Global Virt settings: selecting a bridge now calls `virt.global.get_network` and fills the
    IPv4/IPv6 inputs (and shows bridge IPv4/IPv6/NAT on the screen); the storage-pools field now
    uses the multi-select `DatasetPickerSheet`.
12. Device → server ISO upload: `virtual path`/filename chosen on the server, file picked from
    Android (`OpenDocument`), streamed to TrueNAS's **`/_upload`** multipart endpoint
    (`filesystem.put`), with byte progress during transfer and `core.get_jobs` progress after;
    `ContainerJobTracker` operation `UPLOADING`.
    Verified against the live server: `/_upload` accepts **HTTP Basic auth** (account
    username/password) and returns `{"job_id": N}`; a WS auth token is **not** accepted as a
    Bearer token. The app stores credentials per login method, so API-key logins use
    `Authorization: Bearer <api_key>` and password/TOTP logins use Basic auth.

## Remaining (not wired)

- `virt.volume.import_zvol` — ZVOL import (returns an array; verify shape).
- `virt.instance.set_bootable_disk` — VM phase.

All other container `virt.*` calls are now wired (the `get_network` call is used by the global
settings IPv4/IPv6 prefill).

## API facts (verified against the live server, v25.10.5)

- `virt.device.export_disk_image` — job. Params: `format` (QCOW2, QED, RAW, VDI, VPC, VMDK),
  `directory`, `zvol`. "Exports a zvol to a formatted VM disk image."
- `virt.device.import_disk_image` — job. Params: `diskimg` (path), `zvol` (target). Auto-detects
  QCOW2/QED/RAW/VDI/VPC/VMDK and converts to a ZVOL.
- `virt.global.config` returns: `id`, `pool`, `dataset`, `storage_pools`, `bridge`,
  `v4_network`, `v6_network`, `state`.
- `virt.global.update` — job. Params: `pool`, `bridge`, `storage_pools`, `v4_network`,
  `v6_network`. `bridge = null` auto-creates one; `pool = null` disables the service.
- `virt.global.bridge_choices` → map like `{"[AUTO]": "Automatic", "br1": "br1"}`.
- `virt.global.get_network(name)` → bridge detail (`type`, `managed`, `ipv4_address`,
  `ipv4_nat`, `ipv6_address`, `ipv6_nat`).
- `virt.instance.set_bootable_disk(id, disk)`.
- `virt.volume.create` — `content_type` is const `"BLOCK"`; `size` MB (min 512, default 1024);
  `storage_pool`. No `type` param.
- `virt.volume.update` — only `size` (MB, min 512). Volume edit = resize.
- `virt.volume.import_iso` — `name`, `iso_location`, `upload_iso`, `storage_pool`.
- `virt.volume.import_zvol` — `to_import[]` (`virt_volume_name`, ...), `clone`.
- Device types: `DISK, GPU, NIC, PCI, PROXY, TPM, USB, CDROM`.
  - `CDROM` fields: `source`, `boot_priority`, `readonly`, `name`, `description`.
- Live sample: container `alpine` root device is `{dev_type: DISK, name: root, source: null,
  destination: "/", storage_pool: "Main"}`. `virt.device.disk_choices` returned `{}`; no zvols
  exist on the host, so export must accept a `zvol` path from the user (offer choices when the
  endpoint returns any).

## Tasks

1. **Remove dead duplicate wrappers** — `genAnInstanceWithResult`,
   `getVirtInstanceDeviceListWithResult`, `addInstanceDeviceWithResult`,
   `getInstanceDeviceListWithResult`, `updateInstanceDeviceWithResult`.
2. **Volume create fix** — send `content_type = "BLOCK"` and a `size` (MB, min 512) field.
3. **Volume edit (resize)** — tap a volume row → sheet with current size + new size →
   `virt.volume.update`. Uses `virt.volume.get_instance` to refresh before edit.
4. **CDROM device type** — add to `ContainerDeviceType` + device-add UI (`source`,
   `boot_priority`, `readonly`).
5. **Set bootable disk** — MOVED TO VM PHASE. Verified against the live server:
   `virt.instance.set_bootable_disk` returns `[EFAULT] Setting disk to boot from is only valid for
   VM instances.` The wrapper stays in `VirtService` for the VM edit screen.
   Also verified: `virt.global.update` is a **job** on the WebSocket (returns a job id); the
   service wrapper currently types it as `GlobalEntry`, which must change to `Int` + job poll.
6. **Global Virt settings** — new "Global Virt settings" entry in instance settings → screen
   showing `virt.global.config`; Edit sheet for `pool` (pool choices), `bridge`
   (bridge choices), `storage_pools`, `v4_network`, `v6_network` → `virt.global.update` (job,
   tracked with progress).
7. **Export boot image** — in `ContainerInfoScreen` above the Danger Zone. Dialog: format,
   directory, zvol → `virt.device.export_disk_image` (job, tracked).
8. **Import disk image** — in the container devices screen (devices config). Dialog: diskimg,
   zvol → `virt.device.import_disk_image` (job, tracked).

## Notes

- Export/import disk image are `virt.device.*` and operate on zvol paths. The container root is
  an Incus storage volume (no zvol exposed; `disk_choices` is empty), so these are generic
  zvol/diskimg dialogs that accept paths from the user. Placed in the container screens as
  requested; semantics fit VMs more naturally.
- Job tracking: reuse `ContainerJobTracker` (keyed by container id) for export/import so progress
  is visible and survives navigation. Global update has no container id, so the global settings
  ViewModel polls its own job.
- Keep the existing navigation/architecture; add routes alongside the existing container ones.
