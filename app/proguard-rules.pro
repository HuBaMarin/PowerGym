# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep PowerGym Application class
-keep class com.amarina.powergym.PowerGymApplication { *; }

# Keep all database entities and DAOs
-keep class com.amarina.powergym.database.entities.** { *; }
-keep class com.amarina.powergym.database.dao.** { *; }
-keep interface com.amarina.powergym.database.dao.** { *; }

# Keep Room database
-keep class com.amarina.powergym.database.PowerGymDatabase { *; }

# Keep ViewModels and their factories
-keep class com.amarina.powergym.ui.viewmodel.** { *; }
-keep class com.amarina.powergym.ui.factory.** { *; }

# Keep Activities and their intent extras
-keep class com.amarina.powergym.ui.activity.** { *; }

# Keep Gson classes for JSON parsing
-keepattributes Signature
-keepattributes *Annotation*

# Keep coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep LanguageHelper and translation utilities
-keep class com.amarina.powergym.utils.LanguageHelper { *; }
-keep class com.amarina.powergym.utils.TranslationHelper { *; }

# Keep notification receiver
-keep class com.amarina.powergym.receivers.ReminderReceiver { *; }

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# Keep chart library classes
-keep class com.github.mikephil.charting.** { *; }
