import os
import glob
import math
import hashlib
from perfetto.trace_processor import TraceProcessor

# -----------------------------------------------------------------------------
# Rigorous Percentile Function (Nearest-Rank / Linear Interpolation on Sorted Data)
# -----------------------------------------------------------------------------
def percentile(data, p):
    """
    Computes percentile p (0.0 to 1.0) using linear interpolation on sorted data.
    Guarantees non-decreasing monotonicity: P50 <= P90 <= P95 <= P99.
    """
    if not data:
        return 0.0
    s_data = sorted(data)
    n = len(s_data)
    if n == 1:
        return float(s_data[0])

    k = (n - 1) * p
    f = math.floor(k)
    c = math.ceil(k)
    if f == c:
        val = float(s_data[int(k)])
    else:
        val = float(s_data[int(f)] * (c - k) + s_data[int(c)] * (k - f))

    assert math.isfinite(val), f"Non-finite percentile calculated: {val}"
    assert s_data[0] <= val <= s_data[-1], f"Percentile {val} out of bounds [{s_data[0]}, {s_data[-1]}]"
    return val

def test_percentile_function():
    """Deterministic unit test suite for percentile calculation."""
    # 1. Unsorted distinct array
    data_unsorted = [100.0, 10.0, 50.0, 20.0, 80.0, 30.0, 90.0, 40.0, 70.0, 60.0]
    p0 = percentile(data_unsorted, 0.0)
    p50 = percentile(data_unsorted, 0.5)
    p90 = percentile(data_unsorted, 0.9)
    p95 = percentile(data_unsorted, 0.95)
    p99 = percentile(data_unsorted, 0.99)
    p100 = percentile(data_unsorted, 1.0)

    assert p0 == 10.0, f"Expected 10.0, got {p0}"
    assert p100 == 100.0, f"Expected 100.0, got {p100}"
    assert p0 <= p50 <= p90 <= p95 <= p99 <= p100, f"Monotonicity failed: {p0}, {p50}, {p90}, {p95}, {p99}, {p100}"

    # 2. Duplicate values
    data_dups = [15.0, 15.0, 15.0, 15.0, 15.0]
    assert percentile(data_dups, 0.5) == 15.0
    assert percentile(data_dups, 0.99) == 15.0

    # 3. Single-element array
    data_single = [42.0]
    assert percentile(data_single, 0.5) == 42.0
    assert percentile(data_single, 0.9) == 42.0

    # 4. Empty input
    assert percentile([], 0.5) == 0.0

    print("Percentile unit test suite passed successfully!")

def get_file_md5(path):
    hash_md5 = hashlib.md5()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(4096), b""):
            hash_md5.update(chunk)
    return hash_md5.hexdigest()

# -----------------------------------------------------------------------------
# Trace Inventory Definitions
# -----------------------------------------------------------------------------
SCENARIOS = [
    ("Liquid UI (Unprofiled)", "ScrollBenchmarks_scrollLiquidHomeCompilationNone_iter*.perfetto-trace"),
    ("Liquid UI (Baseline Profile)", "ScrollBenchmarks_scrollLiquidHomeBaselineProfile_iter*.perfetto-trace"),
    ("Gaussian Blur (Unprofiled)", "ScrollBenchmarks_scrollGaussianHomeCompilationNone_iter*.perfetto-trace"),
    ("Gaussian Blur (Baseline Profile)", "ScrollBenchmarks_scrollGaussianHomeBaselineProfile_iter*.perfetto-trace"),
    ("M3 Expressive (Unprofiled)", "ScrollBenchmarks_scrollExpressiveHomeCompilationNone_iter*.perfetto-trace"),
    ("M3 Expressive (Baseline Profile)", "ScrollBenchmarks_scrollExpressiveHomeBaselineProfile_iter*.perfetto-trace"),
]

BASE_DIR_7 = "D:/Turnly/baselineprofile/build/outputs/connected_android_test_additional_output/benchmarkRelease/connected"

def find_traces(base_dir):
    scenario_files = {}
    for sc_name, glob_pat in SCENARIOS:
        matches = sorted(glob.glob(os.path.join(base_dir, "*", glob_pat)))
        scenario_files[sc_name] = matches
    return scenario_files

def analyze_single_trace(path):
    tp = TraceProcessor(path)

    # Extract gesture input window
    gesture_qr = tp.query('''
        SELECT
            min(s.ts) as min_input_ts,
            max(s.ts + s.dur) as max_input_ts
        FROM slice s
        JOIN thread_track tt ON s.track_id = tt.id
        JOIN thread t ON tt.utid = t.utid
        JOIN process p ON t.upid = p.upid
        WHERE p.name = 'com.crescentapps.whozit'
          AND s.name LIKE 'dispatchInputEvent%'
    ''')
    g_row = list(gesture_qr)[0]
    min_input_ts = g_row.min_input_ts if g_row.min_input_ts else 0
    max_input_ts = g_row.max_input_ts if g_row.max_input_ts else 0
    fling_end_ts = max_input_ts + 1_000_000_000 # 1s after last input event

    # Query frames on com.crescentapps.whozit MainActivity layer
    frame_qr = tp.query('''
        SELECT
            a.ts,
            a.dur / 1e6 as act_dur_ms,
            e.dur / 1e6 as exp_dur_ms,
            (a.dur - e.dur) / 1e6 as overrun_ms,
            a.jank_type,
            a.present_type,
            a.layer_name
        FROM actual_frame_timeline_slice a
        JOIN expected_frame_timeline_slice e ON a.surface_frame_token = e.surface_frame_token
        JOIN process p ON a.upid = p.upid
        WHERE p.name = 'com.crescentapps.whozit'
          AND a.layer_name LIKE '%MainActivity%'
          AND a.dur > 0
        ORDER BY a.ts
    ''')

    frames_all = []
    frames_fling = []

    for r in frame_qr:
        item = {
            "ts": r.ts,
            "act_dur_ms": r.act_dur_ms,
            "exp_dur_ms": r.exp_dur_ms,
            "overrun_ms": r.overrun_ms,
            "jank_type": r.jank_type,
            "present_type": r.present_type,
            "layer_name": r.layer_name
        }
        frames_all.append(item)
        if min_input_ts <= r.ts <= fling_end_ts:
            frames_fling.append(item)

    # Readback slices
    rb_qr = tp.query('''
        SELECT
            s.ts,
            s.dur / 1e6 as dur_ms,
            s.name
        FROM slice s
        JOIN thread_track tt ON s.track_id = tt.id
        JOIN thread t ON tt.utid = t.utid
        JOIN process p ON t.upid = p.upid
        WHERE p.name = 'com.crescentapps.whozit'
          AND (s.name LIKE '%copyHWBitmapInto%' OR s.name LIKE '%copyImageInto%')
    ''')

    rb_all = []
    rb_fling = []
    rb_idle = []

    for r in rb_qr:
        rb_item = {"ts": r.ts, "dur_ms": r.dur_ms, "name": r.name}
        rb_all.append(rb_item)
        if min_input_ts <= r.ts <= fling_end_ts:
            rb_fling.append(rb_item)
        else:
            rb_idle.append(rb_item)

    # RenderThread flush layers
    flush_qr = tp.query('''
        SELECT s.dur / 1e6 as dur_ms
        FROM slice s
        JOIN thread_track tt ON s.track_id = tt.id
        JOIN thread t ON tt.utid = t.utid
        JOIN process p ON t.upid = p.upid
        WHERE p.name = 'com.crescentapps.whozit' AND t.name LIKE '%RenderThread%' AND s.name = 'flush layers'
    ''')
    flush_durs = [r.dur_ms for r in flush_qr if r.dur_ms and r.dur_ms > 0]

    return {
        "file": os.path.basename(path),
        "frames_all": frames_all,
        "frames_fling": frames_fling,
        "rb_all": rb_all,
        "rb_fling": rb_fling,
        "rb_idle": rb_idle,
        "flush_durs": flush_durs,
    }

def summarize_frame_list(frame_list):
    if not frame_list:
        return {
            "count": 0, "p50": 0.0, "p90": 0.0, "p95": 0.0, "p99": 0.0,
            "overrun_p50": 0.0, "native_jank_rate": 0.0, "jank_16ms_rate": 0.0, "jank_33ms_rate": 0.0
        }
    durs = [f["act_dur_ms"] for f in frame_list]
    overruns = [f["overrun_ms"] for f in frame_list]
    native_janks = sum(1 for f in frame_list if f["jank_type"] and f["jank_type"] != "None")
    janks_16ms = sum(1 for f in frame_list if f["act_dur_ms"] > 16.67)
    janks_33ms = sum(1 for f in frame_list if f["act_dur_ms"] > 33.33)

    n = len(frame_list)
    p50 = percentile(durs, 0.5)
    p90 = percentile(durs, 0.9)
    p95 = percentile(durs, 0.95)
    p99 = percentile(durs, 0.99)

    # Assert non-decreasing monotonicity
    assert p50 <= p90 <= p95 <= p99, f"Non-decreasing monotonicity error: P50={p50}, P90={p90}, P95={p95}, P99={p99}"

    return {
        "count": n,
        "p50": p50,
        "p90": p90,
        "p95": p95,
        "p99": p99,
        "overrun_p50": percentile(overruns, 0.5),
        "native_jank_rate": (native_janks / n) * 100.0,
        "jank_16ms_rate": (janks_16ms / n) * 100.0,
        "jank_33ms_rate": (janks_33ms / n) * 100.0,
    }

if __name__ == "__main__":
    test_percentile_function()

    print("\n=== Phase 7 Trace Inventory & Recomputed Analysis ===")
    traces = find_traces(BASE_DIR_7)

    for sc_name, files in traces.items():
        all_frames_scenario = []
        fling_frames_scenario = []
        flush_durs_scenario = []
        total_rb_count = 0
        total_rb_dur = 0.0
        fling_rb_count = 0
        fling_rb_dur = 0.0
        idle_rb_count = 0
        idle_rb_dur = 0.0

        for f in files:
            res = analyze_single_trace(f)
            all_frames_scenario.extend(res["frames_all"])
            fling_frames_scenario.extend(res["frames_fling"])
            flush_durs_scenario.extend(res["flush_durs"])

            total_rb_count += len(res["rb_all"])
            total_rb_dur += sum(x["dur_ms"] for x in res["rb_all"])

            fling_rb_count += len(res["rb_fling"])
            fling_rb_dur += sum(x["dur_ms"] for x in res["rb_fling"])

            idle_rb_count += len(res["rb_idle"])
            idle_rb_dur += sum(x["dur_ms"] for x in res["rb_idle"])

        summary_all = summarize_frame_list(all_frames_scenario)
        summary_fling = summarize_frame_list(fling_frames_scenario)
        avg_flush = (sum(flush_durs_scenario) / len(flush_durs_scenario)) if flush_durs_scenario else 0.0

        print(f"\nScenario: {sc_name} ({len(files)} files)")
        print(f"  FULL TRACE WINDOW: Frames={summary_all['count']} | P50={summary_all['p50']:.2f}ms | P90={summary_all['p90']:.2f}ms | P95={summary_all['p95']:.2f}ms | P99={summary_all['p99']:.2f}ms | Overrun P50={summary_all['overrun_p50']:.2f}ms")
        print(f"  ACTIVE FLING WINDOW: Frames={summary_fling['count']} | P50={summary_fling['p50']:.2f}ms | P90={summary_fling['p90']:.2f}ms | P95={summary_fling['p95']:.2f}ms | P99={summary_fling['p99']:.2f}ms | Overrun P50={summary_fling['overrun_p50']:.2f}ms")
        print(f"  JANK RATES (Full Window): Native={summary_all['native_jank_rate']:.1f}% | >16.67ms={summary_all['jank_16ms_rate']:.1f}% | >33.33ms={summary_all['jank_33ms_rate']:.1f}%")
        print(f"  FLUSH LAYERS: Avg={avg_flush:.2f}ms")
        print(f"  READBACKS: Total={total_rb_count} ({total_rb_dur:.1f}ms) | Fling={fling_rb_count} ({fling_rb_dur:.1f}ms) | Idle={idle_rb_count} ({idle_rb_dur:.1f}ms)")
