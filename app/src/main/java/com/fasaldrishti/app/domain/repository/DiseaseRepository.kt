package com.fasaldrishti.app.domain.repository

import com.fasaldrishti.app.domain.model.DiseaseInfo

interface DiseaseRepository {
    suspend fun getDiseaseInfo(classId: String): Result<DiseaseInfo>
    suspend fun getAllDiseases(cropFilter: String? = null): Result<List<DiseaseInfo>>
    suspend fun askAiAdvisory(
        primaryClass: String,
        confidence: Float,
        query: String,
        language: String,
        base64Image: String? = null,
        base64Images: List<String> = emptyList(),
        conversationHistory: List<com.fasaldrishti.app.domain.model.ChatMessage> = emptyList()
    ): Result<String>
}
