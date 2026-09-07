package com.fasaldrishti.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SecondaryAiRequest(
    @SerializedName("primary_result") val primaryResult: String,
    @SerializedName("confidence") val confidence: Float,
    @SerializedName("user_query") val userQuery: String? = null,
    @SerializedName("image_base64") val imageBase64: String? = null,
    @SerializedName("language") val language: String = "en"
)

data class SecondaryAiResponse(
    @SerializedName("explanation") val explanation: String,
    @SerializedName("treatment") val treatment: String?,
    @SerializedName("confidence_note") val confidenceNote: String?,
    @SerializedName("source") val source: String?
)
