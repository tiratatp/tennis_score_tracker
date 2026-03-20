package com.nuttyknot.tennisscoretracker.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nuttyknot.tennisscoretracker.AnnouncerVoice
import com.nuttyknot.tennisscoretracker.AppTheme
import com.nuttyknot.tennisscoretracker.MatchFormat
import com.nuttyknot.tennisscoretracker.ScoreModel
import com.nuttyknot.tennisscoretracker.SettingsManager
import com.nuttyknot.tennisscoretracker.ui.AppFooter
import kotlinx.coroutines.launch

@Suppress("FunctionName")
@Composable
fun SettingsScreen(
    scoreModel: ScoreModel,
    settingsManager: SettingsManager,
    onNavigateBack: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val state = collectSettingsState(scoreModel, settingsManager)

    LaunchedEffect(Unit) {
        settingsManager.detectedKeycode.collect { keycode ->
            settingsManager.updateKeycode(keycode)
            settingsManager.stopKeycodeDetection()
        }
    }

    val (playerData, appData, latencyData) =
        buildSettingsData(
            settingsManager = settingsManager,
            coroutineScope = coroutineScope,
            state = state,
        )

    Scaffold(
        topBar = { SettingsTopBar(onNavigateBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        SettingsLayout(
            paddingValues = paddingValues,
            playerData = playerData,
            appData = appData,
            latencyData = latencyData,
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun collectSettingsState(
    scoreModel: ScoreModel,
    settingsManager: SettingsManager,
): SettingsState {
    val matchState by scoreModel.matchState.collectAsState()
    val currentKeycode by settingsManager.keycodeFlow.collectAsState(initial = SettingsManager.DEFAULT_KEYCODE)
    val currentDoubleClick by settingsManager.doubleClickLatencyFlow.collectAsState(
        initial = SettingsManager.DEFAULT_DOUBLE_CLICK_LATENCY,
    )
    val currentLongPress by settingsManager.longPressLatencyFlow.collectAsState(
        initial = SettingsManager.DEFAULT_LONG_PRESS_LATENCY,
    )
    val userName by settingsManager.userNameFlow.collectAsState(initial = SettingsManager.DEFAULT_USER_NAME)
    val opponentName by settingsManager.opponentNameFlow.collectAsState(initial = SettingsManager.DEFAULT_OPPONENT_NAME)
    val initialServerIsUser by settingsManager.initialServerIsUserFlow.collectAsState(
        initial = SettingsManager.DEFAULT_INITIAL_SERVER_IS_USER,
    )
    val appTheme by settingsManager.appThemeFlow.collectAsState(initial = SettingsManager.DEFAULT_APP_THEME)
    val matchFormat by settingsManager.matchFormatFlow.collectAsState(initial = SettingsManager.DEFAULT_MATCH_FORMAT)
    val ttsEnabled by settingsManager.ttsEnabledFlow.collectAsState(initial = SettingsManager.DEFAULT_TTS_ENABLED)
    val announcerVoice by settingsManager.announcerVoiceFlow.collectAsState(
        initial = SettingsManager.DEFAULT_ANNOUNCER_VOICE,
    )
    val isDetectingKeycode by settingsManager.isDetectingKeycode.collectAsState()

    return SettingsState(
        isMatchInProgress = !matchState.isScoreZero,
        userName = userName,
        opponentName = opponentName,
        initialServerIsUser = initialServerIsUser,
        currentKeycode = currentKeycode,
        isDetectingKeycode = isDetectingKeycode,
        currentDoubleClick = currentDoubleClick,
        currentLongPress = currentLongPress,
        appTheme = appTheme,
        matchFormat = matchFormat,
        ttsEnabled = ttsEnabled,
        announcerVoice = announcerVoice,
    )
}

private data class SettingsState(
    val isMatchInProgress: Boolean,
    val userName: String,
    val opponentName: String,
    val initialServerIsUser: Boolean,
    val currentKeycode: Int,
    val isDetectingKeycode: Boolean,
    val currentDoubleClick: Long,
    val currentLongPress: Long,
    val appTheme: AppTheme,
    val matchFormat: MatchFormat,
    val ttsEnabled: Boolean,
    val announcerVoice: AnnouncerVoice,
)

@Suppress("LongMethod")
private fun buildSettingsData(
    settingsManager: SettingsManager,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    state: SettingsState,
): Triple<PlayerSettingsData, AppSettingsData, LatencySettingsData> {
    val playerData =
        PlayerSettingsData(
            userName = state.userName,
            opponentName = state.opponentName,
            initialServerIsUser = state.initialServerIsUser,
            onUserNameChange = { v ->
                coroutineScope.launch { settingsManager.updateUserName(v) }
            },
            onOpponentNameChange = { v ->
                coroutineScope.launch { settingsManager.updateOpponentName(v) }
            },
            onInitialServerChange = { v ->
                coroutineScope.launch { settingsManager.updateInitialServerIsUser(v) }
            },
        )
    val appData =
        AppSettingsData(
            currentKeycode = state.currentKeycode,
            currentTheme = state.appTheme,
            currentMatchFormat = state.matchFormat,
            isMatchFormatLocked = state.isMatchInProgress,
            ttsEnabled = state.ttsEnabled,
            announcerVoice = state.announcerVoice,
            isDetectingKeycode = state.isDetectingKeycode,
            onKeycodeChange = { code ->
                coroutineScope.launch { settingsManager.updateKeycode(code) }
            },
            onThemeChange = { theme ->
                coroutineScope.launch { settingsManager.updateAppTheme(theme) }
            },
            onMatchFormatChange = { format ->
                coroutineScope.launch { settingsManager.updateMatchFormat(format) }
            },
            onTtsEnabledChange = { enabled ->
                coroutineScope.launch { settingsManager.updateTtsEnabled(enabled) }
            },
            onAnnouncerVoiceChange = { voice ->
                coroutineScope.launch { settingsManager.updateAnnouncerVoice(voice) }
            },
            onDetectKeycode = { settingsManager.startKeycodeDetection() },
            onCancelDetectKeycode = { settingsManager.stopKeycodeDetection() },
        )
    val latencyData =
        LatencySettingsData(
            currentDoubleClick = state.currentDoubleClick,
            currentLongPress = state.currentLongPress,
            onDoubleClickChange = { v ->
                v.toLongOrNull()?.let {
                    coroutineScope.launch { settingsManager.updateDoubleClickLatency(it) }
                }
            },
            onLongPressChange = { v ->
                v.toLongOrNull()?.let {
                    coroutineScope.launch { settingsManager.updateLongPressLatency(it) }
                }
            },
        )
    return Triple(playerData, appData, latencyData)
}

@Suppress("FunctionName")
@Composable
internal fun SettingsLayout(
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    playerData: PlayerSettingsData,
    appData: AppSettingsData,
    latencyData: LatencySettingsData,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsSection(title = "Players") {
            PlayerSettings(playerData)
        }
        SettingsSection(title = "App") {
            AppSettings(appData)
        }
        CollapsibleSettingsSection(title = "Advanced") {
            LatencySettings(latencyData)
        }
        AppFooter()
    }
}
