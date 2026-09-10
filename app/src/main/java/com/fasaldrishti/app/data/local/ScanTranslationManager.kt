package com.fasaldrishti.app.data.local

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

data class TranslatedDossier(
    val languageCode: String,
    val diseaseName: String,
    val symptoms: String,
    val treatment: String,
    val prevention: String
)

class ScanTranslationManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("fasal_drishti_scan_translations", Context.MODE_PRIVATE)

    fun getSavedLanguage(scanId: String): String {
        return prefs.getString("lang_$scanId", "en") ?: "en"
    }

    fun setSavedLanguage(scanId: String, languageCode: String) {
        prefs.edit().putString("lang_$scanId", languageCode).apply()
    }

    fun getTranslatedDossier(scanId: String, languageCode: String): TranslatedDossier? {
        if (languageCode.equals("en", ignoreCase = true)) return null
        val jsonStr = prefs.getString("dossier_${scanId}_$languageCode", null) ?: return null
        return try {
            val obj = JSONObject(jsonStr)
            TranslatedDossier(
                languageCode = obj.optString("language_code", languageCode),
                diseaseName = obj.optString("disease_name", ""),
                symptoms = obj.optString("symptoms", ""),
                treatment = obj.optString("treatment", ""),
                prevention = obj.optString("prevention", "")
            )
        } catch (_: Exception) {
            null
        }
    }

    fun saveTranslatedDossier(scanId: String, dossier: TranslatedDossier) {
        try {
            val obj = JSONObject().apply {
                put("language_code", dossier.languageCode)
                put("disease_name", dossier.diseaseName)
                put("symptoms", dossier.symptoms)
                put("treatment", dossier.treatment)
                put("prevention", dossier.prevention)
            }
            prefs.edit()
                .putString("dossier_${scanId}_${dossier.languageCode}", obj.toString())
                .putString("lang_$scanId", dossier.languageCode)
                .apply()
        } catch (_: Exception) {}
    }
}
