# 🌾 Fasal Drishti (फसल दृष्टि) — AI Crop Disease Vision & Agronomy Advisory App

[![Android 13+](https://img.shields.io/badge/Android-13%2B%20(API%2033%2B)-green.svg)](https://developer.android.com/about/versions/13)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%26%20M3-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![TensorFlow Lite](https://img.shields.io/badge/AI-On--Device%20MobileNetV2-orange.svg)](https://www.tensorflow.org/lite)
[![FastAPI](https://img.shields.io/badge/Backend-FastAPI-teal.svg)](https://fastapi.tiangolo.com/)

**Fasal Drishti** (फसल दृष्टि) is a state-of-the-art AI-powered crop disease detection and farmer assistance mobile application for Android. It runs fine-tuned **MobileNetV2 deep learning models directly on-device** (offline-first) to instantly diagnose 38 crop disease classes from leaf photos in sub-100ms, provides chemical and organic treatment dosages, and connects to an **NVIDIA NIM Agronomy Vision-LLM** for expert advisory.

---

## ✨ Key Features

- 🧠 **On-Device Primary AI Inference**: Powered by a fine-tuned MobileNetV2 Keras model (`224x224x3 RGB`, 38 PlantVillage classes) executing locally via TensorFlow Lite & NNAPI hardware acceleration.
- 📱 **Android 13+ Native & 120Hz Refresh Rate**: Optimized for modern Android devices (minSdk 33, targetSdk 34) with dynamic 120Hz/144Hz high refresh rate locking and Material You Dynamic Theming.
- 🌾 **Dynamic Theme-Adaptive App Icon**: Custom single golden-emerald rice stalk + AI vision eye logo that tints dynamically with Android 13+ wallpaper palettes.
- 📷 **CameraX Smart Leaf Scanner**: Live camera preview with guided pulsing reticle frames, gallery picker, flash control, and shutter animations.
- 💊 **Actionable Agronomy Advice**: Symptoms, chemical treatments (e.g. Mancozeb, Ridomil MZ dosages), organic remedies, and prevention tips in both **English and Hindi (हिंदी)**.
- 💬 **Ask Fasal AI (NVIDIA NIM Chat)**: Conversational AI agronomist assistant for contextual diagnosis questions and dosage calculations.
- 🔄 **In-App Auto-Updater**: Automatic update checks on startup, Android notification bar alerts, red dot badge indicators, and direct download links.
- 🗄️ **Supabase & Room Offline Sync**: Room SQLite for local offline history storage + Supabase Auth (Google & GitHub OAuth), PostgreSQL, and Storage.

---

## 🏗️ Project Structure

```
.
├── model_4_mobilenet_finetuned.keras   # 23MB Pretrained Keras MobileNetV2 model (38 classes)
├── backend/                            # FastAPI ML Serving & NVIDIA NIM fallback
│   ├── app/
│   │   ├── main.py                     # FastAPI REST API endpoints
│   │   ├── model_handler.py            # Image preprocessing & inference
│   │   ├── disease_db.py               # 38-class agronomy knowledge base (Hindi & English)
│   │   └── nvidia_service.py           # NVIDIA NIM secondary LLM service
│   ├── Dockerfile
│   ├── render.yaml                     # Render 1-click cloud deploy
│   └── convert_tflite.py               # Keras -> TFLite converter
├── supabase/
│   ├── schema.sql                      # PostgreSQL tables (scans, disease_info) & RLS policies
│   └── seed_diseases.sql               # Seed data for all 38 classes
└── app/                                # Native Android Kotlin application
    ├── src/main/
    │   ├── AndroidManifest.xml
    │   ├── assets/                     # labels.txt & TFLite model assets
    │   ├── res/                        # Themed adaptive vector icons & colors
    │   └── java/com/fasaldrishti/app/
    │       ├── data/                   # Room DB, Retrofit API, SupabaseManager, TFLite Classifier
    │       ├── domain/                 # Domain models & repository contracts
    │       └── ui/
    │           ├── theme/              # Material 3 Green Design System
    │           ├── components/         # FasalBottomBar, AppLogo, GradientButton, ConfidenceRing
    │           └── screens/            # Splash, Onboarding, Login, Home, Scan, Analyzing,
    │                                   # Result, History, Chat, Profile, Settings, About, Updater, Library
```

---

## 🚀 Getting Started

### 1. Android Studio Setup
1. Open the repository root in **Android Studio (Hedgehog or newer)**.
2. Ensure JDK 17 is selected in Project Structure.
3. Build and Run on any Android 13+ (API 33+) physical phone or emulator.

### 2. Running the FastAPI Backend
```bash
cd backend
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```
API Documentation: `http://localhost:8000/docs`

---

## 👥 Contributors
- **Lead Developer**: [@Rupam852](https://github.com/Rupam852)
- **Fasal Drishti Open-Source Community**
