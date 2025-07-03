package com.dvhamham.manager.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

class ThemeManager(private val context: Context) {
    private val sharedPrefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    
    private val _isDarkMode = MutableStateFlow(loadDarkModePreference())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _themeMode = MutableStateFlow(loadThemeModePreference())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private fun loadDarkModePreference(): Boolean {
        return sharedPrefs.getBoolean("is_dark_mode", false)
    }

    private fun loadThemeModePreference(): ThemeMode {
        val modeString = sharedPrefs.getString("theme_mode", ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(modeString ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    private fun saveDarkModePreference(isDark: Boolean) {
        sharedPrefs.edit().putBoolean("is_dark_mode", isDark).apply()
    }

    private fun saveThemeModePreference(mode: ThemeMode) {
        sharedPrefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        saveThemeModePreference(mode)
        updateDarkMode()
    }

    fun toggleDarkMode() {
        val currentMode = _themeMode.value
        val newMode = when (currentMode) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.SYSTEM -> ThemeMode.DARK
        }
        setThemeMode(newMode)
    }

    private fun updateDarkMode() {
        val shouldBeDark = when (_themeMode.value) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> false // We'll handle this in the Composable
        }
        _isDarkMode.value = shouldBeDark
        saveDarkModePreference(shouldBeDark)
    }

    fun updateSystemTheme(isSystemDark: Boolean) {
        if (_themeMode.value == ThemeMode.SYSTEM) {
            _isDarkMode.value = isSystemDark
            saveDarkModePreference(isSystemDark)
        }
    }
}

val LocalThemeManager = staticCompositionLocalOf<ThemeManager> { 
    error("No ThemeManager provided") 
}

@Composable
fun rememberThemeManager(): ThemeManager {
    val context = LocalContext.current
    return remember { ThemeManager(context) }
} 