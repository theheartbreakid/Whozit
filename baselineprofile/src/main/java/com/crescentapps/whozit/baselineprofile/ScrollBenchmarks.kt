package com.crescentapps.whozit.baselineprofile

import android.content.Intent
import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.TraceSectionMetric
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalMetricApi::class)
@RunWith(AndroidJUnit4::class)
class ScrollBenchmarks {
    @get:Rule val rule = MacrobenchmarkRule()

    private fun scrollHomeScenario(
        uiSystemMode: String,
        rendererTestTag: String,
        compilationMode: CompilationMode
    ) {
        rule.measureRepeated(
            packageName = "com.crescentapps.whozit",
            metrics = listOf(TraceSectionMetric("Choreographer#doFrame%")),
            iterations = 5,
            startupMode = StartupMode.WARM,
            compilationMode = compilationMode,
            setupBlock = {
                pressHome()
                // Dedicated benchmark fixture setup in setupBlock outside measureBlock
                startActivityAndWait { intent ->
                    intent.setClassName("com.crescentapps.whozit", "com.crescentapps.whozit.MainActivity")
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.putExtra("ui_system_mode", uiSystemMode)
                    intent.putExtra("seed_demo_data", true)
                }
                // Dismiss permission dialogs if present
                device.executeShellCommand("pm grant com.crescentapps.whozit android.permission.POST_NOTIFICATIONS")
                try {
                    val allowBtn = device.findObject(By.res("com.android.permissioncontroller:id/permission_allow_button"))
                    allowBtn?.click()
                } catch (_: Exception) {}
                device.waitForIdle()

                // Assert intended renderer is active
                device.wait(Until.findObject(By.res(rendererTestTag)), 5000)
                // Wait for DB seeding and Today List content to settle fully in setupBlock
                device.wait(Until.findObject(By.res("today_list")), 5000)
                device.waitForIdle()
            }
        ) {
            startActivityAndWait { intent ->
                intent.setClassName("com.crescentapps.whozit", "com.crescentapps.whozit.MainActivity")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                intent.putExtra("ui_system_mode", uiSystemMode)
            }

            val todayList = device.wait(Until.findObject(By.res("today_list")), 3000)
            if (todayList != null) {
                todayList.setGestureMargin(device.displayWidth / 5)
                todayList.fling(Direction.DOWN)
                device.waitForIdle()
                todayList.fling(Direction.UP)
                device.waitForIdle()
            } else {
                device.swipe(
                    device.displayWidth / 2,
                    (device.displayHeight * 0.8).toInt(),
                    device.displayWidth / 2,
                    (device.displayHeight * 0.2).toInt(),
                    10
                )
                device.waitForIdle()
                device.swipe(
                    device.displayWidth / 2,
                    (device.displayHeight * 0.2).toInt(),
                    device.displayWidth / 2,
                    (device.displayHeight * 0.8).toInt(),
                    10
                )
                device.waitForIdle()
            }
        }
    }

    // Liquid UI Benchmarks
    @Test
    fun scrollLiquidHomeBaselineProfile() = scrollHomeScenario(
        uiSystemMode = "LIQUID",
        rendererTestTag = "renderer_liquid",
        compilationMode = CompilationMode.Partial(BaselineProfileMode.Require)
    )

    @Test
    fun scrollLiquidHomeCompilationNone() = scrollHomeScenario(
        uiSystemMode = "LIQUID",
        rendererTestTag = "renderer_liquid",
        compilationMode = CompilationMode.None()
    )

    // Gaussian Blur Benchmarks
    @Test
    fun scrollGaussianHomeBaselineProfile() = scrollHomeScenario(
        uiSystemMode = "GAUSSIAN_BLUR",
        rendererTestTag = "renderer_gaussian_blur",
        compilationMode = CompilationMode.Partial(BaselineProfileMode.Require)
    )

    @Test
    fun scrollGaussianHomeCompilationNone() = scrollHomeScenario(
        uiSystemMode = "GAUSSIAN_BLUR",
        rendererTestTag = "renderer_gaussian_blur",
        compilationMode = CompilationMode.None()
    )

    // Material 3 Expressive Benchmarks
    @Test
    fun scrollExpressiveHomeBaselineProfile() = scrollHomeScenario(
        uiSystemMode = "MATERIAL_3_EXPRESSIVE",
        rendererTestTag = "renderer_material_3_expressive",
        compilationMode = CompilationMode.Partial(BaselineProfileMode.Require)
    )

    @Test
    fun scrollExpressiveHomeCompilationNone() = scrollHomeScenario(
        uiSystemMode = "MATERIAL_3_EXPRESSIVE",
        rendererTestTag = "renderer_material_3_expressive",
        compilationMode = CompilationMode.None()
    )
}
