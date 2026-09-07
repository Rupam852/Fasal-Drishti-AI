# Proguard rules for Fasal Drishti
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.jetbrains.annotations.Nullable *;
    @org.jetbrains.annotations.NotNull *;
}
-keep class com.fasaldrishti.app.data.remote.dto.** { *; }
-keep class com.fasaldrishti.app.domain.model.** { *; }
