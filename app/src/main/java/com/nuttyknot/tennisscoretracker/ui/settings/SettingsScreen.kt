package com.nuttyknot.tennisscoretracker.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nuttyknot.tennisscoretracker.AppTheme
import com.nuttyknot.tennisscoretracker.SettingsManager
import kotlinx.coroutines.launch

@Suppress("FunctionName")
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    onNavigateBack: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val currentKeycode by settingsManager.keycodeFlow.collectAsState(
        initial = SettingsManager.DEFAULT_KEYCODE,
    )
    val currentDoubleClick by settingsManager.doubleClickLatencyFlow.collectAsState(
        initial = SettingsManager.DEFAULT_DOUBLE_CLICK_LATENCY,
    )
    val currentLongPress by settingsManager.longPressLatencyFlow.collectAsState(
        initial = SettingsManager.DEFAULT_LONG_PRESS_LATENCY,
    )
    val userName by settingsManager.userNameFlow.collectAsState(
        initial = SettingsManager.DEFAULT_USER_NAME,
    )
    val opponentName by settingsManager.opponentNameFlow.collectAsState(
        initial = SettingsManager.DEFAULT_OPPONENT_NAME,
    )
    val initialServerIsUser by settingsManager.initialServerIsUserFlow.collectAsState(
        initial = SettingsManager.DEFAULT_INITIAL_SERVER_IS_USER,
    )
    val appTheme by settingsManager.appThemeFlow.collectAsState(
        initial = SettingsManager.DEFAULT_APP_THEME,
    )

    val (playerData, appData) =
        buildSettingsData(
            settingsManager = settingsManager,
            coroutineScope = coroutineScope,
            state =
                SettingsState(
                    userName = userName,
                    opponentName = opponentName,
                    initialServerIsUser = initialServerIsUser,
                    currentKeycode = currentKeycode,
                    currentDoubleClick = currentDoubleClick,
                    currentLongPress = currentLongPress,
                    appTheme = appTheme,
                ),
        )

    Scaffold(
        topBar = { SettingsTopBar(onNavigateBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        SettingsLayout(
            paddingValues = paddingValues,
            playerData = playerData,
            appData = appData,
        )
    }
}

private data class SettingsState(
    val userName: String,
    val opponentName: String,
    val initialServerIsUser: Boolean,
    val currentKeycode: Int,
    val currentDoubleClick: Long,
    val currentLongPress: Long,
    val appTheme: AppTheme,
)

private fun buildSettingsData(
    settingsManager: SettingsManager,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    state: SettingsState,
): Pair<PlayerSettingsData, AppSettingsData> {
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
            currentDoubleClick = state.currentDoubleClick,
            currentLongPress = state.currentLongPress,
            currentTheme = state.appTheme,
            onKeycodeChange = { code ->
                coroutineScope.launch { settingsManager.updateKeycode(code) }
            },
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
            onThemeChange = { theme ->
                coroutineScope.launch { settingsManager.updateAppTheme(theme) }
            },
        )
    return playerData to appData
}

@Suppress("FunctionName")
@Composable
private fun SettingsLayout(
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    playerData: PlayerSettingsData,
    appData: AppSettingsData,
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
        PlayerSettings(playerData)
        AppSettings(appData)
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "made with ♥ by NuttyKnot",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}
