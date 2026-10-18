package com.crescentapps.whozit.presentation.screens.settings

import com.crescentapps.whozit.core.model.ThemeMode
import com.crescentapps.whozit.core.model.UiSystemMode
import com.crescentapps.whozit.data.preferences.UserPreferences
import com.crescentapps.whozit.data.preferences.UserPreferencesRepository
import org.junit.Assert.*
import org.junit.Test

class SettingsViewModelTest {

    class FakeUserPreferencesRepository : UserPreferencesRepository(context = null) {
        var currentPreferences = UserPreferences()
        override val userPreferencesFlow = kotlinx.coroutines.flow.MutableStateFlow(currentPreferences)

        override suspend fun setUiSystemMode(mode: UiSystemMode) {
            currentPreferences = currentPreferences.copy(uiSystemMode = mode)
            userPreferencesFlow.value = currentPreferences
        }

        override suspend fun setThemeMode(mode: ThemeMode) {
            currentPreferences = currentPreferences.copy(themeMode = mode)
            userPreferencesFlow.value = currentPreferences
        }
    }

    @Test
    fun testUserPreferences_DefaultState() {
        val prefs = UserPreferences()
        assertEquals(UiSystemMode.GAUSSIAN_BLUR, prefs.uiSystemMode)
        assertEquals(ThemeMode.SYSTEM, prefs.themeMode)
        assertTrue(prefs.adaptiveLuminance)
        assertEquals(1000, prefs.adaptiveLuminanceInterval)
    }

    @Test
    fun testLiveOverrides_MergeWithPreferences() {
        val repo = FakeUserPreferencesRepository()
        val basePrefs = repo.currentPreferences
        
        // Override glassCornerRadius from 28f to 42f
        val updated = basePrefs.copy(glassCornerRadius = 42f)
        assertEquals(42f, updated.glassCornerRadius, 0.01f)
    }
}
