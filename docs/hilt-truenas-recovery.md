# Hilt Migration and TrueNAS Recovery Implementation

## Status

The Hilt migration is integrated into `v0.8.1-rc` and the follow-up recovery work is being developed on `recovery/truenas-session`.

Base integration:

- Base branch: `v0.8.1-rc`
- Merge commit: `a0f43a9`
- Recovery branch: `recovery/truenas-session`
- Experimental branch removed locally and remotely

The current branch contains the DI migration and the implemented recovery foundation. The remaining work is device-level validation, broader cache invalidation, and handling any behavior exposed by testing.

## First recovery slice

The first recovery implementation is now present on `recovery/truenas-session`:

- `auth.login_with_token` and `auth.login_with_api_key` Boolean results are checked explicitly; `false` is not treated as success.
- `auth.login_ex` credential failures map to a non-retryable credentials outcome, while transport/API errors remain retryable.
- OTP-required results remain distinct.
- Successful authentication with token-generation or local token-storage failure is represented as a temporary authenticated session.
- `SessionProvider.open()` records the selected server/account as last-used before saving the new current session.
- Account-switch, worker, widget, and App Function consumers accept both durable and temporary authenticated managers.
- `MainViewModel.attemptLoginWithToken()` now verifies the Boolean token-login result before publishing a manager.
- Authentication methods now use a non-recovering raw API call path, preventing recovery from recursively entering `TrueNASApiManager.recoveryMutex`.
- Direct JSON-RPC `-32001` method errors are no longer treated as authentication failures; nested `207` authentication errors and explicit authentication messages remain recovery signals.
- Pure JVM tests cover Boolean authentication results, credential rejection classification, OTP mapping, and JSON-RPC authentication-error classification.

The recovery coordinator, network/background state machine, and per-account session ownership are now implemented. The remaining work is device-level validation, broader cache invalidation, and handling any behavior exposed by testing.

## Implemented Hilt setup

Hilt is configured in the root and app build files:

- Hilt Gradle plugin
- Hilt Android runtime
- Hilt compiler through KSP
- AndroidX Hilt compiler
- Hilt lifecycle ViewModel Compose support
- Hilt WorkManager support

Application and Android entry points are configured in:

- `app/src/main/java/com/imnotndesh/truehub/TrueHubApplication.kt`
- `app/src/main/java/com/imnotndesh/truehub/MainActivity.kt`

`TrueHubApiManager` is now provided through the authenticated `AppModule` binding:

- `app/src/main/java/com/imnotndesh/truehub/di/AppModule.kt`
- `app/src/main/java/com/imnotndesh/truehub/data/helpers/SessionCoordinator.kt`
- `app/src/main/java/com/imnotndesh/truehub/data/helpers/SessionProvider.kt`

`SessionCoordinator` is the process-local source of truth for the active authenticated manager. `SessionProvider` handles profile resolution, authentication outcomes, token-first recovery, and credential fallback. Persisted servers, accounts, credentials, and tokens remain owned by the session repository.

## Migrated ViewModels

### Homepage

All ViewModels under `app/src/main/java/com/imnotndesh/truehub/ui/homepage` were migrated from custom factories to Hilt constructor injection.

Examples include:

- `HomeViewModel` with `@ApplicationContext Context`
- Pool and dataset ViewModels
- Settings and audit ViewModels
- Boot and network ViewModels
- User and API key ViewModels
- Alert service ViewModels
- Service detail ViewModel using Hilt assisted injection
- Alert service detail ViewModel using `SavedStateHandle` for `serviceId`

The existing composable signatures, route arguments, callback behavior, and UI code were preserved.

### Services

The following services ViewModels were migrated:

- `AppsScreenViewModel`
- `AppDetailsViewModel`
- `AppResourceUsageViewModel`
- `ContainerScreenViewModel`
- `VmsScreenViewModel`

Manual factories were removed and Compose acquisition was changed to `hiltViewModel()`.

Existing ViewModel keys were preserved for detail screens where appropriate:

- App details use the app name.
- App resource usage uses the app ID.
- Alert service detail uses the service ID.
- VM and container list screens remain unkeyed within their navigation owners.

`AppResourceUsageViewModel` receives `appId` from the navigation `SavedStateHandle`.

VM and container detail screens do not have their own ViewModels. They receive their selected model through existing data-holder navigation and were not changed as part of the DI conversion.

### Login, setup, and account switcher

#### Login

`LoginScreenViewModel` uses Hilt assisted injection:

- The nullable `TrueNASApiManager` remains an assisted runtime input.
- The application context is injected with `@ApplicationContext`.
- Login ViewModel creation uses the Hilt assisted factory.
- Existing login, OTP, credential, and navigation behavior was preserved.

#### Setup

`SetupScreenViewModel` is a Hilt ViewModel with no injected manager dependency. The server setup bottom sheet now obtains it with `hiltViewModel()` instead of constructing it with `remember`.

#### Account switcher

`AccountSwitcherViewModel` owns the existing account-switcher state and persistence operations:

- Loading saved servers
- Loading account profiles
- Reloading profiles
- Deleting one account
- Deleting all accounts
- Saving a new server

The account-switcher composable retains its dialog, menu, and setup-sheet visibility state. Existing callbacks and navigation behavior were preserved.

## TrueNAS API constraints

The recovery design must follow the configured TrueNAS API contract rather than assume REST authentication behavior.

The current TrueNAS API documentation exposes JSON-RPC over WebSocket and provides these authentication methods:

- `core.ping`
- `auth.login_with_token`
- `auth.login_with_api_key`
- `auth.login`
- `auth.login_ex`
- `auth.login_ex_continue`
- `auth.generate_token`
- `auth.me`
- `auth.logout`
- `auth.sessions`
- `auth.terminate_session`
- `auth.terminate_other_sessions`

Important contract details:

- `auth.login_with_token` returns a Boolean; `false` means the token did not authenticate.
- `auth.login_with_api_key` returns a Boolean.
- `auth.login_ex` returns typed responses including `SUCCESS`, `AUTH_ERR`, `EXPIRED`, `OTP_REQUIRED`, and `REDIRECT`.
- `auth.login_ex_continue` is the supported way to continue an OTP-required login attempt.
- `auth.me` can verify the account authenticated by a token.
- `auth.logout` deauthenticates the app and removes its token from the session.
- `auth.generate_token` accepts a TTL and origin policy.
- Tokens can be invalidated after inactivity unless configured with an appropriate TTL policy.
- `match_origin` defaults to `true`, so a token may fail after an origin or IP change.
- `core.ping` is unauthenticated and rate-limited. It is useful for connection liveness, not authentication validation.
- JSON-RPC errors `-32000` and `-32001` are not automatically authentication failures.
- The API does not support batch JSON-RPC requests.

## Recovery design

The recovery implementation on `recovery/truenas-session` follows the configured TrueNAS API contract and keeps authentication state separate from the generic API call path.

### Session states

The recovery coordinator should model these states explicitly:

```text
Uninitialized
ResolvingProfile
Offline
Connecting
ValidatingToken
Reauthenticating
OtpRequired
Authenticated
AuthenticatedTemporary
Unauthenticated
```

### Recovery policy

1. Resolve the active server/account profile.
2. Validate that the server, account, and credential relationship is intact.
3. Connect directly to the configured TrueNAS server.
4. Attempt `auth.login_with_token`.
5. Verify the resulting identity with `auth.me`.
6. If the token is invalid and automatic login is allowed, use the saved API key or password.
7. If the password requires OTP, transition to interactive OTP recovery.
8. If credential authentication succeeds but token generation or storage fails, use the authenticated manager temporarily and expose the persistence failure.
9. Keep network failures separate from authentication failures.
10. Never retry an invalid credential indefinitely.

### Saved credential policy

- `autoLoginEnabled = true`: allow startup, foreground, worker, and background recovery.
- `autoLoginEnabled = false`: do not automatically authenticate; require explicit user login or account selection.
- API key credentials: eligible for unattended background recovery.
- Password credentials: eligible for foreground recovery and background recovery when the server does not require OTP.
- OTP credentials: interactive only. Workers should report `AuthenticationRequired` and stop.
- No usable credentials: route to login without reusing the previous account manager.

### Account switching

Account switching should be transactional:

1. Keep the current session active while authenticating the selected profile.
2. Use the selected server/account identity throughout the attempt.
3. Handle OTP explicitly for the selected account.
4. Persist the selected profile and token together.
5. Publish the new manager and session generation together.
6. Invalidate the previous navigation graph, feature ViewModels, workers, and profile-owned caches.

A failed switch must not pass the old manager to the login screen as if it belonged to the selected account.

### Workers, widgets, and App Functions

Workers should carry `serverId` and `accountId` in their input data rather than resolving the current last-used account at execution time.

Widgets may render cached data without authentication, but widget caches must be scoped by server/account. Authenticated widget actions should use the shared recovery coordinator.

App Functions should use an explicit active profile or a caller-provided profile and should return typed outcomes instead of collapsing all failures into a generic authentication error.

## Hilt architecture for recovery

The recovery implementation introduces a singleton coordinator:

```text
SessionCoordinator
├── active server/account identity
├── current manager
├── SessionState StateFlow
├── network state
├── token state
├── recovery mutex/job
├── account-switch operation
└── logout operation
```

It should be injected into:

- `MainViewModel`
- Login, setup, and account-switcher ViewModels
- Hilt workers
- Widget configuration and action components
- App Functions
- Feature ViewModels that need session state

A separate session repository should own:

- Saved servers
- Saved accounts
- Per-account credentials
- Per-account tokens
- Active profile
- Token metadata
- Auto-login policy

Credentials and tokens should eventually be moved to keystore-backed storage. The current `EncryptedPrefs` implementation uses ordinary Preferences DataStore and should not be treated as encrypted credential storage.

## Remaining considerations

- Confirm widget and cached UI data invalidation for every account-switch path.
- Decide whether users should remain on the previous authenticated screen when a profile is disabled.
- Validate error-message localization for durable failures, OTP prompts, and connection errors.
- Run device-level testing for expired sessions, server reboots, token invalidation, account switching, and background workers.

## Verification

The migrated code and recovery implementation were checked with:

```bash
JAVA_HOME=/home/brian/.local/share/JetBrains/Toolbox/apps/android-studio/jbr \
./gradlew :app:testGithubDebugUnitTest :app:compileGithubDebugKotlin
```

The focused JVM tests, Hilt Java compilation, and Kotlin compile check completed successfully. APK assembly and installation remain for the final device validation stage.
