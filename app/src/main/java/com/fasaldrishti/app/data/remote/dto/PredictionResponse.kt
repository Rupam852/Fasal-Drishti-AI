package com.fasaldrishti.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PredictionResponse(
    @SerializedName("predicted_class") val predictedClass: String,
    @SerializedName("crop_name") val cropName: String,
    @SerializedName("crop_hindi") val cropHindi: String?,
    @SerializedName("disease_name") val diseaseName: String,
    @SerializedName("disease_hindi") val diseaseHindi: String?,
    @SerializedName("severity") val severity: String,
    @SerializedName("confidence") val confidence: Float,
    @SerializedName("top_3") val top3: List<TopPredictionItem>,
    @SerializedName("disease_info") val diseaseInfo: DiseaseInfoDto?,
    @SerializedName("model_version") val modelVersion: String,
    @SerializedName("requires_secondary_ai") val requiresSecondaryAi: Boolean
)

data class TopPredictionItem(
    @SerializedName("class") val className: String,
    @SerializedName("crop_name") val cropName: String,
    @SerializedName("disease_name") val diseaseName: String,
    @SerializedName("confidence") val confidence: Float
)

data class DiseaseInfoDto(
    @SerializedName("class_id") val classId: String,
    @SerializedName("crop_name") val cropName: String,
    @SerializedName("crop_hindi") val cropHindi: String?,
    @SerializedName("disease_name") val diseaseName: String,
    @SerializedName("disease_hindi") val diseaseHindi: String?,
    @SerializedName("severity") val severity: String,
    @SerializedName("is_healthy") val isHealthy: Boolean,
    @SerializedName("symptoms") val symptoms: String,
    @SerializedName("symptoms_hindi") val symptomsHindi: String?,
    @SerializedName("treatment") val treatment: String,
    @SerializedName("treatment_hindi") val treatmentHindi: String?,
    @SerializedName("prevention") val prevention: String
)
