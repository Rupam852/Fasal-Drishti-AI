# Proguard & R8 rules for Fasal Drishti

-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep data models and DTOs
-keep class com.fasaldrishti.app.data.remote.dto.** { *; }
-keep class com.fasaldrishti.app.domain.model.** { *; }
-keep class com.fasaldrishti.app.data.local.** { *; }

# TensorFlow Lite & JNI
-dontwarn org.tensorflow.lite.**
-dontwarn com.google.android.gms.tflite.**
-keep class org.tensorflow.lite.** { *; }
-keep class com.google.android.gms.tflite.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}

# OkHttp & Okio
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Coroutines
-dontwarn kotlinx.coroutines.**
-dontwarn androidx.**
