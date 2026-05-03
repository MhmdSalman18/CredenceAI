# Hilt rules
-keep public class * extends android.app.Service
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.view.View
-keep class  **.*_HiltModules* { *; }
-keep class  **.*_HiltComponents* { *; }
-keep class  **.*_EntryPoint* { *; }

# Room rules
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity
-keep interface * extends androidx.room.Dao
-keep class * extends androidx.room.TypeConverter
-dontwarn androidx.room.paging.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keepclassmembernames class kotlinx.coroutines.android.HandlerContext$ScheduledRunnable {
    long nanoseconds;
}

# DataStore / Protobuf
-keep class androidx.datastore.** { *; }

# Timber
-keep class timber.log.Timber* { *; }

# App specific models (to prevent obfuscation of JSON/DB fields if needed)
-keep class com.credenceai.app.domain.model.** { *; }
-keep class com.credenceai.app.data.model.** { *; }
