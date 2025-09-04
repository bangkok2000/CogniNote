# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep Room entities and DAOs
-keep class com.cogninote.app.data.entities.** { *; }
-keep class com.cogninote.app.data.dao.** { *; }
-keep class com.cogninote.app.data.database.** { *; }

# Keep Hilt components
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.* <methods>;
}

# Keep ViewModels
-keep class com.cogninote.app.presentation.viewmodel.** { *; }

# Keep SQLCipher classes
-keep class net.zetetic.database.** { *; }

# Keep Kotlinx serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.cogninote.app.**$$serializer { *; }
-keepclassmembers class com.cogninote.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.cogninote.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep TensorFlow Lite
-keep class org.tensorflow.lite.** { *; }
-keep class org.tensorflow.lite.support.** { *; }

# Keep biometric classes
-keep class androidx.biometric.** { *; }

# Rich Text Editor
-keep class com.mohamedrejeb.richeditor.** { *; }

# Kotlinx datetime
-keep class kotlinx.datetime.** { *; }

# Keep Jetpack Compose
-keep class androidx.compose.** { *; }
-keep class androidx.activity.compose.** { *; }
-keep class androidx.navigation.compose.** { *; }
-keep class androidx.hilt.navigation.compose.** { *; }

# Generic rules for reflection
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
