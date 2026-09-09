package com.fasaldrishti.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasaldrishti.app.data.local.LocalChatManager
import com.fasaldrishti.app.domain.model.ChatMessage
import com.fasaldrishti.app.domain.repository.DiseaseRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isAiTyping: Boolean = false,
    val contextInfo: String? = null,
    val selectedLanguage: String = "English"
)

class ChatViewModel(
    private val diseaseRepository: DiseaseRepository,
    private val localChatManager: LocalChatManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ChatUiState(selectedLanguage = localChatManager.getPreferredLanguage())
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun setResponseLanguage(lang: String) {
        localChatManager.savePreferredLanguage(lang)
        _uiState.value = _uiState.value.copy(selectedLanguage = lang)
    }

    fun initContext(context: String?) {
        val savedMessages = localChatManager.loadMessages()

        if (savedMessages.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                contextInfo = context ?: _uiState.value.contextInfo,
                messages = savedMessages
            )
            // If new diagnosis context arrives that wasn't previously greeted, add greeting
            if (context != null && savedMessages.none { it.text.contains(context) }) {
                val contextGreeting = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = "Namaste! I am your AI Agronomist 🌾. I see you just scanned: $context. Feel free to ask about chemical spray dosages, organic remedies, or prevention!",
                    isUser = false
                )
                val updated = savedMessages + contextGreeting
                _uiState.value = _uiState.value.copy(messages = updated)
                localChatManager.saveMessages(updated)
            }
        } else {
            val initialMessage = if (context != null) {
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = "Namaste! I am your AI Agronomist 🌾. I have reviewed your scan diagnosis ($context). How can I assist you with treatment dosages, spray schedules, or soil management?",
                    isUser = false
                )
            } else {
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = "Namaste! I am Fasal Drishti's AI Agronomist 🌾. Ask me anything about crop diseases, pest controls, fertilizers, and organic treatments. You can also select your preferred response language from the top bar!",
                    isUser = false
                )
            }
            val initialList = listOf(initialMessage)
            _uiState.value = _uiState.value.copy(
                contextInfo = context,
                messages = initialList
            )
            localChatManager.saveMessages(initialList)
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = userText,
            isUser = true
        )

        val updatedMessages = _uiState.value.messages + userMessage
        _uiState.value = _uiState.value.copy(messages = updatedMessages, isAiTyping = true)
        localChatManager.saveMessages(updatedMessages)

        viewModelScope.launch {
            delay(500)
            val primaryClass = _uiState.value.contextInfo ?: "General Crop Query"
            val currentLang = _uiState.value.selectedLanguage
            val result = diseaseRepository.askAiAdvisory(
                primaryClass = primaryClass,
                confidence = 0.94f,
                query = userText,
                language = currentLang
            )

            val aiReplyText = result.getOrDefault(
                if (currentLang == "Hinglish") {
                    "Fasal ($primaryClass) ke liye: Sankramit pattiyo ko todkar alag karein, Mancozeb (2.5g/L) ka spray karein, aur kheton me jal-nikasi (drainage) accha rakhein."
                } else {
                    "For $primaryClass, ensure you rotate with non-host crops, prune infected leaves, and maintain balanced potassium and zinc in the soil."
                }
            )

            val aiMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                text = aiReplyText,
                isUser = false
            )

            val finalList = _uiState.value.messages + aiMessage
            _uiState.value = _uiState.value.copy(
                messages = finalList,
                isAiTyping = false
            )
            localChatManager.saveMessages(finalList)
        }
    }

    fun clearChat() {
        localChatManager.clearChat()
        val defaultWelcome = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = "Namaste! I am Fasal Drishti's AI Agronomist 🌾. How can I help you today?",
            isUser = false
        )
        val initialList = listOf(defaultWelcome)
        _uiState.value = _uiState.value.copy(messages = initialList)
        localChatManager.saveMessages(initialList)
    }
}
