package com.fasaldrishti.app

import android.app.Application
import com.fasaldrishti.app.data.local.AppDatabase
import com.fasaldrishti.app.data.ml.TFLiteDiseaseClassifier
import com.fasaldrishti.app.data.remote.NvidiaClient
import com.fasaldrishti.app.data.remote.PredictApi
import com.fasaldrishti.app.data.remote.SupabaseManager
import com.fasaldrishti.app.data.remote.UpdateManager
import com.fasaldrishti.app.data.repository.AuthRepositoryImpl
import com.fasaldrishti.app.data.repository.DiseaseRepositoryImpl
import com.fasaldrishti.app.data.repository.ScanRepositoryImpl
import com.fasaldrishti.app.domain.repository.AuthRepository
import com.fasaldrishti.app.domain.repository.DiseaseRepository
import com.fasaldrishti.app.domain.repository.ScanRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

import com.fasaldrishti.app.data.remote.WeatherManager

class FasalDrishtiApp : Application() {

    lateinit var scanRepository: ScanRepository
        private set
    lateinit var authRepository: AuthRepository
        private set
    lateinit var diseaseRepository: DiseaseRepository
        private set
    lateinit var onDeviceClassifier: TFLiteDiseaseClassifier
        private set
    lateinit var updateManager: UpdateManager
        private set
    lateinit var supabaseManager: SupabaseManager
        private set
    lateinit var weatherManager: WeatherManager
        private set
    lateinit var themeManager: com.fasaldrishti.app.data.local.ThemeManager
        private set
    lateinit var languageManager: com.fasaldrishti.app.data.local.LanguageManager
        private set
    lateinit var networkMonitor: com.fasaldrishti.app.util.NetworkMonitor
        private set
    lateinit var geminiClient: com.fasaldrishti.app.data.remote.GeminiClient
        private set

    override fun onCreate() {
        super.onCreate()

        // 0. Initialize Network Monitor
        networkMonitor = com.fasaldrishti.app.util.NetworkMonitor(this)

        // 1. Initialize Local Database, Theme & Language Manager
        val database = AppDatabase.getInstance(this)
        themeManager = com.fasaldrishti.app.data.local.ThemeManager(this)
        languageManager = com.fasaldrishti.app.data.local.LanguageManager(this)

        // 2. Initialize Networking (Retrofit)
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://tajizxhfxewkelzrmgux.supabase.co/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val predictApi = retrofit.create(PredictApi::class.java)

        // 3. Initialize Supabase Manager (Handles remote configs, Auth, DB)
        supabaseManager = SupabaseManager(this)

        // 4. Initialize Weather Manager for live agro-weather & spray condition
        weatherManager = WeatherManager(this)
        CoroutineScope(Dispatchers.IO).launch {
            weatherManager.refreshWeather()
        }

        // 5. Initialize On-Device TensorFlow Lite Classifier (Primary Neural Net)
        onDeviceClassifier = TFLiteDiseaseClassifier(this)

        // 6. Initialize AiConfigManager & AI Clients (Gemini & NVIDIA NIM)
        val aiConfigManager = com.fasaldrishti.app.data.local.AiConfigManager(this)

        val geminiClient = com.fasaldrishti.app.data.remote.GeminiClient(
            supabaseManager = supabaseManager,
            aiConfigManager = aiConfigManager
        )
        this.geminiClient = geminiClient
        val nvidiaClient = NvidiaClient(
            supabaseManager = supabaseManager,
            aiConfigManager = aiConfigManager
        )

        // 7. Initialize Disease Repository
        diseaseRepository = DiseaseRepositoryImpl(
            predictApi = predictApi,
            geminiClient = geminiClient,
            nvidiaClient = nvidiaClient
        )

        // 8. Initialize Scan Repository
        scanRepository = ScanRepositoryImpl(
            scanDao = database.scanDao(),
            predictApi = predictApi,
            supabaseManager = supabaseManager,
            onDeviceClassifier = onDeviceClassifier,
            diseaseRepository = diseaseRepository,
            geminiClient = geminiClient,
            nvidiaClient = nvidiaClient,
            aiConfigManager = aiConfigManager
        )

        authRepository = AuthRepositoryImpl(supabaseManager = supabaseManager)

        // 9. Initialize In-App Update Manager & run auto-check on startup
        updateManager = UpdateManager(this)
        if (updateManager.autoCheckEnabled.value) {
            CoroutineScope(Dispatchers.IO).launch {
                updateManager.checkForUpdates(isAutoCheck = true)
            }
        }
    }
}
