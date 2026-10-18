# Jetpack Compose Audit Report

Target: D:/Turnly
Date: 2026-10-18
Scope: app/src/main/java/com/crescentapps/whozit
Excluded from scoring: None
Confidence: High
Overall Score: 67/100

## Scorecard

| Category | Score | Weight | Status | Notes |
|----------|-------|--------|--------|-------|
| Performance | 6/10 | 35% | needs work | Capped by non-deferred animated reads in composition |
| State management | 7/10 | 25% | solid | Lazy layouts missing keys; UiState holding unstable lists |
| Side effects | 6/10 | 20% | needs work | Widespread unsafe `collectAsState` without lifecycle bounds |
| Composable API quality | 8/10 | 20% | solid | Good modifier forwarding and component layering |

## Critical Findings

1. **Side Effects: `collectAsState` without lifecycle awareness**
   - Why it matters: UI Flows must stop collecting when the screen is not STARTED to prevent background battery drain.
   - Evidence: `GaussianRoomScreens.kt`, `GaussianHomeScreen.kt`, `SettingsScreen.kt`, `M3HomeScreen.kt`, `MainActivity.kt`
   - Fix direction: Replace `.collectAsState()` with `.collectAsStateWithLifecycle()` from `androidx.lifecycle.compose`.
   - References: <https://developer.android.com/topic/architecture/ui-layer/state-production#continuous-vs-discrete>

2. **Performance: non-deferred animated reads in composition**
   - Why it matters: Reading animated values directly in the composition phase causes the subtree to recompose on every animation frame.
   - Evidence: `app/src/main/java/com/crescentapps/whozit/presentation/components/camera/QRScannerView.kt:236`, `app/src/main/java/com/crescentapps/whozit/presentation/gaussianblur/components/GaussianSwitch.kt:82`
   - Fix direction: Use lambda modifier `Modifier.offset { IntOffset(x, y) }` instead of `Modifier.offset(x = ...)` to defer reads to the layout phase.
   - References: <https://developer.android.com/develop/ui/compose/performance/bestpractices#defer-reads-as-long-as-possible>

3. **State Management / Performance: Lazy lists missing stable keys**
   - Why it matters: Omitting `key = { it.id }` forces Compose to use position-based index keys. When the list changes, Compose drops scroll positions, cancels animations, and reallocates items unnecessarily.
   - Evidence: `M3RoomsScreen.kt:188`, `M3HistoryScreen.kt:150`, `M3ScheduleDetailScreen.kt:213`
   - Fix direction: Provide a stable `key` parameter to `items(...)` blocks (e.g., `items(uiState.rooms, key = { it.id })`).
   - References: <https://developer.android.com/develop/ui/compose/performance/bestpractices#keys>

## Adjacent Findings

### Android Launch UX

- Android 12+ splash icon status: not configured
- Evidence: no `windowSplashScreenAnimatedIcon` found in project resources.
- Finding, if risky: N/A

## Category Details

### Performance — 6/10

**Ceiling check**

- Strong Skipping: ON (Kotlin 2.2.10, default enabled)
- Ceiling table applied: SSM-on
- Module-wide `skippable%`: 145/326 = 44.4%
- Named-only `skippable%`: 34/34 = 100% — zero-arg lambdas anchored the module-wide number.
- Unstable shared types from compiler: 11
- SSM-on binding evidence: None observed for identity churn, but capped by non-deferred animated reads.
- Qualitative score: 6/10
- Ceiling: cap at 6 (non-deferred animated reads recomposing on every frame)
- Applied score: 6/10

**What is working**

- Strong Skipping Mode handles named composable skipping perfectly (100% skippable named restartable composables).
- Expensive operations are correctly deferred away from composition blocks for standard UI components.

**What is hurting the score**

- Animated properties like `laserOffsetY` and `thumbOffset` are read inside the composition phase and passed into `Modifier.offset(x = ...)` / `Modifier.offset(y = ...)` directly, leading to every-frame recomposition.

**Animation performance signals**

- Status: risky
- Affected Performance because composition-phase animated reads were found on `QRScannerView.kt` and `GaussianSwitch.kt`.

**Paging list signals**

- Status: clean

**Evidence**

- `QRScannerView.kt:236` — animated `laserOffsetY` fed to `Modifier.offset(y = ...)` in composition · References: <https://developer.android.com/develop/ui/compose/performance/bestpractices#defer-reads-as-long-as-possible>
- `GaussianSwitch.kt:82` — animated `thumbOffset` fed to `Modifier.offset(x = ...)` in composition · References: <https://developer.android.com/develop/ui/compose/performance/bestpractices#defer-reads-as-long-as-possible>

### State Management — 7/10

**What is working**

- Most UiState properties are read-only and passed correctly from the ViewModel.
- Hoisting correctness is generally well respected across the app.

**What is hurting the score**

- Some `items(uiState.list)` calls omit `key =`, defaulting to positional indexes.
- `UiState` objects contain unstable `List<T>`, forcing reliance purely on Strong Skipping instance identity equality, which can risk churn if ViewModels construct new list instances.

**Paging load-state signals**

- Status: not present

**Evidence**

- `M3RoomsScreen.kt:188` — `items(uiState.rooms)` missing `key = { it.id }` · References: <https://developer.android.com/develop/ui/compose/performance/bestpractices#keys>
- `M3HistoryScreen.kt:150` — `items(uiState.filteredItems)` missing `key = { it.id }` · References: <https://developer.android.com/develop/ui/compose/performance/bestpractices#keys>

### Side Effects — 6/10

**What is working**

- `LaunchedEffect(key)` bindings generally look correct for triggering one-off actions on state change.
- No immediate evidence of rogue blocking work inside composition.

**What is hurting the score**

- Widespread usage of `.collectAsState()` instead of `.collectAsStateWithLifecycle()`. In Android UI code, flows must be collected with lifecycle bounds.

**Animation side-effect signals**

- Status: clean

**Paging side-effect signals**

- Status: clean

**Evidence**

- `GaussianRoomScreens.kt:53` — `viewModel.uiState.collectAsState()` · References: <https://developer.android.com/topic/architecture/ui-layer/state-production#continuous-vs-discrete>
- `SettingsScreen.kt:82` — `viewModel.effectivePreferences.collectAsState()` · References: <https://developer.android.com/topic/architecture/ui-layer/state-production#continuous-vs-discrete>

### Composable API Quality — 8/10

**What is working**

- Components accept `modifier: Modifier = Modifier` safely and forward it.
- Explicit `Shape` and `Color` parameters are cleanly exposed without internal shadowing.
- Layering across Liquid, Gaussian Blur, and Material 3 Expressive architectures demonstrates good separation of concerns.

**What is hurting the score**

- Minor formatting deviations (e.g., implicit `content` lambda parameter ordering).

**Evidence**

- `M3ExpressiveDock.kt` — solid modifier propagation · References: <https://developer.android.com/develop/ui/compose/api-guidelines#naming-modifiers>

## Prioritized Fixes

1. Replace `collectAsState()` with `collectAsStateWithLifecycle()` in all ViewModels emitting UI states (`SettingsScreen.kt:82`, `GaussianRoomScreens.kt:53`).
2. Update `Modifier.offset(x = ...)` inside `GaussianSwitch.kt:82` and `QRScannerView.kt:236` to lambda-based offset (`Modifier.offset { IntOffset(...) }`).
3. Add `key = { it.id }` to lazy layout `items()` calls in `M3RoomsScreen.kt:188` and `M3HistoryScreen.kt:150`.

## Notes And Limits

- Entire app module was audited.
- Confidence is High.
- Adjacent coverage notes: none observed
- Android Launch UX resources: not configured
- Strong Skipping mode: on across modules
- Weight choice: default 35/25/20/20
- Renormalization: none
- Compiler diagnostics used: yes — `compose-reports.init.gradle` ran successfully. Module-wide and named-only `skippable%` were extracted. SSM-on table was applied.

## Suggested Follow-Up

- Run `compose-agent focus on animation` if animation findings need a file-scope rewrite rather than an audit-level diagnosis.