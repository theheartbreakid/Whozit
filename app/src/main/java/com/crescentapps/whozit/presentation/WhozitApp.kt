package com.crescentapps.whozit.presentation

import androidx.compose.runtime.Composable
import com.crescentapps.whozit.WhozitApplication
import com.crescentapps.whozit.core.model.UiSystemMode
import com.crescentapps.whozit.data.preferences.UserPreferences
import com.crescentapps.whozit.presentation.gaussianblur.GaussianBlurApp
import com.crescentapps.whozit.presentation.liquid.LiquidApp

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/**
 * Top-Level Whozit App Root Router.
 * Routes seamlessly between GaussianBlur UI, Liquid UI, and Material 3 Expressive based on user preference.
 */
@Composable
fun WhozitApp(
    app: WhozitApplication,
    prefs: UserPreferences,
    deepLinkCode: String? = null
) {
    when (prefs.uiSystemMode) {
        UiSystemMode.GAUSSIAN_BLUR -> {
            Box(Modifier.testTag("renderer_gaussian_blur")) {
                GaussianBlurApp(
                    app = app,
                    prefs = prefs,
                    deepLinkCode = deepLinkCode
                )
            }
        }
        UiSystemMode.LIQUID -> {
            Box(Modifier.testTag("renderer_liquid")) {
                LiquidApp(
                    app = app,
                    prefs = prefs,
                    deepLinkCode = deepLinkCode
                )
            }
        }
        UiSystemMode.MATERIAL_3_EXPRESSIVE -> {
            Box(Modifier.testTag("renderer_material_3_expressive")) {
                com.crescentapps.whozit.presentation.m3expressive.Material3ExpressiveApp(
                    app = app,
                    prefs = prefs,
                    deepLinkCode = deepLinkCode
                )
            }
        }
    }
}
