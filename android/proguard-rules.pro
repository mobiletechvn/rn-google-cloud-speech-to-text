# ProGuard rules for Google Cloud Speech-to-Text REST API

# Keep JSON classes
-keep class org.json.** { *; }

# Keep HTTP connection classes
-keep class java.net.** { *; }
-keep class javax.net.ssl.** { *; }

# Keep Base64 encoder/decoder
-keep class java.util.Base64 { *; }

# Suppress warnings for network operations
-dontwarn java.net.**
-dontwarn javax.net.ssl.**

# Keep annotations
-keepattributes *Annotation*

# Keep native method names
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep React Native TurboModule interfaces
-keep class com.rngooglecloudspeechtotext.** { *; }
-keep class com.facebook.react.turbomodule.** { *; }

# Keep audio recording classes
-keep class android.media.AudioRecord { *; }
-keep class android.media.AudioFormat { *; }
-keep class android.media.MediaRecorder { *; }
