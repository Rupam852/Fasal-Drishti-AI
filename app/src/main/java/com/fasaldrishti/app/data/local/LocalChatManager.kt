package com.fasaldrishti.app.data.local

import android.content.Context
import com.fasaldrishti.app.domain.model.ChatMessage
import org.json.JSONArray
import org.json.JSONObject

/**
 * 100% On-Device Local Chat Storage Manager.
 * Stores farmer AI advisory conversation history locally on device.
 * Never uploaded or synced to cloud / Supabase (Strict Privacy).
 */
class LocalChatManager(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("fasal_local_chat_db", Context.MODE_PRIVATE)

    fun loadMessages(): List<ChatMessage> {
        val jsonStr = prefs.getString("chat_messages", null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<ChatMessage>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    ChatMessage(
                        id = obj.getString("id"),
                        text = obj.getString("text"),
                        isUser = obj.getBoolean("is_user"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        imageUri = if (obj.has("image_uri")) obj.optString("image_uri") else null,
                        isError = obj.optBoolean("is_error", false),
                        isApiKeyError = obj.optBoolean("is_api_key_error", false),
                        failedQuery = if (obj.has("failed_query")) obj.optString("failed_query") else null
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveMessages(messages: List<ChatMessage>) {
        try {
            val jsonArray = JSONArray()
            // Keep up to 100 recent messages for lightweight storage
            val recentMessages = if (messages.size > 100) messages.takeLast(100) else messages
            for (msg in recentMessages) {
                val obj = JSONObject().apply {
                    put("id", msg.id)
                    put("text", msg.text)
                    put("is_user", msg.isUser)
                    put("timestamp", msg.timestamp)
                    if (!msg.imageUri.isNullOrBlank()) {
                        put("image_uri", msg.imageUri)
                    }
                    put("is_error", msg.isError)
                    put("is_api_key_error", msg.isApiKeyError)
                    if (msg.failedQuery != null) {
                        put("failed_query", msg.failedQuery)
                    }
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString("chat_messages", jsonArray.toString()).apply()
        } catch (_: Exception) {
        }
    }

    fun clearChat() {
        prefs.edit().remove("chat_messages").apply()
    }

    fun getChatStorageSizeFormatted(): String {
        val jsonStr = prefs.getString("chat_messages", null) ?: return "0 KB"
        val bytes = jsonStr.toByteArray(Charsets.UTF_8).size
        return if (bytes < 1024) {
            "$bytes B"
        } else if (bytes < 1024 * 1024) {
            String.format(java.util.Locale.US, "%.1f KB", bytes / 1024.0)
        } else {
            String.format(java.util.Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
        }
    }

    fun getChatMessageCount(): Int {
        val jsonStr = prefs.getString("chat_messages", null) ?: return 0
        return try {
            JSONArray(jsonStr).length()
        } catch (_: Exception) {
            0
        }
    }

    fun getPreferredLanguage(): String {
        return prefs.getString("chat_response_language", "English") ?: "English"
    }

    fun savePreferredLanguage(language: String) {
        prefs.edit().putString("chat_response_language", language).apply()
    }
}

