-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*,Exceptions

# Keep all serialized data models so Moshi's reflection adapters can access them at runtime.
-keep class com.imnotndesh.truehub.data.models.** { *; }
-keep class com.imnotndesh.truehub.data.api.** { *; }

# Moshi reflection-based adapters.
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# Libs that use reflection / runtime lookups used across the app.
-keep class com.imnotndesh.truehub.data.helpers.EncryptedPrefs { *; }

# co-svg, markdown and chart libs occasionally reflect over internals.
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}
-dontwarn org.commonmark.**
