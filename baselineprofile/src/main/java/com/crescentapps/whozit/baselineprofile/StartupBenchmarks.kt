package com.crescentapps.whozit.baselineprofile

import android.content.Intent
import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.TraceSectionMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalMetricApi::class)
@RunWith(AndroidJUnit4::class)
class StartupBenchmarks {
    @get:Rule val rule = MacrobenchmarkRule()

    @Test
    fun startupColdCompilationNone() = rule.measureRepeated(
        packageName = "com.crescentapps.whozit",
        metrics = listOf(TraceSectionMetric("ActivityThreadMain")),
        iterations = 5,
        startupMode = StartupMode.COLD,
        compilationMode = CompilationMode.None(),
        setupBlock = {
            killProcess()
        }
    ) {
        pressHome()
        startActivityAndWait { intent ->
            intent.setClassName("com.crescentapps.whozit", "com.crescentapps.whozit.MainActivity")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    @Test
    fun startupColdCompilationBaselineProfile() = rule.measureRepeated(
        packageName = "com.crescentapps.whozit",
        metrics = listOf(TraceSectionMetric("ActivityThreadMain")),
        iterations = 5,
        startupMode = StartupMode.COLD,
        compilationMode = CompilationMode.Partial(BaselineProfileMode.Require),
        setupBlock = {
            killProcess()
        }
    ) {
        pressHome()
        startActivityAndWait { intent ->
            intent.setClassName("com.crescentapps.whozit", "com.crescentapps.whozit.MainActivity")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    @Test
    fun startupWarmCompilationNone() = rule.measureRepeated(
        packageName = "com.crescentapps.whozit",
        metrics = listOf(TraceSectionMetric("ActivityThreadMain")),
        iterations = 5,
        startupMode = StartupMode.WARM,
        compilationMode = CompilationMode.None(),
        setupBlock = {
            pressHome()
        }
    ) {
        startActivityAndWait { intent ->
            intent.setClassName("com.crescentapps.whozit", "com.crescentapps.whozit.MainActivity")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    @Test
    fun startupWarmCompilationBaselineProfile() = rule.measureRepeated(
        packageName = "com.crescentapps.whozit",
        metrics = listOf(TraceSectionMetric("ActivityThreadMain")),
        iterations = 5,
        startupMode = StartupMode.WARM,
        compilationMode = CompilationMode.Partial(BaselineProfileMode.Require),
        setupBlock = {
            pressHome()
        }
    ) {
        startActivityAndWait { intent ->
            intent.setClassName("com.crescentapps.whozit", "com.crescentapps.whozit.MainActivity")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}