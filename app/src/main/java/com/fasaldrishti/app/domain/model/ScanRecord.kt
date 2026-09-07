package com.fasaldrishti.app.domain.model

data class ScanRecord(
    val id: String,
    val imageUrl: String,
    val predictedClass: String,
    val confidence: Float,
    val cropName: String,
    val diseaseName: String,
    val severity: String,
    val symptoms: String,
    val treatment: String,
    val timestamp: Long = System.currentTimeMillis()
)
