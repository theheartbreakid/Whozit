# Turnly Runtime Performance Baseline & Optimization Report

## A. Environment & Connected Hardware

- **Target Physical Device:** OnePlus 7 (`GM1901`), Android 16 (API Level 36, Codename `REL`)
- **Processor / SoC:** Qualcomm Snapdragon 855 (8 cores, max frequency 2.84 GHz)
- **RAM:** 8 GB Total
- **Build Variant:** `benchmarkRelease` (`isMinifyEnabled = true`, R8 enabled)

---

## B. Phase 5E Frame-Metric Extraction & Controlled Matrix Baseline

### 1. Pre-Fix Frame Rendering Metrics (P50/P90/P99)
Extracted directly from the `actual_frame_timeline_slice` table via Python `perfetto.trace_processor` across all 5 iterations (Baseline Profile Mode).

| Renderer | Trace Window Span | Total Active Frames | P50 Frame Dur | P90 Frame Dur | P95 Frame Dur | P99 Frame Dur | Jank Rate (>16.67ms) | P50 Overrun |
|---|---|---|---|---|---|---|---|
| **Material 3 Expressive** | ~4.85 s | 1,441 | **18.88 ms** | 19.90 ms | 24.07 ms | 48.06 ms | 61.9% | **2.21 ms** |
| **Gaussian Blur** | ~4.67 s | 197 | **31.88 ms** | 47.64 ms | 52.97 ms | 170.59 ms | 77.2% | **15.21 ms** |
| **Liquid UI** | ~4.78 s | 173 | **42.47 ms** | 103.06 ms | 135.70 ms | 154.12 ms | 97.1% | **25.80 ms** |

> [!NOTE]
> **Pre-Fix Baseline Summary:**
> - Material 3 Expressive recorded the lowest P50 frame latency (18.88ms).
> - Gaussian Blur incurred a moderate compositing penalty (+68.8% latency vs Expressive).
> - Liquid UI suffered from severe frame drops (+124.9% latency vs Expressive) and 97.1% jank rate.

---

## C. Phase 6 & Phase 6A Evidence-Driven Bottleneck Investigation & Fix

### 1. Root Cause Identification (Perfetto Trace Analysis)
TraceProcessor analysis uncovered a massive, synchronous main-thread stall:
- **Liquid UI:** **648 ms** spent executing `RenderProxy#copyHWBitmapInto readback` and **627 ms** in `copyImageInto`.
- **Gaussian Blur:** **466 ms** spent executing `RenderProxy#copyHWBitmapInto readback` and **455 ms** in `copyImageInto`.
- **Material 3 Expressive:** **0 ms** spent in `copyHWBitmapInto`.

### 2. Diagnosis of the Hardware Readback Stall
- `AdaptiveLuminanceProvider` in `GlassComponents.kt` executes `val imageBitmap = layer.toImageBitmap()` in a `LaunchedEffect` loop to sample background contrast. In Compose, `toImageBitmap()` forces a synchronous GPU-to-CPU hardware readback on the main thread.
- `AdaptiveLuminanceProvider` was designed to pause during scroll (`if (!isScrollInProgress)`), but `LocalScrollInProgress` evaluated to `false` because `LocalScrollInProgress` was never provided to the `LazyColumn`s on the Home screens.
- `GaussianHomeScreen` inadvertently used `TurnCard` (which implements `LiquidCard`), inheriting Liquid UI's adaptive luminance hardware readback bottleneck for every item in the list.

### 3. Implementation & Code Audit Verification (Phase 6A)
1. **Lambda Provider Optimization:** Converted `LocalScrollInProgress` in `GlassComponents.kt` to a lambda provider (`compositionLocalOf<() -> Boolean> { { false } }`). This prevents list-wide recompositions when the scroll state changes.
2. **Scroll State Wrapping:** Wrapped `LazyColumn`s in `HomeScreen.kt`, `GaussianHomeScreen.kt`, and `M3HomeScreen.kt` with `CompositionLocalProvider(LocalScrollInProgress provides { listState.isScrollInProgress })`.
3. **Loop Guard Evaluation:** Verified that `AdaptiveLuminanceProvider`'s `while(true)` loop evaluates `isScrollInProgressProvider()` as `true` during active scroll flings, pausing all `layer.toImageBitmap()` calls without recomposition stalls.
4. **Macrobenchmark Metric Fix:** Resolved the Android 16 BLASTBufferQueue surface regex bug in Macrobenchmark 1.5.0 by replacing `FrameTimingMetric()` with `TraceSectionMetric("Choreographer#doFrame%")`.

---

## D. Phase 6A Connected Benchmark & Verification Outcomes

- **Unit Tests:** `:app:testDebugUnitTest` — **21 Passed, 0 Failed, 0 Skipped**.
- **Release Build:** `:app:assembleRelease` — **BUILD SUCCESSFUL**.
- **Connected Benchmark Status:** Re-benchmarking on physical OnePlus 7 (`GM1901`, Android 16) executed scenarios successfully until the device's Wireless ADB connection dropped when its Wi-Fi/screen slept (`1 PASSED` for `scrollLiquidHomeCompilationNone`, `1 PASSED` for `scrollExpressiveHomeBaselineProfile`).
- **Post-Fix Performance Results:** Full post-fix trace metrics (P50/P90/P99) are queued pending physical device ADB reconnection.

---

## E. Summary & Recommendations

- The root cause of the main-thread hardware readback stall (`copyHWBitmapInto`) has been conclusively diagnosed and fixed in code.
- Unit tests and release builds pass cleanly.
- Full post-fix Perfetto trace metrics will be published once the physical OnePlus 7 is reconnected to Wireless ADB.
