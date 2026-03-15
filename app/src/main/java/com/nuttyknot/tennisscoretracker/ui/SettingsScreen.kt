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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MenuDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nuttyknot.tennisscoretracker.SettingsManager
import com.nuttyknot.tennisscoretracker.ui.theme.Black
import com.nuttyknot.tennisscoretracker.ui.theme.White
import kotlinx.coroutines.launch

private data class KeycodeOption(
    val name: String,
    val code: Int,
    val category: String,
)

private val KEYCODE_OPTIONS =
    listOf(
        // Camera Shutter Buttons
        KeycodeOption("Volume Up", KeyEvent.KEYCODE_VOLUME_UP, "Camera Shutter Buttons"),
        KeycodeOption("Enter", KeyEvent.KEYCODE_ENTER, "Camera Shutter Buttons"),
        KeycodeOption("Volume Down", KeyEvent.KEYCODE_VOLUME_DOWN, "Camera Shutter Buttons"),
        // Media Remotes
        KeycodeOption("Media Next", KeyEvent.KEYCODE_MEDIA_NEXT, "Media Remotes"),
        KeycodeOption("Media Previous", KeyEvent.KEYCODE_MEDIA_PREVIOUS, "Media Remotes"),
        KeycodeOption("Media Play/Pause", KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, "Media Remotes"),
        KeycodeOption("Media Stop", KeyEvent.KEYCODE_MEDIA_STOP, "Media Remotes"),
        // Presentation Clickers
        KeycodeOption("Page Up", KeyEvent.KEYCODE_PAGE_UP, "Presentation Clickers"),
        KeycodeOption("Page Down", KeyEvent.KEYCODE_PAGE_DOWN, "Presentation Clickers"),
        KeycodeOption("D-Pad Left", KeyEvent.KEYCODE_DPAD_LEFT, "Presentation Clickers"),
        KeycodeOption("D-Pad Right", KeyEvent.KEYCODE_DPAD_RIGHT, "Presentation Clickers"),
        KeycodeOption("Space", KeyEvent.KEYCODE_SPACE, "Presentation Clickers"),
        // VR / Mini Gamepads
        KeycodeOption("Button A", KeyEvent.KEYCODE_BUTTON_A, "VR / Mini Gamepads"),
        KeycodeOption("Button B", KeyEvent.KEYCODE_BUTTON_B, "VR / Mini Gamepads"),
        KeycodeOption("Button C", KeyEvent.KEYCODE_BUTTON_C, "VR / Mini Gamepads"),
        KeycodeOption("Escape", KeyEvent.KEYCODE_ESCAPE, "VR / Mini Gamepads"),
    )

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
    val onKeycodeChange: (Int) -> Unit,
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
        config =
            SettingsItemConfig(
                keyboardType = KeyboardType.Text,
                validate = { v ->
                    when {
                        v.length > 30 -> "Name must be 30 characters or less"
                        else -> null
                    }
                },
            ),
    )

    SettingsItem(
        label = "Opponent Name",
        value = data.opponentName,
        onValueChange = data.onOpponentNameChange,
        description = "Opponent's name for announcements.",
        config =
            SettingsItemConfig(
                keyboardType = KeyboardType.Text,
                validate = { v ->
                    when {
                        v.length > 30 -> "Name must be 30 characters or less"
                        else -> null
                    }
                },
            ),
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
    KeycodeDropdown(
        currentKeycode = data.currentKeycode,
        onKeycodeChange = data.onKeycodeChange,
    )

    SettingsItem(
        label = "Double Click Latency (ms)",
        value = data.currentDoubleClick.toString(),
        onValueChange = data.onDoubleClickChange,
        description = "Max time window for a double click. Default is 300.",
        config =
            SettingsItemConfig(
                validate = { v ->
                    val num = v.toLongOrNull()
                    when {
                        v.isBlank() -> "Latency is required"
                        num == null -> "Must be a valid number"
                        num < 100 -> "Must be at least 100 ms"
                        num > 1000 -> "Must be 1000 ms or less"
                        else -> null
                    }
                },
            ),
    )

    SettingsItem(
        label = "Long Press Latency (ms)",
        value = data.currentLongPress.toString(),
        onValueChange = data.onLongPressChange,
        description = "Min time window to trigger long press. Default is 1000.",
        config =
            SettingsItemConfig(
                keyboardType = KeyboardType.Number,
                validate = { v ->
                    val num = v.toLongOrNull()
                    when {
                        v.isBlank() -> "Latency is required"
                        num == null -> "Must be a valid number"
                        num < 300 -> "Must be at least 300 ms"
                        num > 3000 -> "Must be 3000 ms or less"
                        num <= data.currentDoubleClick ->
                            "Must be greater than double click latency (${data.currentDoubleClick} ms)"
                        else -> null
                    }
                },
            ),
    )
}

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KeycodeDropdown(
    currentKeycode: Int,
    onKeycodeChange: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOption = KEYCODE_OPTIONS.find { it.code == currentKeycode }
    val displayText = selectedOption?.let { "${it.name} (${it.code})" } ?: "Unknown ($currentKeycode)"

    Column {
        Text(text = "Target KeyCode", color = White, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = displayText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        focusedBorderColor = White,
                        unfocusedBorderColor = White,
                    ),
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            )
            if (expanded) {
                KeycodeMenu(
                    currentKeycode = currentKeycode,
                    onKeycodeChange = onKeycodeChange,
                    onDismiss = { expanded = false },
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Select the button your Bluetooth remote sends.",
            color = White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownMenuBoxScope.KeycodeMenu(
    currentKeycode: Int,
    onKeycodeChange: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    ExposedDropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
    ) {
        var lastCategory = ""
        KEYCODE_OPTIONS.forEach { option ->
            if (option.category != lastCategory) {
                lastCategory = option.category
                DropdownMenuItem(
                    text = {
                        Text(
                            text = lastCategory,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                    onClick = {},
                    enabled = false,
                )
            }
            DropdownMenuItem(
                text = { Text("${option.name} (${option.code})") },
                onClick = {
                    onKeycodeChange(option.code)
                    onDismiss()
                },
                colors =
                    MenuDefaults.itemColors(
                        textColor =
                            if (option.code == currentKeycode) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                    ),
            )
        }
    }
}

@Suppress("FunctionName")
@Composable
fun SettingsItem(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    description: String,
    config: SettingsItemConfig = SettingsItemConfig(),
) {
    var localValue by remember(value) { mutableStateOf(value) }
    val errorMessage = config.validate?.invoke(localValue)
    val hasError = errorMessage != null
    Column {
        Text(text = label, color = White, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = localValue,
            onValueChange = { newValue ->
                localValue = newValue
                val isValid = config.validate?.invoke(newValue) == null
                if (isValid) {
                    onValueChange(newValue)
                }
            },
            isError = hasError,
            keyboardOptions = KeyboardOptions(keyboardType = config.keyboardType),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    focusedBorderColor = if (hasError) Color.Red else White,
                    unfocusedBorderColor = if (hasError) Color.Red else White,
                    cursorColor = White,
                    errorBorderColor = Color.Red,
                ),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(2.dp))
        if (hasError) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(2.dp))
        }
        Text(
            text = description,
            color = White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

data class SettingsItemConfig(
    val keyboardType: KeyboardType = KeyboardType.Number,
    val validate: ((String) -> String?)? = null,
)

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
