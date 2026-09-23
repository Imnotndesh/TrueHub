# WebSocket Session Recovery Architecture Hypothesis

## Status and purpose

This document is a design hypothesis for review before implementation. It does not change application code.

The goal is to make recovery from an accidental transport drop safe and consistent throughout the application, not only on the home screen. In this document, a transport drop includes a literal WebSocket close, a failed WebSocket request, a server/proxy/router interruption, Android process recreation, and a network transition that leaves the current TrueNAS session unusable. A dropped transport should not automatically destroy the user's session, but it must never be allowed to make stale or incorrectly authenticated data appear to be valid.

The proposal is based on the current TrueHub behavior after the Hilt migration and the recovery work merged into `v0.8.1-rc`.

## Current behavior

The application currently has two partially overlapping recovery paths.

### Page-level recovery

The home screen explicitly calls the shared manager recovery operation before a retry or a refresh when the transport is disconnected. This is useful for the reported home-screen case, but it is page-specific.

Other pages, including Apps, Marketplace, services, and detail screens, generally call their API services directly. They do not explicitly invoke the same reconnect-before-refresh operation.

### Request-level recovery

Most normal API methods use `TrueNASApiManager.callWithResult()`. That layer reconnects the transport when a request notices that the client is no longer connected. It also recognizes certain authentication failures and can run a single session recovery and request retry.

This gives other pages some protection when the failed request is clearly an authentication error. It does not fully handle an accidental WebSocket drop where the first failure is classified as a normal transport or request error rather than an authentication error.

The current distinction is therefore important:

- A request that returns a recognized authentication error may recover automatically.
- A request that fails because the socket is closed may reconnect its transport but may not reauthenticate before continuing.
- A page that wants a deliberate retry after displaying a disconnected state may not trigger reauthentication.
- A process restart can restore a new manager and a persisted session, but in-memory ViewModels and page state still need to be treated as a new session generation.

## Failure model

The design must account for more than a simple network-offline event.

### Transport and WebSocket drops

The relevant failure is not limited to a WebSocket listener receiving an explicit close event. It is the broader condition where the app can no longer rely on its existing TrueNAS transport session.

Examples include:

- The server closes the WebSocket while the phone still has general network connectivity.
- A router, proxy, VPN, or NAT silently drops the long-lived connection.
- Android destroys the process and later restores it without the old in-memory connection.
- The network briefly changes and the old socket appears usable locally but is no longer usable on the server.
- A request fails before the application can determine whether the server still considers the session authenticated.
- The WebSocket handshake succeeds again, but the server-side authenticated session was lost.

The key architectural distinction is between transport recovery and session recovery. Re-establishing a WebSocket only proves that a connection was created. It does not prove that the selected account is authenticated, that the saved token is valid, or that the old account manager can safely be reused.

The recovery design must therefore handle both the transport event and the possible authentication loss that follows it.

### Process death and restoration

Android can destroy the process while the user is away. On return, the app may display cached data before it has established a new WebSocket or validated the saved session.

The saved profile and token may still be correct, but the process-local authenticated manager and any old ViewModels must not be assumed to still represent the server's current session state.

### Token invalidation

A token can fail because it expired, was revoked, was invalidated by inactivity, was created with an origin policy that no longer matches, or was invalidated by a server-side session change.

Token failure is an authentication outcome, not automatically an offline outcome.

### Server or network failure

The server can be temporarily unavailable even when the token and credentials are correct. This should result in a retryable transport state, not an invalid-credentials state.

### Authentication failure

A saved password, API key, or token may genuinely no longer be accepted. Retrying those credentials indefinitely would create a login loop and hide the real problem.

### Account switching race

A recovery operation started for account A must never publish a result for account B after the user switches accounts. The active server/account identity must be captured and checked throughout the operation.

## Safety goals and invariants

The proposed architecture should preserve these invariants.

1. A successful WebSocket connection is not proof that the user is authenticated.
2. Authentication recovery must be owned by one process-wide authority, not duplicated in every page.
3. At most one recovery operation for the same active profile may run at a time.
4. A recovery result may only be published if its server and account identity still match the active session generation.
5. A failed recovery must not silently reuse a manager belonging to another account.
6. A normal API request may be retried only a bounded number of times after recovery.
7. Authentication calls themselves must not recurse through the generic request-recovery path.
8. Network failures, invalid credentials, OTP requirements, and token persistence failures must remain distinguishable.
9. A token must not be treated as valid solely because it exists in preferences.
10. Cached data may remain visible while disconnected, but it must be labeled or handled as stale until a fresh request succeeds.
11. Auto-login-disabled profiles must not be silently reauthenticated by background or page-level recovery.
12. Interactive-only OTP profiles must not be recovered by workers.
13. Account switching must not expose the old authenticated navigation graph with the new profile's persisted state.
14. Logout and account deletion must invalidate pending recovery before their changes become visible.
15. Recovery must be cancellable when the user leaves the active profile, signs out, or switches accounts.

## Proposed ownership model

The recovery authority should be a singleton Hilt component, conceptually separate from the page ViewModels.

It should own:

- The active server ID and account ID.
- The current session generation.
- The current authenticated manager or a connection to the session provider.
- The current transport state.
- The current authentication state.
- The active recovery job and mutex.
- The account-switch operation.
- The logout operation.
- The last recovery outcome and retry metadata.
- The active profile's policy, including whether automatic login is allowed.

The persisted session repository should remain the owner of:

- Saved servers.
- Saved accounts.
- Credentials.
- Per-account tokens.
- Token expiry metadata.
- Last-used profile.
- Auto-login settings.

The singleton should be the authority for process-local state. The repository should be the authority for data that must survive process death.

## Proposed state model

The exact names can change during implementation, but the state model should distinguish these situations.

### Unauthenticated

There is no usable authenticated session. This may be the initial state, a logout result, a missing profile, or a profile that explicitly disabled automatic login.

### Offline

The device or server is currently unavailable. A saved profile may still exist, but no authentication attempt should be treated as successful.

### Connecting

The application is attempting to establish or replace the WebSocket transport. This is a transport state, not proof of authentication.

### ValidatingToken

A saved token is being used to establish the server-side authenticated session.

### Reauthenticating

The token failed or was unavailable, and the saved API key or password is being used. The operation may be foreground-only depending on credential type and OTP policy.

### OtpRequired

The saved credentials require interactive OTP verification. Foreground UI may offer the OTP flow. Background workers must stop and report that authentication is required.

### Authenticated

The current manager is connected and the server has accepted the active account identity.

### AuthenticatedTemporary

The server session is currently authenticated, but a new token could not be generated or persisted. This is safe for foreground work, but it is not a durable recovery state.

### RecoveryFailed

The last recovery attempt failed in a way that is not automatically retryable, such as rejected credentials or an unusable profile.

A recovery attempt should always resolve to one of the terminal outcomes: authenticated, temporary authenticated, offline/retryable, interactive authentication required, or unauthenticated.

## Recovery trigger model

Recovery should be centralized but triggered by more than the home screen.

### WebSocket lifecycle events

A socket close or failure should update transport state and fail pending requests. It may schedule a low-priority recovery attempt, but it should not immediately run expensive credential fallback while the device is offline or the process is being stopped.

### Android network events

When network connectivity returns, the coordinator should consider recovery. It must still respect the active profile, auto-login policy, and process state.

### Foreground lifecycle

When the application returns to the foreground after being backgrounded or restored, the coordinator should validate the current session before allowing a new foreground request to rely on it.

This is especially important after Android has killed and recreated the process.

### API request failures

A normal request may ask the coordinator to recover only when the failure indicates one of the following:

- The transport is disconnected.
- The request received a recognized authentication failure.
- The request was made against an old session generation.

A request should not start an unbounded recovery loop.

### Explicit user retry

Retry buttons and pull-to-refresh actions should call the same coordinator operation as ordinary API requests. A page should not need to know whether recovery will use a token, an API key, or a password.

### Background workers

Workers should request recovery for their explicit server/account scope. They should not use whichever profile happens to be current when the worker runs.

## Proposed recovery sequence

The coordinator should use the following high-level sequence.

1. Capture the active server ID, account ID, and session generation at the start of the operation.
2. Verify that the profile still exists, still belongs to the server, and still allows the requested kind of recovery.
3. Cancel or ignore any older recovery job for the same profile when the lifecycle no longer permits it.
4. Ensure the WebSocket transport is connected. If the network or server is unavailable, remain in an offline or retryable state.
5. Attempt authentication with the saved token when the token is eligible and not known to be expired.
6. Treat a Boolean authentication result strictly: only an explicit true means token authentication succeeded.
7. Verify the resulting identity with `auth.me` before treating a token login as the active account session.
8. If token authentication fails and automatic login is allowed, attempt the saved API key or password using the existing login mechanism.
9. If the credential path requires OTP, stop and request interactive authentication rather than retrying in the background.
10. If credentials are rejected, stop with a durable authentication failure. Do not keep retrying them automatically.
11. After successful authentication, generate and persist a replacement token when the session policy calls for one.
12. If token generation or storage fails, publish only a temporary authenticated session for the foreground and expose the persistence failure.
13. Verify that the active profile and session generation have not changed before publishing the result.
14. Publish the manager and session generation atomically.
15. Allow the original request or page refresh to continue only when the published result is authenticated or temporary authenticated.
16. Retry the original operation at most once. If it fails again, surface the failure rather than silently entering another recovery cycle.

## Handling page requests safely

The ideal page-facing contract is:

- A page requests data for a page-owned operation.
- The shared request layer performs the bounded recovery if required.
- The page receives either fresh data, a clearly stale cached-data state, or a typed failure.

Pages should not independently log in, select the last-used account, or call raw authentication methods.

A page may still explicitly request a refresh through a shared session operation. The page should not need to know the recovery implementation details. The only page-specific decision should be whether the user explicitly requested a foreground retry.

This gives the following behavior:

- Home refresh: uses the shared recovery operation.
- Apps refresh: uses the shared recovery operation.
- Marketplace refresh: uses the shared recovery operation.
- Services refresh: uses the shared recovery operation.
- Detail-page retry: uses the shared recovery operation if the detail page exposes a retry.
- One-shot background operation: uses the same operation with its explicit profile scope.

## Retry policy

The policy should be deliberately conservative.

### Immediate recovery

An explicit user retry may attempt recovery immediately once. This is the most important new behavior for pages with retry buttons.

### Request retry

After successful authentication, retry the failed request once. A second failure should be returned to the caller.

### Deferred recovery

A socket failure may schedule a delayed recovery attempt. The delay should be short enough to feel responsive but long enough to avoid reconnect loops when a server is down.

### Backoff

Repeated transport failures should use bounded exponential backoff with jitter. Backoff should reset only after a successful authenticated request, not merely after a successful WebSocket handshake.

### Cancellation

Pending recovery should be cancelled when:

- The user signs out.
- The profile is deleted.
- The account changes.
- The server changes.
- The application is no longer eligible for automatic login.
- The ViewModel scope is destroyed for a foreground-only operation.

Cancellation must not publish a late result.

## Account-switch safety

Account switching is the most important race in this design.

A recovery attempt for account A must retain the server ID, account ID, and session generation associated with A. If the user switches to account B while the attempt is running, the result from A must be discarded.

The switch operation should be transactional in this order:

1. Authenticate the selected profile using a manager and profile identity that belong to that profile.
2. Verify the selected account identity.
3. Persist the selected active profile and token together.
4. Increment or publish a new session generation.
5. Publish the new manager only after the profile is active.
6. Invalidate the old navigation graph and profile-owned state.
7. Disconnect the old manager after the new session is committed.

A failed switch must not leave the old manager visible under the selected profile name.

## Process-death safety

After process recreation, the app should treat all old in-memory objects as stale until the active profile is resolved again.

The recovery coordinator should not assume that a token shown in preferences is currently valid. It should establish a new transport and validate the token before publishing a manager.

Old ViewModels should either be destroyed with the old navigation graph or be prevented from issuing requests under a stale session generation. Hilt constructor injection alone does not update a ViewModel after an account switch.

Cached data may be displayed during this restoration process, but it should be treated as cached data until a successful authenticated request confirms the new session.

## Failure classification

The recovery system should preserve the following distinctions.

| Failure | Meaning | Default action |
| --- | --- | --- |
| Offline or unavailable server | Session validity is unknown | Do not clear credentials; allow bounded retry |
| WebSocket disconnected | Transport was lost | Reconnect before deciding authentication is invalid |
| Token returned false | Token did not authenticate | Try eligible credentials or request login |
| Token expired or revoked | Saved token is unusable | Refresh or use credentials according to policy |
| API key rejected | Saved API key is no longer valid | Do not retry indefinitely; request explicit login |
| Password rejected | Saved password is no longer valid | Do not retry indefinitely; request explicit login |
| OTP required | Interactive proof is required | Foreground OTP only; workers stop |
| `auth.me` identity mismatch | Authenticated identity is not the selected account | Reject the recovery result and protect the account boundary |
| Token persistence failure | Server session works but durable recovery is weaker | Use temporary authenticated state and expose the issue |
| Logout or account switch | Session was deliberately invalidated | Cancel pending recovery and reject late results |

## Avoiding recursion and duplicate recovery

The architecture must prevent the following loop:

1. A request fails.
2. The request layer starts recovery.
3. Recovery calls an authentication method.
4. The authentication method uses the generic request layer.
5. The generic layer interprets its failure as another reason to recover.
6. Recovery starts again.

The safe design is to keep authentication calls on a raw, non-recovering path. The recovery coordinator is the only component allowed to start a new authentication cycle, and it is protected by a mutex or single-flight operation.

Normal requests may ask for one recovery and one retry. They may not recursively request recovery from inside the recovery operation.

## Observability hypothesis

Recovery should be diagnosable without exposing secrets.

Useful non-sensitive diagnostics include:

- The recovery trigger: socket close, foreground, network, explicit retry, or request failure.
- The server and account IDs, or safe redacted identifiers.
- The session generation.
- The recovery outcome.
- Whether token, API key, or password recovery was attempted.
- Whether identity verification succeeded.
- Whether the result was durable or temporary.
- Retry count and backoff duration.
- Whether a late result was rejected because the account or generation changed.

Logs must not contain passwords, API keys, tokens, OTP values, or full server secrets.

## Phased implementation proposal

### Phase 1: Centralize page recovery

- Add a single foreground recovery operation to the session authority.
- Route explicit retry and pull-to-refresh actions through it.
- Preserve the existing bounded request-level recovery as a fallback.
- Verify Apps, Marketplace, Services, and Home behavior.

### Phase 2: Add transport and generation safety

- Track transport state separately from authentication state.
- Track a session generation for every account switch and logout.
- Reject late recovery results that belong to an old generation.
- Ensure stale ViewModels cannot publish old-session data.

### Phase 3: Add deferred recovery

- React to socket close and Android network events with bounded backoff.
- Add cancellation for logout, account switch, and process teardown.
- Make foreground and background policy explicit.

### Phase 4: Add durable diagnostics

- Record redacted recovery outcomes and timings.
- Add tests for races between socket loss, account switching, and process restoration.
- Confirm behavior with server reboot, token invalidation, router loss, and Android process-kill scenarios.

## Tests that should be required before approval

The implementation should not be approved without tests covering these scenarios.

- A socket closes while the device still reports network connectivity.
- A request fails on a dropped socket and is retried after token reauthentication.
- A dropped socket with an invalid token falls back to valid credentials.
- A dropped socket with rejected credentials does not loop.
- A dropped socket with OTP-required credentials opens the interactive flow only in the foreground.
- A worker receiving a dropped socket stops without accessing the wrong account.
- Two simultaneous page requests share one recovery operation.
- A retry from Apps or Marketplace uses the same recovery path as Home.
- A retry from Services uses the selected profile and not a stale active profile.
- A socket closes during an account switch and the old result is discarded.
- A socket closes during logout and cannot republish the session.
- A process is killed and restored with a valid token.
- A process is restored with an expired token and valid credentials.
- A token authenticates a different username than the selected account and is rejected.
- Token generation fails after successful authentication and yields temporary authenticated state.
- Repeated server failures use backoff and do not create a reconnect storm.
- Cached data remains visible only as stale data until a fresh request succeeds.

## Approval questions

Before implementation, these decisions should be confirmed.

1. Should every explicit retry and pull-to-refresh action in the application automatically invoke the shared recovery operation, or should some pages remain offline-only?
2. Should a dropped socket trigger recovery automatically in the background, or should background recovery wait for a network callback or worker execution?
3. Should password-based recovery be allowed in the background when the saved account does not require OTP, or should all password recovery be foreground-only?
4. Should a failed identity check after token login immediately clear the token, or should it preserve it for account-switch diagnosis while preventing its use?
5. Should the UI retain cached data during a failed reconnect, or replace it with an error state after the user presses Retry?
6. What delay and maximum backoff are acceptable for repeated server failures?
7. Should the account-switch operation always wait for the new session to be authenticated before replacing the old navigation graph?
8. Should a temporary authenticated session be visually distinguishable from a durable authenticated session?
9. Which redacted recovery events should be retained for diagnostics, and for how long?

## Hypothesis conclusion

The safest hypothesis is to make recovery a process-wide session operation triggered by all relevant lifecycle and request events, while leaving each page responsible only for its own data and retry presentation.

The central coordinator should own the recovery decision and the account-generation boundary. The manager should provide bounded transport and request retry. The repository should provide persisted profiles and credentials. ViewModels should not implement independent login logic.

This design would make Apps, Marketplace, Services, Home, and detail pages consistent while avoiding the main risks of this class of fix: recursive retries, duplicate logins, stale account data, infinite credential loops, and late recovery results overwriting a newer account switch.
