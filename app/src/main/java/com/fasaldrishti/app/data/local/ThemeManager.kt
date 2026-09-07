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
        val saved = prefs.getString("selected_theme_mode", ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(saved ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("selected_theme_mode", mode.name).apply()
        _themeMode.value = mode
    }
}
