package com.fasaldrishti.app.data.remote

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.fasaldrishti.app.domain.model.SprayStatus
import com.fasaldrishti.app.domain.model.WeatherData
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

class WeatherManager(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val prefs = context.getSharedPreferences("fasal_weather_cache", Context.MODE_PRIVATE)

    private val _weatherData = MutableStateFlow(loadCachedWeather())
    val weatherData: StateFlow<WeatherData> = _weatherData.asStateFlow()

    private fun loadCachedWeather(): WeatherData {
        val location = prefs.getString("cached_location", "Farm Location (भारत)") ?: "Farm Location (भारत)"
        val temp = prefs.getFloat("cached_temp", 28.0f).toDouble()
        val humidity = prefs.getInt("cached_humidity", 60)
        val wind = prefs.getFloat("cached_wind", 10.5f).toDouble()
        val rain = prefs.getInt("cached_rain", 15)
        val code = prefs.getInt("cached_code", 1)
        val desc = prefs.getString("cached_desc", "Mainly Clear (साफ मौसम)") ?: "Mainly Clear (साफ मौसम)"
        val sprayStatusStr = prefs.getString("cached_spray_status", SprayStatus.SAFE.name) ?: SprayStatus.SAFE.name
        val advisory = prefs.getString("cached_advisory", "छिड़काव के लिए उत्तम मौसम (Safe for spraying)")
            ?: "छिड़काव के लिए उत्तम मौसम (Safe for spraying)"

        val sprayStatus = try {
            SprayStatus.valueOf(sprayStatusStr)
        } catch (_: Exception) {
            SprayStatus.SAFE
        }

        return WeatherData(
            locationName = location,
            temperature = temp,
            humidity = humidity,
            windSpeed = wind,
            rainProbability = rain,
            weatherCode = code,
            weatherDescription = desc,
            sprayStatus = sprayStatus,
            sprayAdvisory = advisory,
            isLoaded = true,
            isLoading = false
        )
    }

    private fun saveCachedWeather(data: WeatherData) {
        prefs.edit()
            .putString("cached_location", data.locationName)
            .putFloat("cached_temp", data.temperature.toFloat())
            .putInt("cached_humidity", data.humidity)
            .putFloat("cached_wind", data.windSpeed.toFloat())
            .putInt("cached_rain", data.rainProbability)
            .putInt("cached_code", data.weatherCode)
            .putString("cached_desc", data.weatherDescription)
            .putString("cached_spray_status", data.sprayStatus.name)
            .putString("cached_advisory", data.sprayAdvisory)
            .apply()
    }

    @SuppressLint("MissingPermission")
    suspend fun refreshWeather() = withContext(Dispatchers.IO) {
        try {
            _weatherData.value = _weatherData.value.copy(isLoading = true)

            var latitude = 25.5941 // Default: Central Gangetic Agro Zone (Patna / Delhi)
            var longitude = 85.1376
            var resolvedCity = "Farm Location (कृषि क्षेत्र)"

            val hasFineLocation = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val hasCoarseLocation = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasFineLocation || hasCoarseLocation) {
                try {
                    val cts = CancellationTokenSource()
                    val locResult = kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
                        fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                            cts.token
                        ).addOnSuccessListener { loc ->
                            if (continuation.isActive) continuation.resume(loc, null)
                        }.addOnFailureListener {
                            if (continuation.isActive) continuation.resume(null, null)
                        }
                        continuation.invokeOnCancellation { cts.cancel() }
                    }
                    if (locResult != null) {
                        latitude = locResult.latitude
                        longitude = locResult.longitude
                        resolvedCity = resolveLocationName(latitude, longitude)
                    }
                } catch (_: Exception) {
                    // Fallback to default location
                }
            }

            // Fetch live data from Open-Meteo Meteorological API
            val url = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current=temperature_2m,relative_humidity_2m,precipitation,weather_code,wind_speed_10m&hourly=precipitation_probability&timezone=auto"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val jsonObj = JSONObject(body)
                val current = jsonObj.optJSONObject("current")
                val hourly = jsonObj.optJSONObject("hourly")

                val temp = current?.optDouble("temperature_2m", 28.0) ?: 28.0
                val humidity = current?.optInt("relative_humidity_2m", 60) ?: 60
                val windSpeed = current?.optDouble("wind_speed_10m", 10.0) ?: 10.0
                val weatherCode = current?.optInt("weather_code", 0) ?: 0

                val rainProbArray = hourly?.optJSONArray("precipitation_probability")
                val rainProb = if (rainProbArray != null && rainProbArray.length() > 0) {
                    rainProbArray.getInt(0)
                } else 10

                val weatherDesc = getWeatherDescription(weatherCode)
                val (sprayStatus, advisory) = calculateSprayAdvisory(temp, humidity, windSpeed, rainProb)

                val newWeatherData = WeatherData(
                    locationName = resolvedCity,
                    temperature = temp,
                    humidity = humidity,
                    windSpeed = windSpeed,
                    rainProbability = rainProb,
                    weatherCode = weatherCode,
                    weatherDescription = weatherDesc,
                    sprayStatus = sprayStatus,
                    sprayAdvisory = advisory,
                    isLoaded = true,
                    isLoading = false
                )

                saveCachedWeather(newWeatherData)
                _weatherData.value = newWeatherData
            } else {
                _weatherData.value = _weatherData.value.copy(isLoading = false)
            }
            response.close()
        } catch (_: Exception) {
            _weatherData.value = _weatherData.value.copy(isLoading = false)
        }
    }

    private fun resolveLocationName(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                val state = addr.adminArea
                if (!locality.isNullOrBlank() && !state.isNullOrBlank()) {
                    "$locality, $state"
                } else locality ?: state ?: "Farm Location"
            } else "Farm Location"
        } catch (_: Exception) {
            "Farm Location"
        }
    }

    private fun getWeatherDescription(code: Int): String {
        return when (code) {
            0 -> "Clear Sky (साफ मौसम ☀️)"
            1, 2 -> "Mainly Clear (हल्के बादल 🌤️)"
            3 -> "Overcast (घने बादल ☁️)"
            45, 48 -> "Foggy / Mist (कोहरा 🌫️)"
            51, 53, 55 -> "Drizzle (बूंदाबांदी 🌦️)"
            61, 63, 65 -> "Rain Showers (बारिश 🌧️)"
            71, 73, 75 -> "Snowfall (बर्फबारी ❄️)"
            80, 81, 82 -> "Heavy Rain (तेज बारिश ⛈️)"
            95, 96, 99 -> "Thunderstorm (आंधी-तूफान ⚡)"
            else -> "Partly Cloudy (आंशिक बादल ⛅)"
        }
    }

    private fun calculateSprayAdvisory(
        temp: Double,
        humidity: Int,
        windSpeed: Double,
        rainProb: Int
    ): Pair<SprayStatus, String> {
        return when {
            rainProb >= 50 || windSpeed >= 22.0 -> {
                Pair(
                    SprayStatus.UNSAFE,
                    "⚠️ आज छिड़काव न करें! ${if (rainProb >= 50) "बारिश की संभावना ($rainProb%) है" else "तेज हवा ($windSpeed km/h) चल रही है"} - दवा व्यर्थ हो जाएगी।"
                )
            }
            windSpeed in 15.0..21.9 || temp > 36.0 || humidity > 85 -> {
                Pair(
                    SprayStatus.CAUTION,
                    "⚠️ सावधानी से छिड़काव करें: ${if (temp > 36.0) "अधिक तापमान है, सुबह/शाम करें" else "हल्की हवा ($windSpeed km/h) है, नोजल नीचे रखें"}।"
                )
            }
            else -> {
                Pair(
                    SprayStatus.SAFE,
                    "✅ छिड़काव के लिए उत्तम मौसम! (हवा शांत है और बारिश की कोई संभावना नहीं है)"
                )
            }
        }
    }
}
