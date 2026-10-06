package com.crescentapps.whozit.presentation

import androidx.compose.runtime.Composable
import com.crescentapps.whozit.WhozitApplication
import com.crescentapps.whozit.core.model.UiSystemMode
import com.crescentapps.whozit.data.preferences.UserPreferences
import com.crescentapps.whozit.presentation.gaussianblur.GaussianBlurApp
import com.crescentapps.whozit.presentation.liquid.LiquidApp

/**
 * Top-Level Whozit App Root Router.
 * Routes seamlessly between GaussianBlur UI and Liquid UI based on user preference.
 */
@Composable
fun WhozitApp(
    app: WhozitApplication,
    prefs: UserPreferences,
    deepLinkCode: String? = null
) {
    when (prefs.uiSystemMode) {
        UiSystemMode.GAUSSIAN_BLUR -> {
            GaussianBlurApp(
                app = app,
                prefs = prefs,
                deepLinkCode = deepLinkCode
            )
        }
        UiSystemMode.LIQUID -> {
            LiquidApp(
                app = app,
                prefs = prefs,
                deepLinkCode = deepLinkCode
            )
        }
        UiSystemMode.MATERIAL_3_EXPRESSIVE -> {
            com.crescentapps.whozit.presentation.m3expressive.Material3ExpressiveApp(
                app = app,
                prefs = prefs,
                deepLinkCode = deepLinkCode
            )
        }
    }
}
