package com.fasaldrishti.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasaldrishti.app.domain.model.ScanRecord

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey
    val id: String,
    val imageUrl: String,
    val predictedClass: String,
    val confidence: Float,
    val cropName: String,
    val diseaseName: String,
    val severity: String,
    val symptoms: String,
    val treatment: String,
    val timestamp: Long
) {
    fun toDomain(): ScanRecord = ScanRecord(
        id = id,
        imageUrl = imageUrl,
        predictedClass = predictedClass,
        confidence = confidence,
        cropName = cropName,
        diseaseName = diseaseName,
        severity = severity,
        symptoms = symptoms,
        treatment = treatment,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(record: ScanRecord): ScanEntity = ScanEntity(
            id = record.id,
            imageUrl = record.imageUrl,
            predictedClass = record.predictedClass,
            confidence = record.confidence,
            cropName = record.cropName,
            diseaseName = record.diseaseName,
            severity = record.severity,
            symptoms = record.symptoms,
            treatment = record.treatment,
            timestamp = record.timestamp
        )
    }
}
