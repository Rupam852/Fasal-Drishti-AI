package com.fasaldrishti.app.domain.model

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val totalScans: Int = 0,
    val healthyCount: Int = 0,
    val diseasedCount: Int = 0
)

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    val isError: Boolean = false,
    val isApiKeyError: Boolean = false,
    val failedQuery: String? = null
)
