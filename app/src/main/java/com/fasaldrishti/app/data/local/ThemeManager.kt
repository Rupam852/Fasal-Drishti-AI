package com.fasaldrishti.app.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

class ThemeManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("fasal_theme_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private fun loadThemeMode(): ThemeMode {
        // One-time migration for v1.1.0 to set default Light Mode for all existing & new users
        val isMigrated = prefs.getBoolean("v1_1_0_light_theme_migrated", false)
        if (!isMigrated) {
            prefs.edit()
                .putString("selected_theme_mode", ThemeMode.LIGHT.name)
                .putBoolean("v1_1_0_light_theme_migrated", true)
                .apply()
            return ThemeMode.LIGHT
        }

        val saved = prefs.getString("selected_theme_mode", ThemeMode.LIGHT.name)
        return try {
            ThemeMode.valueOf(saved ?: ThemeMode.LIGHT.name)
        } catch (_: Exception) {
            ThemeMode.LIGHT
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("selected_theme_mode", mode.name).apply()
        _themeMode.value = mode
    }
}
