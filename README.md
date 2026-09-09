# 🌾 Fasal Drishti (फसल दृष्टि) — AI Precision Agronomy & Crop Vision App

[![Version](https://img.shields.io/badge/Version-v1.0.5-brightgreen.svg)](https://github.com/Rupam852/Fasal-Drishti-AI/releases)
[![Android 13+](https://img.shields.io/badge/Android-13%2B%20(API%2033%2B)-green.svg)](https://developer.android.com/about/versions/13)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%26%20M3-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![TensorFlow Lite](https://img.shields.io/badge/AI-On--Device%20MobileNetV2-orange.svg)](https://www.tensorflow.org/lite)
[![Google Gemini AI](https://img.shields.io/badge/AI-Google%20Gemini%202.5%20Flash-blue.svg)](https://deepmind.google/technologies/gemini/)
[![NVIDIA NIM](https://img.shields.io/badge/AI-NVIDIA%20NIM%20Vision--LLM-76B900.svg)](https://www.nvidia.com/)

**Fasal Drishti** (फसल दृष्टि) is a production-grade, state-of-the-art AI-powered precision agriculture mobile application engineered for Indian farmers and agronomists. It features **on-device sub-100ms offline disease diagnosis** (fine-tuned MobileNetV2 over 38 crop disease classes), **dual-engine cloud AI** (Google Gemini & NVIDIA NIM), **10-language voice assistance**, **ICAR-standard NPK fertilizer calculators**, **Live APMC Mandi Bhav with GPS detection**, **Soil Health Advisor**, **1-Tap PDF Prescriptions**, and **Sarkari Krishi Yojanaen**.

---

## 🌟 What's New in v1.0.5

- 📍 **GPS Auto-Detected Live Mandi Bhav**: Automatically detects farmer's state from GPS location with 1-tap state selector fallback when location is disabled.
- 🧪 **Interactive Fertilizer & NPK Guide**: Question help dialog `(?)` with complete agronomy dosage instructions in 10 Indian languages.
- 🌱 **Soil Health & pH Master Guide**: Comprehensive soil type, NPK index, and pH balance guide in all supported regional languages.
- 🎨 **Refined Fluid UI**: Transparent, modern weather cards and zero visual clutter.
- 🎙️ **Multi-Lingual Voice Assistant**: Clean STT & TTS integration with separated controls.

---

## ✨ Core Features & Architecture

### 1. 🧠 Dual-Tier Crop Disease Diagnosis
- **Tier 1 (On-Device Edge AI)**: Fine-tuned MobileNetV2 (`224x224x3 RGB`, 38 PlantVillage classes) executing locally via TensorFlow Lite & NNAPI hardware acceleration for instantaneous, offline diagnosis.
- **Tier 2 (Cloud Vision-LLM Reasoning)**: High-resolution leaf examination using **Google Gemini 2.5 Flash** and **NVIDIA NIM Agronomy Vision** with multi-key failover and localized treatment prescriptions.

### 2. 🧪 Fertilizer & NPK Dosage Calculator
- Formulates exact **Urea (46% N)**, **DAP (18:46:0)**, **MOP / Potash (60% K₂O)**, **Zinc Sulphate**, and **Organic FYM** requirements based on ICAR standards.
- Supports native land measurements: **Acre**, **Bigha**, **Hectare**, and **Guntha**.
- Includes foliar spray water calculation and staged application timelines (Basal, Vegetative, Flowering).

### 3. 🌱 Soil Health & Crop Advisor AI
- Evaluates **Soil Texture** (Alluvial, Black, Red, Sandy Loam, Clay), **NPK Levels**, and **pH Range (Acidic to Alkaline)**.
- Recommends best suited crops and corrective organic amendments (Gypsum, Lime, Bio-fertilizers).

### 4. 🌾 Live APMC Mandi Bhav
- Real-time commodity market prices directly from Indian Mandis & Agmarknet.
- GPS auto-detection of state, commodity filtering (Wheat, Rice, Cotton, Mustard, Potato, Onion, etc.), and AI buy/sell/hold market advice.

### 5. 🎙️ 10-Language Multilingual Support & Voice AI
- Native UI and speech synthesis in **English, Hindi (हिंदी), Bengali (বাংলা), Marathi (मराठी), Punjabi (ਪੰਜਾਬੀ), Gujarati (ગુજરાતી), Telugu (తెలుగు), Tamil (தமிழ்), Kannada (ಕನ್ನಡ), and Odia (ଓଡ଼ିଆ)**.
- Hands-free microphone voice queries and natural voice prescription audio playback.

### 6. 📄 1-Tap PDF Doctor Prescription & WhatsApp Sharing
- Generates formatted agronomy prescription PDFs with crop photo, diagnosed symptoms, chemical dosage, organic remedies, and QR verification for direct sharing on WhatsApp.

### 7. 🏛️ Sarkari Krishi Yojanaen (Government Schemes)
- Direct eligibility guides and portal access for **PM-Kisan Samman Nidhi**, **Pradhan Mantri Fasal Bima Yojana (PMFBY)**, **Kisan Credit Card (KCC)**, and **Soil Health Card Scheme**.

### 8. 🔄 In-App Auto-Updater
- Real-time GitHub release polling, automatic download manager, and background notification alerts for new versions.

---

## 🏗️ Repository Structure

```
.
├── app/                                # Native Android Application (Kotlin + Jetpack Compose)
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── assets/                     # TFLite models, labels.txt, agmarknet data
│   │   ├── res/                        # Vector drawables, adaptive launcher icons, XMLs
│   │   └── java/com/fasaldrishti/app/
│   │       ├── data/                   # Room DB, Retrofit API, AgmarknetClient, GeminiClient
│   │       │   ├── local/              # LocalChatManager, LanguageManager, ScanDao
│   │       │   └── remote/             # SupabaseClient, UpdateManager, WeatherClient
│   │       ├── domain/model/           # ScanRecord, MandiRecord, CropNutrientProfile
│   │       ├── ui/
│   │       │   ├── components/         # TopBars, BottomBar, Dialogs, VoiceWaveform
│   │       │   ├── localization/       # AppStrings (10 Regional Indian Languages)
│   │       │   ├── screens/            # Home, Scan, Diagnosis, Tools (Mandi, Fertilizer, Soil, Yojanaen),
│   │       │   │                       # Chat, History, Profile, Settings, About, Updater
│   │       │   └── theme/              # Emerald & Solar Gold Material 3 Theme
│   │       └── util/                   # PdfGenerator, VoiceAssistantManager, NetworkMonitor
│   └── build.gradle.kts                # Android build configuration (minSdk 33, targetSdk 34)
├── backend/                            # Optional FastAPI Microservice
│   ├── app/
│   │   ├── main.py                     # REST API endpoints
│   │   ├── model_handler.py            # Image inference pipeline
│   │   └── disease_db.py               # 38-class agronomy disease encyclopedia
│   ├── Dockerfile
│   └── requirements.txt
├── supabase/                           # Cloud Database Schemas & Migrations
│   ├── schema.sql                      # Supabase PostgreSQL tables & RLS policies
│   └── insert_gemini_key.sql           # Dynamic AI API key vault
└── README.md
```

---

## 🚀 Getting Started & Installation

### Prerequisites
- **Android Studio Hedgehog (2023.1.1)** or newer
- **JDK 17**
- **Android Device or Emulator** running **Android 13+ (API 33+)**

### Build & Run
```bash
# 1. Clone the repository
git clone https://github.com/Rupam852/Fasal-Drishti-AI.git
cd Fasal-Drishti-AI

# 2. Build Debug APK
./gradlew assembleDebug

# 3. Build Signed Release APK
./gradlew assembleRelease
```

The compiled release APK will be located at:
`app/build/outputs/apk/release/app-release.apk`

---

## 🔒 Security & Privacy
- **Offline First**: All image scans are classified locally on the device by default. No photos are sent to external servers without explicit user request.
- **Secure Supabase Integration**: Row-Level Security (RLS) guarantees user scan histories and chat records remain strictly private.
- **Zero Plaintext Secrets**: Dynamic encrypted key resolution for AI services.

---

## 👨‍💻 Author & Contributions
- **Lead Developer**: [Rupam Bairagya](https://github.com/Rupam852)
- Contributions, issues, and feature requests are welcome! Feel free to check the [Issues Page](https://github.com/Rupam852/Fasal-Drishti-AI/issues).

---

## 📄 License
This project is licensed under the **Apache License 2.0** — see the [LICENSE](LICENSE) file for details.
