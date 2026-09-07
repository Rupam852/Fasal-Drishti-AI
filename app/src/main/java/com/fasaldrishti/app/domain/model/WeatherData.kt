package com.fasaldrishti.app.domain.model

enum class SprayStatus {
    SAFE,       // 🟢 Safe conditions for spraying
    CAUTION,    // 🟡 Moderate conditions (slight wind or high temp)
    UNSAFE      // 🔴 Bad conditions (rain forecast or high winds)
}

data class WeatherData(
    val locationName: String = "Detecting Location...",
    val temperature: Double = 27.0,
    val humidity: Int = 62,
    val windSpeed: Double = 9.5,
    val rainProbability: Int = 12,
    val weatherCode: Int = 1,
    val weatherDescription: String = "Clear Sky",
    val sprayStatus: SprayStatus = SprayStatus.SAFE,
    val sprayAdvisory: String = "छिड़काव के लिए उत्तम मौसम (Safe for pesticide & fertilizer spray)",
    val isLoading: Boolean = false,
    val isLoaded: Boolean = true
)
