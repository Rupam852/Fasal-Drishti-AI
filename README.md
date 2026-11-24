# 🌾 Fasal Drishti (Crop Disease Detection with AI)

[![Website](https://img.shields.io/badge/Website-fasaldrishti--ai.vercel.app-005B94?style=flat&logo=vercel)](https://fasaldrishti-ai.vercel.app)
[![Release](https://img.shields.io/badge/Release-v1.0.5-brightgreen.svg)](https://github.com/Rupam852/Fasal-Drishti-AI/releases)
[![Android](https://img.shields.io/badge/Android-13%2B%20(API%2033%2B)-green.svg)](https://developer.android.com/about/versions/13)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%26%20M3-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![TensorFlow Lite](https://img.shields.io/badge/AI-On--Device%20MobileNetV2-orange.svg)](https://www.tensorflow.org/lite)
[![Google Gemini AI](https://img.shields.io/badge/AI-Google%20Gemini%202.5%20Flash-blue.svg)](https://deepmind.google/technologies/gemini/)
[![NVIDIA NIM](https://img.shields.io/badge/AI-NVIDIA%20NIM%20Vision--LLM-76B900.svg)](https://www.nvidia.com/)

**Fasal Drishti** (फसल दृष्टि) is a production-grade, state-of-the-art **Crop Disease Detection System with AI** engineered for Indian farmers and agronomists. It features **sub-100ms offline on-device leaf pathology classification** (quantized MobileNetV2 across 38+ crop disease classes), **dual-engine cloud AI** (Google Gemini 2.5 Flash & NVIDIA NIM), **10-language voice assistance**, **ICAR-standard NPK fertilizer calculators**, **Live APMC Mandi Bhav with GPS detection**, **Soil Health Advisor**, **1-Tap PDF Prescriptions**, and **Sarkari Krishi Yojanaen**.

🌐 **Official Live Web Portal:** [https://fasaldrishti-ai.vercel.app](https://fasaldrishti-ai.vercel.app)  
📱 **Direct APK Download:** [Latest Universal Release APK](https://github.com/Rupam852/Fasal-Drishti-AI/releases/latest)

---

## 🌟 What's New in v1.0.5

- 🌐 **Official Web Portal on Vercel**: Real-time crop disease dataset catalog, clinical verification guidelines, theme switcher, and instant APK installation via QR code.
- 📍 **GPS Auto-Detected Live Mandi Bhav**: Automatically detects farmer's state from GPS location with 1-tap state selector fallback when location is disabled.
- 🧪 **Interactive Fertilizer & NPK Guide**: Question help dialog `(?)` with complete agronomy dosage instructions in 10 Indian languages.
- 🌱 **Soil Health & pH Master Guide**: Comprehensive soil type, NPK index, and pH balance guide in all supported regional languages.
- 🎨 **Refined Fluid UI**: Transparent, modern weather cards and zero visual clutter.
- 🎙️ **Multi-Lingual Voice Assistant**: Clean STT & TTS integration with separated controls.

---

## ✨ Core Features & Architecture

### 1. 🧠 Dual-Tier Crop Disease Diagnosis
- **Tier 1 (On-Device Edge AI)**: Fine-tuned MobileNetV2 (`224x224x3 RGB`, 38 PlantVillage classes + domain cohorts) executing locally via TensorFlow Lite & NNAPI hardware acceleration for instantaneous, sub-40ms offline diagnosis without internet connectivity.
- **Tier 2 (Cloud Multimodal Reasoning)**: High-resolution foliar examination using **Google Gemini 2.5 Flash** and **NVIDIA NIM Agronomy Vision** with multi-key failover and localized treatment prescriptions for complex pathological edge cases.

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

## 🔬 Model Training & Edge Quantization

The offline model training pipeline and export notebooks are located in [`Fasal-Drishti-ML-Model-Train/`](./Fasal-Drishti-ML-Model-Train/):

* **Interactive Notebook:** [`fasal-drishti-offline-model-trainer.ipynb`](./Fasal-Drishti-ML-Model-Train/fasal-drishti-offline-model-trainer.ipynb)
* **Backbone Architecture:** MobileNetV2 (Width multiplier $\alpha=1.0$) with Depthwise Separable Convolutions.
* **Optimization:** Two-phase transfer learning (Initial frozen backbone $\eta=10^{-3}$, differential fine-tuning $\eta=10^{-5}$) with Early Stopping.
* **Quantization:** 8-bit Integer (INT8) Post-Training Quantization reducing model size from **58.4 MB to 2.74 MB** with sub-40ms on-device latency.
* **Evaluation Metrics:** **96.84% Overall Accuracy**, 96.52% Precision, 96.18% Recall, and 96.35% F1-score across 38 crop pathology classes.

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
├── Fasal-Drishti-ML-Model-Train/       # Edge AI Training Pipelines & Export
│   ├── fasal-drishti-offline-model-trainer.ipynb  # Interactive Model Trainer Notebook
│   ├── labels.txt                      # 38-class leaf pathology label map
│   └── model_4_mobilenet_finetuned.tflite        # Quantized MobileNetV2 FlatBuffer
├── website/                            # Official Production Web Portal (Vercel)
│   ├── index.html                      # Interactive web app & dataset directory
│   ├── style.css                       # Responsive design & Dark/Light mode theme
│   ├── script.js                       # UI controller, language switcher, dataset modal
│   └── vercel.json                     # Vercel deployment configuration
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
- **JDK 17** (OpenJDK 17.0.9+)
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
- **User Privacy First**: All preliminary leaf diagnostics, prescription histories, and conversations with AI Salah are stored securely in local encrypted SQLite database.
- **Secure Supabase Integration**: Row-Level Security (RLS) guarantees user scan histories and chat records remain strictly private.
- **Zero Third-Party Ads**: No tracking pixels, zero analytics leaks, and no monetization of farmer records.
- **Dynamic AI Key Resolution**: Secure credential handling for Google Gemini and NVIDIA NIM vision backends.

---

## 👥 Project Team & Contributors

| Contributor | Role | GitHub |
|---|---|---|
| **Rupam Bairagya** | **Lead AI & Full-Stack Architect** | [@Rupam852](https://github.com/Rupam852) |
| **Pritam Rakshit** | **Core Contributor & Developer** | [@pritamrakshit24](https://github.com/pritamrakshit24) |
| **Anish Das** | **Core Contributor & Developer** | [@Anish061106](https://github.com/Anish061106) |
| **Abhijit Sahoo** | **Core Contributor & Developer** | [@abhijitsahoo70](https://github.com/abhijitsahoo70) |
| **Tiasa Neogi** | **Core Contributor & Developer** | [@Katha04](https://github.com/Katha04) |
| **Tiyasha Ghosh** | **Core Team Contributor** | [@bwubts24051-ux](https://github.com/bwubts24051-ux) |

Contributions, issues, and feature requests are welcome! Feel free to check the [Issues Page](https://github.com/Rupam852/Fasal-Drishti-AI/issues).

---

## 📄 License
This project is licensed under the **Apache License 2.0** — see the [LICENSE](LICENSE) file for details.
