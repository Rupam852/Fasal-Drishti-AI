package com.fasaldrishti.app.domain.model

data class DiseaseInfo(
    val classId: String,
    val cropName: String,
    val cropHindi: String? = null,
    val diseaseName: String,
    val diseaseHindi: String? = null,
    val severity: String,
    val isHealthy: Boolean,
    val symptoms: String,
    val symptomsHindi: String? = null,
    val treatment: String,
    val treatmentHindi: String? = null,
    val prevention: String
)
