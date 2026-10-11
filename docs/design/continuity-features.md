# Continuity & Device Pairing — Research & Proposal

Status: **proposal / for discussion** (not yet implemented)
Scope: secure, low-effort sign-in on a second device (primarily tablets) by handing off
access from a device that is already signed in.

---

## 1. Goal & non-goals

**Goal.** A user who is already signed in on their phone should be able to get their tablet
(or any other device) signed in **without typing the server URL, username, password, or 2FA
code** — by showing a QR on the tablet and scanning it with the phone. On large screens this
becomes the *primary* sign-in path; the existing password / API-key form stays as an optional
fallback.

**Non-goals (for v1).**
- No cloud service of ours. TrueHub is self-hosted; there is no backend we can run a relay on.
- Not transferring the user's *password*. Ever.
- Not a full account-sync / backup product. This is a one-time, revocable access handoff.

---

## 2. What "continuity" means here

Apple calls it Continuity/Handoff, Google is building the Cross-Device SDK, FIDO calls it
hybrid transport. The common shape is:

1. The new device shows a QR code (or asks for one).
2. The trusted device proves it is physically near, agrees on a key with the new device.
3. The trusted device authorises the new device over an **end-to-end encrypted** channel.
4. The new device ends up with its **own** credential — never a copy of the user's password.

The last point is the important one, and it is what makes a design "safe".

---

## 3. Threat model

| # | Threat | Consequence | Mitigation (see §6) |
|---|---|---|---|
| T1 | Bystander photographs / screenshots the QR | Attacker learns the payload | Payload is ECDH-encrypted; the QR alone is useless |
| T2 | QR forwarded remotely (chat, email) | Attacker logs in their device | Mutual-QR ECDH: ciphertext is bound to the *scanner's* ephemeral key; a forwarded QR cannot be decrypted |
| T3 | Replay of a captured handshake | Attacker re-uses a session | One-time session nonce + short TTL + consumed on use |
| T4 | Long-lived credential leaks | Permanent account access | Credential is a short-lived, revocable API key (or session token), not a password |
| T5 | Key extraction from the new device | Attacker reads stored secret | Android Keystore-backed encryption (hardware-backed where available) |
| T6 | Downgrade to plaintext transport | Credential intercepted | API-key auth requires HTTPS/wss; TrueNAS auto-revokes keys seen over insecure transport |
| T7 | Malicious app drives the flow | Silent account linking | Explicit user confirmation on **both** devices + visible linked-device list |

Out of scope: a fully compromised phone or a rooted device with an unlocked Keystore.

---

## 4. Prior art (what the industry does)

- **FIDO2 / WebAuthn hybrid transport (a.k.a. caBLE, now "Proximity Exchange Protocol").**
  The new device shows a QR containing an ephemeral public key + a secret; the phone proves
  proximity with a BLE advertisement; all real data flows over an end-to-end-encrypted tunnel
  using a Noise `KNpsk0` handshake (P-256, SHA-256, AES-256-GCM). **Bluetooth is a proximity
  proof, not the data channel.** A forwarded QR alone is not enough because the phone demands
  the BLE advert. This is the gold standard, but it needs a relying party that speaks WebAuthn
  (TrueNAS does not) and a tunnel service — so we borrow the *shape*, not the protocol.
- **Signal-style device provisioning.** QR carries `{sessionId, primaryEphemeralPublicKey}`;
  sessions expire in **5 minutes**; ECDH derives the encryption key; the relay only ever sees
  ciphertext; sessions are one-time.
- **LearnCard "QR login".** New device generates an ephemeral ECDH key and shows a QR + a
  6-digit code; the signed-in device encrypts its "device share" with the ECDH secret and posts
  it; the server relays opaque bytes only.
- **IETF `draft-secure-credential-transfer`.** Relay + mailbox + one-time "Secret" + device
  claims; recommends sending the URL and the secret over *different* channels.
- **ECDHE-over-QR (Univ. Hamburg).** Two mutual QR scans, ephemeral ECDH, AES-GCM — works with
  **no network and no Bluetooth at all**.
- **Google Cross-Device SDK.** `Discovery` + `Sessions` APIs; same-Google-account devices,
  system device picker, encrypted peer-to-peer channel. Great UX, but Android-only, same-account
  only, and currently a Developer Preview.
- **Android `CompanionDeviceManager`.** `requestDeviceTrustPairing(...)` with
  `TRUST_TYPE_CROSS_DEVICE_AUTHENTICATION` can establish a platform-level trusted channel
  between devices — a possible future "fast path".

**Takeaway:** the QR + ephemeral-ECDH + one-time-session pattern is well established and does
not require a relay if we do the exchange **mutually** (device → phone → device).

---

## 5. The credential we hand over (key insight)

TrueNAS SCALE ≥ 25.04 has **user-linked API keys** with exactly the lifecycle we want:

- `api_key.create` takes an **`expires_at`** timestamp (or `null` for non-expiring).
- Keys are **revocable** (`api_key.delete`), listable (`api_key.query`, `api_key.my_keys`).
- They are **64 random characters**, shown only once at creation.
- They are **password-equivalent** and **bypass 2FA** for the owning user.

So instead of shipping a password across the wire, the phone **mints a fresh, expiring,
revocable API key for the new device**, and the tablet stores *its own* key. The user can
revoke it later from the phone or the TrueNAS UI. This is strictly better than copying a
session token, because revocation is independent and the blast radius is time-boxed.

**Caveats to design around:**
- Creating a key requires the `API_KEY_WRITE` (or `READONLY_ADMIN`) role. If the signed-in user
  lacks it, fall back to transferring the current **session token** with the same ECDH
  protection (still short-lived, still revocable by logging out).
- API keys bypass 2FA, which is exactly why we make them **short-lived by default** (e.g. 15
  minutes) and surface them in a "linked devices" list.
- API-key auth over insecure transport is auto-revoked by TrueNAS — pair only over HTTPS/wss.

---

## 6. Recommended design — "Pair a device" (one scan, LAN-direct ECDHE)

**The tablet only *displays* a QR; the phone does all the scanning and the connecting.**
This removes the biggest usability risk: tablets frequently have poor or no cameras. The phone
is the device with the good camera, so it should be the one doing the work.

No relay server is required because the two devices are almost always on the same LAN, and the
payload is end-to-end encrypted anyway (the transport does not need to be trusted).

```
  TABLET (new device, no camera needed)      PHONE (already signed in, good camera)
  ────────────────────────────────────      ─────────────────────────────────────
  1. generate ephemeral EC key pair (Kt, kt)
  2. open an ephemeral pairing socket
  3. show QR:
     { v, sessionNonce, Kt, addrs[], port }
                                            4. scan the tablet's QR
                                            5. connect to the tablet over the LAN
                                            6. confirm "Sign in <tablet name>?"
                                            7. mint a short-lived API key on TrueNAS
                                               (expires_at = now + chosen lifetime)
                                            8. S = ECDH(kp, Kt)
                                               key = HKDF(S, nonce ‖ Kt ‖ Kp)
                                               blob = AES-256-GCM(key, { serverUrl,
                                                    insecure, credential, expiresAt })
                                               send { Kp, iv, blob, tag }
  9. S = ECDH(kt, Kp); decrypt blob
 10. seal credential in Keystore-backed store
 11. connect + land on Home
```

### 6.1 Why LAN-direct (and not a relay)

- The tablet advertises its **own** candidate LAN addresses (all non-loopback IPv4/IPv6) and a
  random high port in the QR. The phone connects straight to it.
- The channel carries **only ciphertext**. The tablet is authenticated because its public key
  came from the QR the phone just scanned; the phone is authenticated to the tablet by the
  ECDH handshake. Neither the Wi-Fi nor an on-path attacker can read or forge the payload.
- One request/response, then the socket closes. ~90 s timeout. No discovery protocol, no mDNS,
  no Bluetooth permission, no Play Services.
- If the two devices are **not** on the same network, or the LAN blocks client-to-client
  traffic, fall back to the mutual-QR path (§6.4) or the manual form.

### 6.2 Crypto

- **Key agreement:** ephemeral **ECDH on P-256** per session; private keys never leave their
  device and are discarded after the transfer.
- **Key derivation:** **HKDF-SHA256** over the shared secret, with `info` binding
  `sessionNonce ‖ Kt ‖ Kp` (prevents key-reuse / cross-session substitution).
- **Payload:** **AES-256-GCM**, random 96-bit IV, auth tag verified before parsing.
- **Session:** random 128-bit `sessionNonce`; **TTL 5 minutes**; **single use**.
- All primitives are in the platform (`KeyPairGenerator`/`KeyAgreement`/`Cipher`/`Mac`) — no
  third-party crypto dependency.

### 6.3 Why this resists the threats

- **T1/T2 (QR captured/forwarded):** the payload is encrypted to the *tablet's* ephemeral key.
  A screenshot or a forwarded QR is useless — an attacker has neither private key, and the
  phone never sends anything to anyone but the address in the QR.
- **T3 (replay):** the nonce + 5-minute TTL + one-time consumption make a captured handshake
  worthless.
- **T4 (leak):** the credential is a revocable, expiring API key, not a password.
- **T5:** the stored key is sealed with an Android Keystore key (see §9).
- **T6:** we refuse to pair over `ws://` (insecure) unless the user explicitly opts in, and
  TrueNAS revokes keys used insecurely anyway.
- **T7:** both devices show an explicit confirmation; the phone lists the device it just
  authorised and offers "Revoke".

### 6.4 Fallbacks (in order)

1. **LAN-direct** (primary) — tablet displays, phone scans + connects. Tablet needs no camera.
2. **Mutual-QR** — if the LAN path fails *and* the tablet has a working camera: the phone shows
   a second QR containing `{Kp, blob}` and the tablet scans it. Same crypto, one extra scan.
3. **Manual** — the existing server-URL + username/password/API-key form.

### 6.5 Optional hardening (v1.1)

- Add a **6-digit confirmation code** displayed on both devices (derived from the shared
  secret) — the user taps "these match", defeating a hypothetical MITM that swaps public keys.
- Add a **BLE proximity advert** (as FIDO does) so a *forwarded* QR can't even start a session.
- Support **Nearby Connections / Cross-Device SDK** as a same-account fast path that skips the
  address exchange entirely.

---

### 6.6 Discovery: why the QR carries the address (and where Nearby Connections fits)

**Fast Pair** (headphones) solves a *different* problem. Its "magic" is a **BLE advertisement**
carrying a 24-bit **Model ID**; the phone's Google Play Services recognises it and shows a
half-sheet. Spoofing is prevented by an **ECDH anti-spoofing handshake** plus an **account key**
bound to the user's Google account, which enables the "second device" notification later. It is
Bluetooth-only, accessory-oriented, and needs Google's blessing per product — so it is **not
reusable for app-to-app** pairing. We can borrow the *idea* (advertise a stable, verifiable
identity, then prove possession), but not the mechanism.

The real question is just: **how does the phone learn the tablet's address?** Our answer is the
QR itself. That is a feature, not a limitation:

| Discovery method | Extra permissions | Radios | Private? | Reliability |
|---|---|---|---|---|
| **Address in the QR (ours)** | none | none (the QR *is* the discovery) | yes — nothing is broadcast | high; only needs the LAN to be reachable |
| mDNS / DNS-SD (`NsdManager`) | `ACCESS_LOCAL_NETWORK` (Android 17+), multicast lock pre-13 | Wi-Fi | **no** — every device on the LAN sees the service | fragile: known `NsdManager` IP/TXT cross-device mixing bug; AP isolation breaks it |
| Wi-Fi Aware (NAN) | `NEARBY_WIFI_DEVICES` | Wi-Fi (needs HW support) | no | device-dependent |
| Wi-Fi Direct (P2P) | `NEARBY_WIFI_DEVICES` + location | Wi-Fi | no | clunky, location-gated |
| UDP broadcast/multicast | multicast lock | Wi-Fi | no | AP isolation + multicast lock issues |
| **Nearby Connections** | Bluetooth scan/advertise/connect, location or `NEARBY_WIFI_DEVICES`, Play Services | BLE + Bluetooth + Wi-Fi | no (advertises a service ID) | good, but heavy |

Because the QR already hands the phone a verifiable public key **and** the tablet's address, we
need no discovery protocol at all for the primary path. Fewer permissions, no broadcast, no
Google dependency, works even with Bluetooth switched off.

**Is Nearby Connections secure?** Mostly, with caveats:

- It **encrypts** connections and uses a **symmetric authentication flow** — both devices are
  shown a short **authentication token** and the app is expected to have the user confirm they
  match. If an app accepts blindly, a nearby MITM/relay is possible, so the token comparison is
the actual trust anchor.
- It pulls in a **lot of runtime permissions** (Bluetooth advertise/connect/scan, location or
  nearby-Wi-Fi, and `ACCESS_LOCAL_NETWORK` on Android 17+), requires **Google Play Services**,
  and collects **usage analytics** (opt-out via Google settings).
- It uses **Bluetooth** under the hood — so it does not meet a "no Bluetooth" requirement.

**How we'd use it safely:** treat Nearby Connections (or NAN, or mutual-QR) purely as a
**transport**, never as the trust anchor. The payload is already end-to-end encrypted with
ECDH and the tablet's public key came from the QR, so even a fully compromised transport cannot
read or forge the credential. That is the whole point of doing our own crypto: **the transport
becomes interchangeable.**

Recommended ladder:
1. **QR + LAN socket** (primary) — no permissions, no radios beyond Wi-Fi.
2. **QR + Nearby Connections** (optional transport) — when the LAN is unreachable but the user
   accepts the Bluetooth/location permissions; still only a pipe for our ciphertext.
3. **Mutual QR** — if the tablet has a camera.
4. **Manual form.**


| Option | Pros | Cons | Verdict |
|---|---|---|---|
| **LAN-direct, one scan (tablet displays, phone scans + connects)** | No backend; E2E encrypted; revocable credential; tablet needs no camera; cross-platform | Needs both devices on the same reachable LAN | **Recommended** |
| **Nearby Connections as the transport** | Works without a shared LAN; abstracted radios | Bluetooth + location/nearby-Wi-Fi permissions; Play Services; analytics; token-based trust | Optional transport only (we keep our own E2E crypto) |
| Mutual-QR ECDHE (two scans) | Works with no shared network | Requires a tablet camera | Fallback only |
| Single QR with a PIN-wrapped key (Coldcard-style) | One scan | Short PIN ⇒ weaker; relies on rate-limiting | Fallback only |
| Relay server (IETF draft) | One scan; works when devices aren't co-located | We must run/host a relay; another trust anchor | Rejected for v1 |
| WebAuthn/passkey hybrid transport | Best-in-class UX & security | TrueNAS isn't a WebAuthn RP; needs a tunnel service | Not possible today |
| Google Cross-Device SDK / Sessions | Great UX on same-account Android | Android-only, same Google account, Preview | Optional fast path |
| Transfer the session token directly | Simple | Token may be long-lived; revoke = log out everywhere | Only as fallback |

---

## 8. Where it lives in the app

**Tablet / large screens (primary).** The login screen (`OnboardingScaffold`, Expanded width)
leads with a **"Sign in from your phone"** action; the password / API-key form moves behind a
"Use password or API key instead" disclosure. Phones keep the current form as primary.

**Settings → "Easy setup" (new).** A dedicated screen with two roles:
- **"Link this device"** (on the new device): shows the pairing QR and waits. The tablet needs
  **no camera** — the phone does all the work.
- **"Authorise a device"** (on the signed-in device): scans the new device's QR, confirms,
  mints the key, connects to the new device over the LAN, sends the encrypted payload, then
  shows success + a **Revoke** affordance.
- **"Linked devices"**: list of API keys created through pairing (name, created, expires),
  with per-row **Revoke**.

**States to design:** idle → showing QR / waiting for phone → phone connected → confirmed →
success / expired / cancelled / not-permitted (`API_KEY_WRITE` missing) / insecure-transport
blocked / LAN-unreachable (offer the camera fallback or manual entry).

---

## 9. Storage & platform notes

- **Do not** use `EncryptedSharedPreferences` / `EncryptedFile` — the whole Jetpack Security
  Crypto library is deprecated. Use an **Android Keystore** AES-256-GCM key (StrongBox when
  available, TEE fallback) and store the ciphertext (IV + blob) in **DataStore**.
- Exclude the sealed store from Auto Backup; "decryption failure ⇒ re-login", never data loss.
- **QR generation:** ZXing core (or an equivalent) rendered into a Compose `Canvas`/`Image`.
- **QR scanning:** CameraX + ML Kit barcode scanning (or ZXing embedded) — needs `CAMERA`
  permission, requested just-in-time with a rationale.
- Reuse the existing `OnboardingScaffold` / `OnboardingBrand` for the two-pane pairing UI so it
  matches Welcome / Server setup / Login exactly.

---

## 10. Implementation plan

**Phase 0 — groundwork (done / small)**
- TOTP screen is now previewable in the debug mock harness (`MockDataPreviewPage → TOTP`).

**Phase 1 — API surface**
- Add `api_key.create(expires_at)` / `api_key.delete` / `api_key.query` to the API manager.
- Add a small `PairingCrypto` utility (ECDH P-256, HKDF-SHA256, AES-GCM) with unit tests
  (known-answer vectors + round-trip + tamper detection).
- Add Keystore+DataStore sealed store for credentials.

**Phase 2 — the flow**
- `PairDeviceScreen` (new device): QR display + scanner + result states.
- `AuthorizeDeviceScreen` (signed-in device): scanner + confirm + mint + encrypted QR + revoke.
- Wire into `OnboardingScaffold`; make it primary on Expanded.

**Phase 3 — settings & lifecycle**
- Settings → Easy setup, Linked devices, revoke.
- Expiry/revocation handling; error surfacing; accessibility pass (labels, 48dp targets,
  `liveRegion` for "waiting"/"paired", camera alternative = manual code entry).

**Phase 4 — optional fast paths**
- 6-digit mutual confirmation code.
- BLE proximity advert.
- Cross-Device SDK / Nearby Connections same-account shortcut.

---

## 11. Open questions (need your call)

1. **Default key lifetime** for a paired device: 15 minutes (re-link each time), 30 days
   (convenient), or let the user choose? (I lean: 30 days default, 15-minute option, revocable.)
2. **Fallback when `API_KEY_WRITE` is missing** (non-admin user): transfer the session token,
   or require an admin to do the pairing? (I lean: transfer the token, clearly labelled.)
3. **Device naming:** auto (`"Pixel 8a"`) or prompt for a label at pairing time?
4. **Insecure servers:** allow pairing over `ws://` with a warning, or block it? (TrueNAS will
   revoke the key anyway; I lean: block by default, allow with an explicit "I understand".)
5. **LAN-direct transport:** OK to bind an ephemeral socket on the tablet (no extra permissions
   needed), with the mutual-QR path as the only automatic fallback? Or should I add
   **Nearby Connections** as an optional second transport (Bluetooth/location permissions + Play
   Services, but works when the two devices are on different networks)? Note it still only
   carries our ciphertext, so security is unaffected either way.
6. **Tablet QR prominence:** show the pairing QR immediately on the login screen, or behind a
   "Sign in from your phone" button? (I lean: a prominent button, so the form is still one tap
   away.)

---

## 12. References

- FIDO Alliance — *Proximity Exchange Protocol (PXP / CTAP hybrid transport)*:
  https://fidoalliance.org/specs/hybrid/proximity-exchange-protocol-v1.0-wd-20260717.html
- Corbado — *WebAuthn passkey QR codes & Bluetooth (hybrid transport)*:
  https://www.corbado.com/blog/webauthn-passkey-qr-code
- IETF — *draft-secure-credential-transfer*:
  https://www.ietf.org/archive/id/draft-secure-credential-transfer-03.html
- Univ. Hamburg — *Sharing Secrets between Mobile Devices* (ECDHE over QR):
  https://svs.informatik.uni-hamburg.de/publications/2018/2018-08-20-SecretSharing-Whitepaper.pdf
- open-e2ee — *Multi-device provisioning* (5-minute sessions, ECDH):
  https://docs.open-e2ee.dev/build/multi-device
- LearnCard — *Cross-device (QR) login*:
  https://docs.learncard.com/core-concepts/identities-and-keys/cross-device-login
- Coldcard — *Key Teleport* (QR + PIN-wrapped ECDH):
  https://github.com/Coldcard/firmware/blob/master/docs/key-teleport.md
- Android — *Cross device SDK*:
  https://developer.android.com/guide/topics/connectivity/cross-device-sdk/overview
- Android — *Companion device pairing* / `CompanionDeviceManager`:
  https://developer.android.com/develop/connectivity/bluetooth/companion-device-pairing
- TrueNAS — *Managing API Keys* / `api_key.create` (`expires_at`, revocable):
  https://www.truenas.com/docs/scale/toptoolbar/managingapikeys/ ·
  https://api.truenas.com/v26.0/api_methods_api_key.create.html
- Android — *Keystore + DataStore secure storage* (EncryptedSharedPreferences is deprecated):
  https://developer.android.com/reference/kotlin/androidx/security/crypto/package-summary
