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

    private fun getSessionKey(sessionId: String): String {
        val sanitized = sessionId.lowercase().trim().replace(Regex("[^a-z0-9_]"), "_").take(60)
        return "chat_session_$sanitized"
    }

    fun loadMessages(sessionId: String = "general"): List<ChatMessage> {
        val key = getSessionKey(sessionId)
        val jsonStr = prefs.getString(key, null) 
            ?: if (sessionId == "general") prefs.getString("chat_messages", null) else null
            ?: return emptyList()

        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<ChatMessage>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val imageUrisList = mutableListOf<String>()
                if (obj.has("image_uris")) {
                    val arr = obj.getJSONArray("image_uris")
                    for (j in 0 until arr.length()) {
                        imageUrisList.add(arr.getString(j))
                    }
                } else if (obj.has("image_uri")) {
                    val single = obj.optString("image_uri")
                    if (!single.isNullOrBlank()) imageUrisList.add(single)
                }

                list.add(
                    ChatMessage(
                        id = obj.getString("id"),
                        text = obj.getString("text"),
                        isUser = obj.getBoolean("is_user"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        imageUri = if (obj.has("image_uri")) obj.optString("image_uri") else null,
                        imageUris = imageUrisList,
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

    fun saveMessages(sessionId: String = "general", messages: List<ChatMessage>) {
        try {
            val key = getSessionKey(sessionId)
            val jsonArray = JSONArray()
            // Keep up to 100 recent messages per session
            val recentMessages = if (messages.size > 100) messages.takeLast(100) else messages
            for (msg in recentMessages) {
                val obj = JSONObject().apply {
                    put("id", msg.id)
                    put("text", msg.text)
                    put("is_user", msg.isUser)
                    put("timestamp", msg.timestamp)
                    if (msg.imageUris.isNotEmpty()) {
                        val arr = JSONArray()
                        msg.imageUris.forEach { arr.put(it) }
                        put("image_uris", arr)
                    } else if (!msg.imageUri.isNullOrBlank()) {
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
            prefs.edit().putString(key, jsonArray.toString()).apply()
        } catch (_: Exception) {
        }
    }

    fun clearChat(sessionId: String? = null) {
        val editor = prefs.edit()
        if (sessionId != null) {
            editor.remove(getSessionKey(sessionId))
            if (sessionId == "general") editor.remove("chat_messages")
        } else {
            val keysToRemove = prefs.all.keys.filter { it.startsWith("chat_session_") || it == "chat_messages" }
            keysToRemove.forEach { editor.remove(it) }
        }
        editor.apply()
    }

    fun getChatStorageSizeFormatted(): String {
        var totalBytes = 0
        val sessionKeys = prefs.all.keys.filter { it.startsWith("chat_session_") || it == "chat_messages" }
        for (key in sessionKeys) {
            val jsonStr = prefs.getString(key, null)
            if (jsonStr != null) {
                totalBytes += jsonStr.toByteArray(Charsets.UTF_8).size
            }
        }
        return if (totalBytes < 1024) {
            "$totalBytes B"
        } else if (totalBytes < 1024 * 1024) {
            String.format(java.util.Locale.US, "%.1f KB", totalBytes / 1024.0)
        } else {
            String.format(java.util.Locale.US, "%.2f MB", totalBytes / (1024.0 * 1024.0))
        }
    }

    fun getChatMessageCount(): Int {
        var totalCount = 0
        val sessionKeys = prefs.all.keys.filter { it.startsWith("chat_session_") || it == "chat_messages" }
        for (key in sessionKeys) {
            val jsonStr = prefs.getString(key, null)
            if (jsonStr != null) {
                try {
                    totalCount += JSONArray(jsonStr).length()
                } catch (_: Exception) {
                }
            }
        }
        return totalCount
    }

    fun getPreferredLanguage(): String {
        return prefs.getString("chat_response_language", "English") ?: "English"
    }

    fun savePreferredLanguage(language: String) {
        prefs.edit().putString("chat_response_language", language).apply()
    }
}

