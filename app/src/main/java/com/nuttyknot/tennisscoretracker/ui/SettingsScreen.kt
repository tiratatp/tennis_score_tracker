package com.nuttyknot.tennisscoretracker.ui

import android.view.KeyEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nuttyknot.tennisscoretracker.SettingsManager
import com.nuttyknot.tennisscoretracker.ui.theme.Black
import com.nuttyknot.tennisscoretracker.ui.theme.White
import kotlinx.coroutines.launch

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
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
                ),
        )

    Scaffold(
        topBar = { SettingsTopBar(onNavigateBack) },
        containerColor = Black,
    ) { paddingValues ->
        SettingsLayout(
            paddingValues = paddingValues,
            playerData = playerData,
            appData = appData,
        )
    }
}

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = { Text("Settings", color = White) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = @Suppress("DEPRECATION") Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = White,
                )
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = Black,
            ),
    )
}

private data class SettingsState(
    val userName: String,
    val opponentName: String,
    val initialServerIsUser: Boolean,
    val currentKeycode: Int,
    val currentDoubleClick: Long,
    val currentLongPress: Long,
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
            onKeycodeChange = { v ->
                v.toIntOrNull()?.let {
                    coroutineScope.launch { settingsManager.updateKeycode(it) }
                }
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
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PlayerSettings(playerData)
        AppSettings(appData)
    }
}

private data class PlayerSettingsData(
    val userName: String,
    val opponentName: String,
    val initialServerIsUser: Boolean,
    val onUserNameChange: (String) -> Unit,
    val onOpponentNameChange: (String) -> Unit,
    val onInitialServerChange: (Boolean) -> Unit,
)

private data class AppSettingsData(
    val currentKeycode: Int,
    val currentDoubleClick: Long,
    val currentLongPress: Long,
    val onKeycodeChange: (String) -> Unit,
    val onDoubleClickChange: (String) -> Unit,
    val onLongPressChange: (String) -> Unit,
)

@Suppress("FunctionName")
@Composable
private fun PlayerSettings(data: PlayerSettingsData) {
    SettingsItem(
        label = "User Name",
        value = data.userName,
        onValueChange = data.onUserNameChange,
        description = "Your name for announcements.",
        keyboardType = KeyboardType.Text,
    )

    SettingsItem(
        label = "Opponent Name",
        value = data.opponentName,
        onValueChange = data.onOpponentNameChange,
        description = "Opponent's name for announcements.",
        keyboardType = KeyboardType.Text,
    )

    SettingsToggle(
        label = "User starts serving",
        checked = data.initialServerIsUser,
        onCheckedChange = data.onInitialServerChange,
    )
}

@Suppress("FunctionName")
@Composable
private fun AppSettings(data: AppSettingsData) {
    SettingsItem(
        label = "Target KeyCode",
        value = data.currentKeycode.toString(),
        onValueChange = data.onKeycodeChange,
        description = "Default VOLUME_UP is ${KeyEvent.KEYCODE_VOLUME_UP}",
    )

    SettingsItem(
        label = "Double Click Latency (ms)",
        value = data.currentDoubleClick.toString(),
        onValueChange = data.onDoubleClickChange,
        description = "Max time window for a double click. Default is 300.",
    )

    SettingsItem(
        label = "Long Press Latency (ms)",
        value = data.currentLongPress.toString(),
        onValueChange = data.onLongPressChange,
        description = "Min time window to trigger long press. Default is 1000.",
        keyboardType = KeyboardType.Number,
    )
}

@Suppress("FunctionName")
@Composable
fun SettingsItem(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    description: String,
    keyboardType: KeyboardType = KeyboardType.Number,
) {
    Column {
        Text(text = label, color = White, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    focusedBorderColor = White,
                    unfocusedBorderColor = White,
                    cursorColor = White,
                ),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            color = White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Suppress("FunctionName")
@Composable
fun SettingsToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, color = White, style = MaterialTheme.typography.titleMedium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = White.copy(alpha = 0.5f),
                    uncheckedTrackColor = Black,
                ),
        )
    }
}
