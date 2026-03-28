# Keep enums persisted by name in DataStore (SettingsManager.kt)
# R8 full mode can unbox enums, breaking .name lookups
-keepclassmembers enum com.nuttyknot.tennisscoretracker.MatchFormat {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers enum com.nuttyknot.tennisscoretracker.AppTheme {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


# Coroutine dispatcher ServiceLoader pattern
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory { *; }
-keep class kotlinx.coroutines.internal.MainDispatcherFactory { *; }

# Kotlin metadata for R8 full mode nullability handling
-keepattributes RuntimeVisibleAnnotations

# Suppress warnings for optional security providers (referenced but not bundled)
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
