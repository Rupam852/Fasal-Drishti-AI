# Fasal Drishti AI - Offline Model Fleet (62 Agricultural Categories)

Ye folder Fasal Drishti AI ke saare offline on-device neural network models, datasets taxonomy, aur training artifacts ko contain karta hai.

---

## 📁 Model Directory Structure

### 1. `01_Base_Model_43_Classes/`
- **Architecture:** MobileNetV2 Fine-Tuned (Alpha 0.35 / 1.0)
- **Input Dimensions:** `224x224x3` (RGB normalized `[0, 1]`)
- **Format:** TFLite FP32 (`2.74 MB`) + Keras (`39.0 MB`)
- **Categories:** 43 major broadleaf crops (Potato, Tomato, Apple, Corn, Grape, Cotton, Pepper, Peach, Cherry, Strawberry, Soybean, Squash)
- **Opcode Compatibility:** Patched opcode 9 (`FULLY_CONNECTED` version = 1) for universal Android 7.0 to Android 15+ support.
- **Files:**
  - `model_4_mobilenet_finetuned.tflite`
  - `model_4_mobilenet_finetuned.keras`
  - `labels.txt`

---

### 2. `02_Rice_Specialist_Model/`
- **Architecture:** MobileNetV2 Deep Residual Specialist
- **Input Dimensions:** `224x224x3` (RGB normalized `[0, 1]`)
- **Format:** TFLite FP16 Quantized (`3.13 MB`)
- **Categories (8 Rice Classes):**
  1. `Rice___Bacterial_leaf_blight`
  2. `Rice___Brown_spot`
  3. `Rice___Healthy`
  4. `Rice___Hispa`
  5. `Rice___Leaf_Blast`
  6. `Rice___Leaf_Scald`
  7. `Rice___Neck_Blast`
  8. `Rice___Tungro`
- **Files:**
  - `model_rice_fp16.tflite`
  - `labels_rice.txt`

---

### 3. `03_Wheat_Specialist_Model/`
- **Architecture:** MobileNetV2 Agronomic Feature Specialist
- **Input Dimensions:** `224x224x3` (RGB normalized `[0, 1]`)
- **Format:** TFLite FP16 Quantized (`3.14 MB`)
- **Categories (11 Wheat Classes):**
  1. `Wheat___Brown_Rust`
  2. `Wheat___Healthy`
  3. `Wheat___Loose_Smut`
  4. `Wheat___Powdery_Mildew`
  5. `Wheat___Septoria_Leaf_Blotch`
  6. `Wheat___Stem_Rust`
  7. `Wheat___Stripe_Rust`
  8. `Wheat___Tan_Spot`
  9. `Wheat___Wheat_Blast`
  10. `Wheat___Yellow_Rust`
  11. `Wheat___Crown_Rot`
- **Files:**
  - `model_wheat_fp16.tflite`
  - `labels_wheat.txt`

---

## 🚀 How Multi-Model Inference Works on Android

1. **Pre-Inference Guard:** `TFLiteDiseaseClassifier.kt` runs **Chromatic Foliage Guard** (< 3ms) to filter out walls, ceilings, fingers, and non-plant objects.
2. **Parallel CPU Inference:** Uses XNNPACK with 4 threads for ultra-fast < 45ms inference.
3. **Consensus Arbiter:** Combines predictions from Base + Rice + Wheat engines to pick the most confident specialist diagnosis.
