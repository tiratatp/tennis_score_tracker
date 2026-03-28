# Keep ViewModel constructor — instantiated via reflection by ViewModelProvider
-keep class com.nuttyknot.tennisscoretracker.wear.WearRemoteViewModel { <init>(...); }

# Keep WearDataListenerService — instantiated by Wear OS system via manifest class name
-keep class com.nuttyknot.tennisscoretracker.wear.WearDataListenerService { *; }

# Coroutine dispatcher ServiceLoader pattern
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory { *; }
-keep class kotlinx.coroutines.internal.MainDispatcherFactory { *; }

# Kotlin metadata for R8 full mode nullability handling
-keepattributes RuntimeVisibleAnnotations

# Suppress warnings for optional security providers (referenced but not bundled)
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
