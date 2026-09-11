package com.fasaldrishti.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasaldrishti.app.data.local.LocalChatManager
import com.fasaldrishti.app.domain.model.AiApiKeyException
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
    val currentSessionId: String = "general",
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
        val currentMessages = _uiState.value.messages
        val sessionId = _uiState.value.currentSessionId
        val cleanContext = _uiState.value.contextInfo

        // If this session has not yet had any user interaction (only the 1 initial greeting),
        // update the greeting to match the newly selected language instantly!
        if (currentMessages.size == 1 && !currentMessages[0].isUser) {
            val newGreeting = if (cleanContext != null) {
                ChatMessage(
                    id = currentMessages[0].id,
                    text = buildContextGreeting(cleanContext, lang),
                    isUser = false
                )
            } else {
                ChatMessage(
                    id = currentMessages[0].id,
                    text = buildGeneralGreeting(lang),
                    isUser = false
                )
            }
            val updated = listOf(newGreeting)
            _uiState.value = _uiState.value.copy(
                selectedLanguage = lang,
                messages = updated
            )
            localChatManager.saveMessages(sessionId, updated)
        } else {
            _uiState.value = _uiState.value.copy(selectedLanguage = lang)
        }
    }

    fun initContext(context: String?) {
        val cleanContext = context?.removePrefix("Condition: ")?.trim()
        val sessionId = if (!cleanContext.isNullOrBlank()) {
            "plant_" + cleanContext.lowercase().replace(Regex("[^a-z0-9_]"), "_").take(40)
        } else {
            "general"
        }

        val savedMessages = localChatManager.loadMessages(sessionId)
        val currentLang = _uiState.value.selectedLanguage

        if (savedMessages.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                contextInfo = cleanContext,
                currentSessionId = sessionId,
                messages = savedMessages
            )
        } else {
            val initialMessage = if (cleanContext != null) {
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = buildContextGreeting(cleanContext, currentLang),
                    isUser = false
                )
            } else {
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = buildGeneralGreeting(currentLang),
                    isUser = false
                )
            }
            val initialList = listOf(initialMessage)
            _uiState.value = _uiState.value.copy(
                contextInfo = cleanContext,
                currentSessionId = sessionId,
                messages = initialList
            )
            localChatManager.saveMessages(sessionId, initialList)
        }
    }

    private fun buildContextGreeting(cleanContext: String, language: String): String {
        val lang = language.lowercase()
        return when {
            lang.contains("bengali") || lang.contains("বাংলা") -> {
                "নমস্কার কিষাণ ভাই! 🙏 আমি আপনার **$cleanContext** এর ডায়াগনস্টিক রিপোর্ট লোড করেছি।\n\nআমি আপনাকে বিশেষভাবে সাহায্য করতে পারি:\n• 🧪 প্রতি লিটার জলে ছত্রাকনাশক/কীটনাশকের সঠিক ডোজ\n• 🌿 জৈব ও দেশি প্রতিকার\n• ⏳ ফসল তোলার সময় ও সেরে ওঠার দিন\n• 🌧️ আবহাওয়া অনুযায়ী স্প্রে করার সতর্কতা\n\nএই ফসলের রোগ সম্পর্কে আপনি কী জানতে চান?"
            }
            lang.contains("hindi") || lang.contains("हिन्दी") -> {
                "नमस्ते किसान भाई! 🙏 मैंने आपकी **$cleanContext** की डायग्नोस्टिक रिपोर्ट लोड कर ली है।\n\nमैं आपकी विशेष रूप से सहायता कर सकता हूँ:\n• 🧪 प्रति लीटर पानी में फफूंदनाशक/कीटनाशक की सही खुराक\n• 🌿 जैविक एवं देसी उपचार\n• ⏳ फसल कटाई अंतराल (PHI) और सुधार का समय\n• 🌧️ मौसम अनुसार छिड़काव सावधानियां\n\nआप इस फसल की स्थिति के बारे में क्या जानना चाहते हैं?"
            }
            lang.contains("hinglish") -> {
                "Namaste Kisan Bhai! 🙏 Maine aapki **$cleanContext** ki diagnostic report load kar li hai.\n\nMain aapki in baaton me madad kar sakta hoon:\n• 🧪 Prati liter paani me dawai ka sahi spray dose\n• 🌿 Organic aur desi upaay\n• ⏳ Paudha theek hone ka samay\n• 🌧️ Mausam anusar spray ki savdhaniyan\n\nAap is bimari ke bare me kya puchna chahte hain?"
            }
            lang.contains("marathi") || lang.contains("मराठी") -> {
                "नमस्कार शेतकरी बंधू! 🙏 मी तुमच्या **$cleanContext** चा रोग निदान अहवाल लोड केला आहे.\n\nमी तुम्हाला मदत करू शकतो:\n• 🧪 प्रति लिटर पाण्यासाठी औषधाचे अचूक प्रमाण\n• 🌿 सेंद्रिय व जैविक उपाय\n• ⏳ पीक बरे होण्याचा कालावधी\n• 🌧️ हवामानानुसार फवारणीची काळजी\n\nतुम्हाला या पिकाबद्दल काय विचारायचे आहे?"
            }
            lang.contains("punjabi") || lang.contains("ਪੰਜਾਬੀ") -> {
                "ਸਤਿ ਸ੍ਰੀ ਅਕਾਲ ਕਿਸਾਨ ਵੀਰੋ! 🙏 ਮੈਂ ਤੁਹਾਡੀ **$cleanContext** ਦੀ ਰਿਪੋਰਟ ਲੋਡ ਕਰ ਲਈ ਹੈ।\n\nਮੈਂ ਤੁਹਾਡੀ ਮਦਦ ਕਰ ਸਕਦਾ ਹਾਂ:\n• 🧪 ਪ੍ਰਤੀ ਲੀਟਰ ਪਾਣੀ ਵਿੱਚ ਦਵਾਈ ਦੀ ਸਹੀ ਖੁਰਾਕ\n• 🌿 ਜੈਵਿਕ ਅਤੇ ਦੇਸੀ ਇਲਾਜ\n• ⏳ ਠੀਕ ਹੋਣ ਦਾ ਸਮਾਂ\n• 🌧️ ਮੌਸਮ ਅਨੁਸਾਰ ਛਿੜਕਾਅ ਸਾਵਧਾਨੀਆਂ\n\nਤੁਸੀਂ ਇਸ ਬਾਰੇ ਕੀ ਪੁੱਛਣਾ ਚਾਹੁੰਦੇ ਹੋ?"
            }
            lang.contains("gujarati") || lang.contains("ગુજરાતી") -> {
                "નમસ્તે ખેડૂત મિત્ર! 🙏 મેં તમારા **$cleanContext** નો રિપોર્ટ લોડ કર્યો છે.\n\nહું તમને મદદ કરી શકું છું:\n• 🧪 પ્રતિ લીટર પાણીમાં દવાનો સાચો ડોઝ\n• 🌿 ઓર્ગેનિક અને દેશી ઉપચાર\n• ⏳ પાક સુધારણાનો સમયગાળો\n• 🌧️ હવામાન મુજબ છંટકાવની સાવચેતી\n\nતમે આ પાક વિશે શું જાણવા માંગો છો?"
            }
            lang.contains("telugu") || lang.contains("తెలుగు") -> {
                "నమస్కారం రైతు సోదరులారా! 🙏 నేను మీ **$cleanContext** నివేదికను లోడ్ చేసాను.\n\nనేను మీకు సహాయం చేయగలను:\n• 🧪 లీటరు నీటికి మందు సరైన మోతాదు\n• 🌿 సేంద్రీయ మరియు సహజ నివారణలు\n• ⏳ పంట కోలుకునే సమయం\n• 🌧️ వాతావరణ ఆధారిత జాగ్రత్తలు\n\nఈ పంట గురించి మీరు ఏమి తెలుసుకోవాలనుకుంటున్నారు?"
            }
            lang.contains("tamil") || lang.contains("தமிழ்") -> {
                "வணக்கம் விவசாய தோழரே! 🙏 உங்கள் **$cleanContext** அறிக்கை ஏற்றப்பட்டது.\n\nநான் உங்களுக்கு உதவ முடியும்:\n• 🧪 சரியான மருந்து அளவு\n• 🌿 இயற்கை வைத்தியம்\n• ⏳ பயிர் மீட்கும் காலம்\n• 🌧️ தெளிக்கும் முன்னெச்சரிக்கைகள்\n\nநீங்கள் என்ன கேட்க விரும்புகிறீர்கள்?"
            }
            else -> {
                "Namaste Kisan Bhai! 🙏 I have loaded your diagnostic report for **$cleanContext**.\n\nI can assist you specifically with:\n• 🧪 Exact fungicide / pesticide spray dosages per liter water\n• 🌿 Non-toxic organic & bio-remedies\n• ⏳ Pre-harvest interval (PHI) & recovery timeline\n• 🌧️ Weather-based spray precautions\n\nWhat would you like to know about this crop condition?"
            }
        }
    }

    private fun buildGeneralGreeting(language: String): String {
        val lang = language.lowercase()
        return when {
            lang.contains("bengali") || lang.contains("বাংলা") -> {
                "নমস্কার কিষাণ ভাই! 🌾 আমি আপনার ২৪x৭ ডিজিটাল কৃষি পরামর্শদাতা (ফসল উপদেষ্টা)।\n\nআমাকে জিজ্ঞাসা করুন:\n• 🌿 ফসলের রোগ নির্ণয় ও প্রতিকার\n• 🧪 এনপিকে সার গণনা ও মাটির স্বাস্থ্য\n• 🌾 মান্ডি দর ও ফসলের সঠিক সময়\n• 🏛️ পিএম-কিষাণ ও সরকারি কৃষি যোজনা\n\nআপনি মাইক 🎙️ ট্যাপ করে বাংলায় কথা বলেও প্রশ্ন করতে পারেন!"
            }
            lang.contains("hindi") || lang.contains("हिन्दी") -> {
                "नमस्ते किसान भाई! 🌾 मैं आपका 24x7 डिजिटल कृषि सलाहकार (फसल सलाहकार) हूँ।\n\nमुझसे पूछें:\n• 🌿 फसल रोग निदान और रोकथाम\n• 🧪 NPK खाद गणना और मिट्टी का स्वास्थ्य\n• 🌾 मंडी भाव और फसल कटाई का सही समय\n• 🏛️ पीएम-किसान और सरकारी कृषि योजनाएं\n\nआप माइक 🎙️ दबाकर अपनी भाषा में बोलकर भी सवाल पूछ सकते हैं!"
            }
            lang.contains("hinglish") -> {
                "Namaste Kisan Bhai! 🌾 Main aapka 24x7 Digital Agronomist (Fasal Salahkar) hoon.\n\nMujhse puchein:\n• 🌿 Fasal bimari ka ilaj aur roktham\n• 🧪 NPK khad calculator aur mitti ki jaanch\n• 🌾 Mandi bhav aur kheti ke tips\n• 🏛️ PM-Kisan aur sarkari yojanaen\n\nAap mic 🎙️ dabakar bolkar bhi sawal puch sakte hain!"
            }
            else -> {
                "Namaste Kisan Bhai! 🌾 I am your 24x7 Digital Agronomist (Fasal Salahkar).\n\nAsk me anything about:\n• 🌿 Crop disease diagnosis & prevention\n• 🧪 NPK fertilizer calculation & soil health\n• 🌾 Mandi rates & best harvest timings\n• 🏛️ PM-Kisan & government agriculture schemes\n\nYou can also tap the mic 🎙️ to ask queries in Hindi or your regional language!"
            }
        }
    }

    fun retryFailedMessage(failedMessageId: String, query: String) {
        val sessionId = _uiState.value.currentSessionId
        val filtered = _uiState.value.messages.filterNot { it.id == failedMessageId }
        _uiState.value = _uiState.value.copy(messages = filtered, isAiTyping = true)
        localChatManager.saveMessages(sessionId, filtered)

        executeAiAdvisory(query)
    }

    fun sendMessage(
        userText: String,
        imageUri: String? = null,
        base64Image: String? = null,
        imageUris: List<String> = emptyList(),
        base64Images: List<String> = emptyList()
    ) {
        val sessionId = _uiState.value.currentSessionId
        val allUris = if (imageUris.isNotEmpty()) imageUris else if (!imageUri.isNullOrBlank()) listOf(imageUri) else emptyList()
        val allBase64 = if (base64Images.isNotEmpty()) base64Images else if (!base64Image.isNullOrBlank()) listOf(base64Image) else emptyList()

        val query = if (userText.isBlank() && allUris.isNotEmpty()) {
            if (allUris.size > 1) {
                "Please examine these ${allUris.size} attached crop photos (cross-referencing upper/lower foliage, stem & symptoms) and provide an accurate diagnosis with chemical dosages and organic remedies."
            } else {
                "Please analyze this attached crop photo and tell me the disease, symptoms, and spray treatment."
            }
        } else userText

        if (query.isBlank() && allUris.isEmpty()) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = query,
            isUser = true,
            imageUri = allUris.firstOrNull(),
            imageUris = allUris
        )

        val updatedMessages = _uiState.value.messages + userMessage
        _uiState.value = _uiState.value.copy(messages = updatedMessages, isAiTyping = true)
        localChatManager.saveMessages(sessionId, updatedMessages)

        executeAiAdvisory(query, allBase64)
    }

    private fun executeAiAdvisory(query: String, base64Images: List<String> = emptyList()) {
        viewModelScope.launch {
            delay(500)
            val sessionId = _uiState.value.currentSessionId
            val primaryClass = _uiState.value.contextInfo ?: "General Crop Query"
            val currentLang = _uiState.value.selectedLanguage
            val result = diseaseRepository.askAiAdvisory(
                primaryClass = primaryClass,
                confidence = 0.94f,
                query = query,
                language = currentLang,
                base64Image = base64Images.firstOrNull(),
                base64Images = base64Images
            )

            result.onSuccess { aiReplyText ->
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
                localChatManager.saveMessages(sessionId, finalList)
            }.onFailure { err ->
                val isApiKeyErr = err is AiApiKeyException
                val errorText = if (isApiKeyErr) {
                    "Aapki Custom AI Key expire ya galat ho sakti hai. Kripya Settings ➔ AI Engine me jakar nayi key dalein ya Default Engine chunein."
                } else {
                    "Unable to connect to AI engines due to network or quota limit. Please try again shortly."
                }

                val aiErrorMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = errorText,
                    isUser = false,
                    isError = true,
                    isApiKeyError = isApiKeyErr,
                    failedQuery = query
                )
                val finalList = _uiState.value.messages + aiErrorMessage
                _uiState.value = _uiState.value.copy(
                    messages = finalList,
                    isAiTyping = false
                )
                localChatManager.saveMessages(sessionId, finalList)
            }
        }
    }

    fun clearChat() {
        val sessionId = _uiState.value.currentSessionId
        val cleanContext = _uiState.value.contextInfo
        val currentLang = _uiState.value.selectedLanguage
        localChatManager.clearChat(sessionId)

        val freshGreeting = if (cleanContext != null) {
            ChatMessage(
                id = UUID.randomUUID().toString(),
                text = buildContextGreeting(cleanContext, currentLang),
                isUser = false
            )
        } else {
            ChatMessage(
                id = UUID.randomUUID().toString(),
                text = buildGeneralGreeting(currentLang),
                isUser = false
            )
        }
        val initialList = listOf(freshGreeting)
        _uiState.value = _uiState.value.copy(messages = initialList)
        localChatManager.saveMessages(sessionId, initialList)
    }
}
