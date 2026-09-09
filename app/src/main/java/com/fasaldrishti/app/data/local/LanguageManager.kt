package com.fasaldrishti.app.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val flag: String = "🇮🇳"
) {
    ENGLISH("en", "English", "English", "🌐"),
    HINDI("hi", "हिन्दी", "Hindi"),
    BENGALI("bn", "বাংলা", "Bengali"),
    PUNJABI("pa", "ਪੰਜਾਬੀ", "Punjabi"),
    MARATHI("mr", "मराठी", "Marathi"),
    TELUGU("te", "తెలుగు", "Telugu"),
    TAMIL("ta", "தமிழ்", "Tamil"),
    GUJARATI("gu", "ગુજરાતી", "Gujarati"),
    KANNADA("kn", "ಕನ್ನಡ", "Kannada"),
    ODIA("or", "ଓଡ଼ିଆ", "Odia");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}

class LanguageManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("fasal_drishti_language_prefs", Context.MODE_PRIVATE)

    private val _currentLanguage = MutableStateFlow(loadSavedLanguage())
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private fun loadSavedLanguage(): AppLanguage {
        val savedCode = prefs.getString(KEY_LANGUAGE_CODE, AppLanguage.ENGLISH.code) ?: AppLanguage.ENGLISH.code
        return AppLanguage.fromCode(savedCode)
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE_CODE, language.code).apply()
        _currentLanguage.value = language
    }

    companion object {
        private const val KEY_LANGUAGE_CODE = "selected_app_language_code"
    }
}
