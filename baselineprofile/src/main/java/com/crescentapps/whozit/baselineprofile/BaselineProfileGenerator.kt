package com.crescentapps.whozit.baselineprofile

import android.content.Intent
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule val rule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() = rule.collect(
        packageName = "com.crescentapps.whozit",
        includeInStartupProfile = true
    ) {
        pressHome()
        startActivityAndWait { intent ->
            intent.setClassName("com.crescentapps.whozit", "com.crescentapps.whozit.MainActivity")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        val todayList = device.wait(androidx.test.uiautomator.Until.findObject(By.res("today_list")), 3000)
        if (todayList != null) {
            todayList.setGestureMargin(device.displayWidth / 5)
            todayList.fling(Direction.DOWN)
            todayList.fling(Direction.DOWN)
        } else {
            device.swipe(
                device.displayWidth / 2,
                (device.displayHeight * 0.8).toInt(),
                device.displayWidth / 2,
                (device.displayHeight * 0.2).toInt(),
                10
            )
        }
    }
}