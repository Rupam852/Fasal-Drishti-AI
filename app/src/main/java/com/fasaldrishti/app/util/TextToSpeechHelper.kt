package com.fasaldrishti.app.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TextToSpeechHelper(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            Log.d("TextToSpeechHelper", "TTS successfully initialized")
        } else {
            Log.e("TextToSpeechHelper", "TTS initialization failed with code $status")
        }
    }

    fun speak(text: String, languageCode: String = "hi") {
        if (!isInitialized || tts == null) return

        val locale = when (languageCode) {
            "hi" -> Locale("hi", "IN")
            "bn" -> Locale("bn", "IN")
            "mr" -> Locale("mr", "IN")
            "pa" -> Locale("pa", "IN")
            "te" -> Locale("te", "IN")
            "ta" -> Locale("ta", "IN")
            "gu" -> Locale("gu", "IN")
            else -> Locale.ENGLISH
        }

        try {
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to default
                tts?.setLanguage(Locale.getDefault())
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "farm_reminder_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Log.e("TextToSpeechHelper", "TTS speak failed", e)
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
