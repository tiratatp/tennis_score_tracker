package com.nuttyknot.tennisscoretracker

import android.content.Context
import android.view.KeyEvent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

// Property delegate for DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Suppress("TooManyFunctions")
class SettingsManager(private val context: Context) {
    companion object {
        val KEYCODE = intPreferencesKey("keycode")
        val DOUBLE_CLICK_LATENCY = longPreferencesKey("double_click_latency")
        val LONG_PRESS_LATENCY = longPreferencesKey("long_press_latency")
        val USER_NAME = androidx.datastore.preferences.core.stringPreferencesKey("user_name")
        val OPPONENT_NAME = androidx.datastore.preferences.core.stringPreferencesKey("opponent_name")
        val INITIAL_SERVER_IS_USER = androidx.datastore.preferences.core.booleanPreferencesKey("initial_server_is_user")
        val HAS_SEEN_HELP = androidx.datastore.preferences.core.booleanPreferencesKey("has_seen_help")
        val APP_THEME = androidx.datastore.preferences.core.stringPreferencesKey("app_theme")
        val SPORT = stringPreferencesKey("sport")
        val MATCH_FORMAT = stringPreferencesKey("match_format")
        val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
        val ANNOUNCER_VOICE = stringPreferencesKey("announcer_voice")
        val MATCH_STATE_JSON = stringPreferencesKey("match_state_json")

        const val DEFAULT_KEYCODE = KeyEvent.KEYCODE_VOLUME_UP
        const val DEFAULT_DOUBLE_CLICK_LATENCY = 300L
        const val DEFAULT_LONG_PRESS_LATENCY = 1000L
        const val DEFAULT_USER_NAME = ""
        const val DEFAULT_OPPONENT_NAME = ""
        const val DEFAULT_INITIAL_SERVER_IS_USER = true
        const val DEFAULT_HAS_SEEN_HELP = false
        val DEFAULT_APP_THEME = AppTheme.SKY_BLUE
        val DEFAULT_SPORT = Sport.TENNIS
        val DEFAULT_MATCH_FORMAT = MatchFormat.STANDARD
        const val DEFAULT_TTS_ENABLED = true
        const val DEFAULT_ANNOUNCER_VOICE = ""
    }

    private val _isDetectingKeycode = MutableStateFlow(false)
    val isDetectingKeycode: StateFlow<Boolean> = _isDetectingKeycode.asStateFlow()

    private val _detectedKeycode = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val detectedKeycode = _detectedKeycode.asSharedFlow()

    fun startKeycodeDetection() {
        _isDetectingKeycode.value = true
    }

    fun stopKeycodeDetection() {
        _isDetectingKeycode.value = false
    }

    fun onKeycodeDetected(keycode: Int) {
        _detectedKeycode.tryEmit(keycode)
    }

    val keycodeFlow: Flow<Int> =
        context.dataStore.data.map { preferences ->
            preferences[KEYCODE] ?: DEFAULT_KEYCODE
        }

    val doubleClickLatencyFlow: Flow<Long> =
        context.dataStore.data.map { preferences ->
            preferences[DOUBLE_CLICK_LATENCY] ?: DEFAULT_DOUBLE_CLICK_LATENCY
        }

    val longPressLatencyFlow: Flow<Long> =
        context.dataStore.data.map { preferences ->
            preferences[LONG_PRESS_LATENCY] ?: DEFAULT_LONG_PRESS_LATENCY
        }

    val userNameFlow: Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[USER_NAME] ?: DEFAULT_USER_NAME
        }

    val opponentNameFlow: Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[OPPONENT_NAME] ?: DEFAULT_OPPONENT_NAME
        }

    val initialServerIsUserFlow: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[INITIAL_SERVER_IS_USER] ?: DEFAULT_INITIAL_SERVER_IS_USER
        }

    val hasSeenHelpFlow: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[HAS_SEEN_HELP] ?: DEFAULT_HAS_SEEN_HELP
        }

    val appThemeFlow: Flow<AppTheme> =
        context.dataStore.data.map { preferences ->
            val themeName = preferences[APP_THEME] ?: DEFAULT_APP_THEME.name
            AppTheme.entries.find { it.name == themeName } ?: DEFAULT_APP_THEME
        }

    val sportFlow: Flow<Sport> =
        context.dataStore.data.map { preferences ->
            val sportName = preferences[SPORT] ?: DEFAULT_SPORT.name
            Sport.entries.find { it.name == sportName } ?: DEFAULT_SPORT
        }

    val matchFormatFlow: Flow<MatchFormat> =
        context.dataStore.data.map { preferences ->
            val formatName = preferences[MATCH_FORMAT] ?: DEFAULT_MATCH_FORMAT.name
            MatchFormat.entries.find { it.name == formatName } ?: DEFAULT_MATCH_FORMAT
        }

    val ttsEnabledFlow: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[TTS_ENABLED] ?: DEFAULT_TTS_ENABLED
        }

    val announcerVoiceFlow: Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[ANNOUNCER_VOICE] ?: DEFAULT_ANNOUNCER_VOICE
        }

    val matchStateFlow: Flow<MatchState?> =
        context.dataStore.data.map { preferences ->
            preferences[MATCH_STATE_JSON]?.let { json ->
                try {
                    matchStateFromJsonString(json)
                } catch (_: Exception) {
                    null
                }
            }
        }

    suspend fun saveMatchState(state: MatchState) {
        context.dataStore.edit { preferences ->
            preferences[MATCH_STATE_JSON] = state.toJsonString()
        }
    }

    suspend fun updateKeycode(keycode: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEYCODE] = keycode
        }
    }

    suspend fun updateDoubleClickLatency(latency: Long) {
        context.dataStore.edit { preferences ->
            preferences[DOUBLE_CLICK_LATENCY] = latency
        }
    }

    suspend fun updateLongPressLatency(latency: Long) {
        context.dataStore.edit { preferences ->
            preferences[LONG_PRESS_LATENCY] = latency
        }
    }

    suspend fun updateUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME] = name
        }
    }

    suspend fun updateOpponentName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[OPPONENT_NAME] = name
        }
    }

    suspend fun updateInitialServerIsUser(isUser: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[INITIAL_SERVER_IS_USER] = isUser
        }
    }

    suspend fun updateHasSeenHelp(hasSeen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAS_SEEN_HELP] = hasSeen
        }
    }

    suspend fun updateAppTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[APP_THEME] = theme.name
        }
    }

    suspend fun updateSport(sport: Sport) {
        context.dataStore.edit { preferences ->
            preferences[SPORT] = sport.name
            // Reset format to sport's default when sport changes
            val defaultFormat = MatchFormat.entries.first { it.sport == sport }
            preferences[MATCH_FORMAT] = defaultFormat.name
        }
    }

    suspend fun updateMatchFormat(format: MatchFormat) {
        context.dataStore.edit { preferences ->
            preferences[MATCH_FORMAT] = format.name
        }
    }

    suspend fun updateTtsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TTS_ENABLED] = enabled
        }
    }

    suspend fun updateAnnouncerVoice(voice: String) {
        context.dataStore.edit { preferences ->
            preferences[ANNOUNCER_VOICE] = voice
        }
    }
}

enum class AppTheme(val displayName: String, val aliasName: String) {
    GRAND_SLAM("The Grand Slam (Yellow & White)", ".MainActivityGrandSlam"),
    MIAMI_NIGHT("Miami Night (Cyan & Magenta)", ".MainActivityMiamiNight"),
    COLORBLIND_SAFE("Colorblind Safe (Orange & Blue)", ".MainActivityColorblindSafe"),
    SKY_BLUE("Sky Blue (Blue & White)", ".MainActivitySkyBlue"),
}
