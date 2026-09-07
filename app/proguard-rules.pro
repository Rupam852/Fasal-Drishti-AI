# Proguard & R8 rules for Fasal Drishti

-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep data models, DTOs, Room Entities
-keep class com.fasaldrishti.app.data.remote.dto.** { *; }
-keep class com.fasaldrishti.app.domain.model.** { *; }
-keep class com.fasaldrishti.app.data.local.** { *; }
-keep class com.fasaldrishti.app.data.remote.** { *; }

# Gson serialization
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }

# Retrofit
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# TensorFlow Lite & JNI Native delegates
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

# Coroutines & AndroidX
-dontwarn kotlinx.coroutines.**
-dontwarn androidx.**
