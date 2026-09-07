package com.fasaldrishti.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val selectedLanguage: String = "Hinglish"
)

class ChatViewModel(
    private val diseaseRepository: DiseaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun setResponseLanguage(lang: String) {
        _uiState.value = _uiState.value.copy(selectedLanguage = lang)
    }

    fun initContext(context: String?) {
        if (context != null && _uiState.value.contextInfo == null) {
            _uiState.value = _uiState.value.copy(
                contextInfo = context,
                messages = listOf(
                    ChatMessage(
                        id = UUID.randomUUID().toString(),
                        text = "Namaste! Main aapka AI Agronomist hoon 🌾. Maine aapke scan ($context) ka analysis dekha hai. Iske upchar, dava ke dosage, spray timing ya mitti ke baare me kuch bhi poochein!",
                        isUser = false
                    )
                )
            )
        } else if (_uiState.value.messages.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                messages = listOf(
                    ChatMessage(
                        id = UUID.randomUUID().toString(),
                        text = "Namaste Kisan bhai! Main hoon Fasal Drishti ka AI Krishi Doctor 🌾. Aap kisi bhi bhasha me sawal pooch sakte hain. Upar language button se apni response bhasha (Hinglish/Hindi/Others) chun sakte hain!",
                        isUser = false
                    )
                )
            )
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

        viewModelScope.launch {
            delay(800)
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

            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + aiMessage,
                isAiTyping = false
            )
        }
    }
}
