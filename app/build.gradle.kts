plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.fasaldrishti.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.fasaldrishti.app"
        minSdk = 33
        targetSdk = 34
        versionCode = 9
        versionName = "1.0.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "SUPABASE_URL", "\"https://tajizxhfxewkelzrmgux.supabase.co\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRhaml6eGhmeGV3a2VsenJtZ3V4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODg3ODMxMjUsImV4cCI6MjEwNDM1OTEyNX0.l39iKsN8kWzuQt2-T0dISIokx9Ys8E1ITXQfZcTi3Zw\"")
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"856940513973-se4ove810fk03ncojcct1r1rdlunp8bl.apps.googleusercontent.com\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file("fasal_drishti_release.keystore")
            storePassword = "FasalDrishti2026"
            keyAlias = "fasaldrishti"
            keyPassword = "FasalDrishti2026"
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    androidResources {
        noCompress += listOf("tflite", "keras")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // On-Device AI: TensorFlow Lite
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.gpu)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Coil Image Loading
    implementation(libs.coil.compose)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Supabase
    implementation(libs.supabase.gotrue)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.storage)

    // Lottie
    implementation(libs.lottie.compose)

    // Google Play Services Native Sign-In (Direct Android Keystore/SHA-1 Account Picker)
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    // Google Play Services Location for dynamic Agro-Weather & Spray Index
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // Chrome Custom Tabs for OAuth
    implementation("androidx.browser:browser:1.8.0")

    debugImplementation(libs.androidx.ui.tooling)
}
