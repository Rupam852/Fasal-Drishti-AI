package com.fasaldrishti.app.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceAssistantManager(private val context: Context) : RecognitionListener, TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentlySpeakingId = MutableStateFlow<String?>(null)
    val currentlySpeakingId: StateFlow<String?> = _currentlySpeakingId.asStateFlow()

    init {
        try {
            textToSpeech = TextToSpeech(context.applicationContext, this)
        } catch (_: Exception) {}
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            textToSpeech?.language = Locale("hi", "IN")
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    _currentlySpeakingId.value = utteranceId
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _currentlySpeakingId.value = null
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _currentlySpeakingId.value = null
                }
            })
        }
    }

    fun startListening(languageCode: String = "hi-IN", onResult: (String) -> Unit) {
        stopSpeaking()
        stopListening()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            return
        }

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim() ?: ""
                        if (text.isNotBlank()) {
                            _recognizedText.value = text
                            onResult(text)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim() ?: ""
                        if (text.isNotBlank()) {
                            _recognizedText.value = text
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            speechRecognizer?.startListening(intent)
        } catch (_: Exception) {
            _isListening.value = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {}
        _isListening.value = false
    }

    fun speak(text: String, utteranceId: String, languageCode: String = "hi") {
        if (!isTtsInitialized || textToSpeech == null) return

        if (_isSpeaking.value && _currentlySpeakingId.value == utteranceId) {
            stopSpeaking()
            return
        }

        stopSpeaking()

        val locale = when (languageCode.lowercase()) {
            "hi", "hindi" -> Locale("hi", "IN")
            "bn", "bengali", "bangla" -> Locale("bn", "IN")
            "ta", "tamil" -> Locale("ta", "IN")
            "te", "telugu" -> Locale("te", "IN")
            "mr", "marathi" -> Locale("mr", "IN")
            "pa", "punjabi" -> Locale("pa", "IN")
            "gu", "gujarati" -> Locale("gu", "IN")
            "kn", "kannada" -> Locale("kn", "IN")
            "or", "odia" -> Locale("or", "IN")
            else -> Locale.ENGLISH
        }

        try {
            textToSpeech?.language = locale
        } catch (_: Exception) {
            textToSpeech?.language = Locale.ENGLISH
        }

        // Clean markdown characters from text for natural speech
        val cleanText = text
            .replace("**", "")
            .replace("*", "")
            .replace("#", "")
            .replace("`", "")
            .replace("🌾", "")
            .replace("🌿", "")
            .replace("💧", "")
            .replace("⚠️", "")

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
        } catch (_: Exception) {}
        _isSpeaking.value = false
        _currentlySpeakingId.value = null
    }

    fun destroy() {
        stopListening()
        stopSpeaking()
        try {
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (_: Exception) {}
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onError(error: Int) {}
    override fun onResults(results: Bundle?) {}
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
}
