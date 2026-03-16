package com.nuttyknot.tennisscoretracker.ui.settings

import android.view.KeyEvent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nuttyknot.tennisscoretracker.AppTheme
import com.nuttyknot.tennisscoretracker.MatchFormat

@Suppress("FunctionName")
@Composable
fun AppSettings(data: AppSettingsData) {
    MatchFormatDropdown(
        currentFormat = data.currentMatchFormat,
        onFormatChange = data.onMatchFormatChange,
        enabled = !data.isMatchFormatLocked,
    )

    ThemeDropdown(
        currentTheme = data.currentTheme,
        onThemeChange = data.onThemeChange,
    )

    KeycodeDropdown(
        currentKeycode = data.currentKeycode,
        onKeycodeChange = data.onKeycodeChange,
        isDetecting = data.isDetectingKeycode,
        onDetect = data.onDetectKeycode,
        onCancelDetect = data.onCancelDetectKeycode,
    )
}

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeDropdown(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "Color Scheme",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = currentTheme.displayName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    ),
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                AppTheme.entries.forEach { theme ->
                    DropdownMenuItem(
                        text = { Text(theme.displayName) },
                        onClick = {
                            onThemeChange(theme)
                            expanded = false
                        },
                        colors =
                            MenuDefaults.itemColors(
                                textColor =
                                    if (theme == currentTheme) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                            ),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Choose a color scheme that suits your style or visibility needs.",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Suppress("FunctionName")
@Composable
fun KeycodeDropdown(
    currentKeycode: Int,
    onKeycodeChange: (Int) -> Unit,
    isDetecting: Boolean = false,
    onDetect: () -> Unit = {},
    onCancelDetect: () -> Unit = {},
) {
    val selectedOption = KEYCODE_OPTIONS.find { it.code == currentKeycode }
    val displayText =
        if (isDetecting) {
            "Press any button..."
        } else {
            selectedOption?.let { "${it.name} (${it.code})" }
                ?: "${KeyEvent.keyCodeToString(currentKeycode)} ($currentKeycode)"
        }

    Column {
        Text(
            text = "Target KeyCode",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        KeycodeSelector(displayText, currentKeycode, isDetecting, onKeycodeChange)
        Spacer(modifier = Modifier.height(4.dp))
        KeycodeDetectButton(isDetecting, onDetect, onCancelDetect)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Select from the list, or tap Detect to auto-detect your remote's button.",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KeycodeSelector(
    displayText: String,
    currentKeycode: Int,
    isDetecting: Boolean,
    onKeycodeChange: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val primary = MaterialTheme.colorScheme.primary
    val onBg = MaterialTheme.colorScheme.onBackground
    val textColor = if (isDetecting) primary else onBg
    val borderColor = if (isDetecting) primary else onBg.copy(alpha = 0.5f)

    ExposedDropdownMenuBox(
        expanded = expanded && !isDetecting,
        onExpandedChange = { if (!isDetecting) expanded = !expanded },
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                if (!isDetecting) ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = borderColor,
                ),
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        if (expanded && !isDetecting) {
            KeycodeMenu(
                currentKeycode = currentKeycode,
                onKeycodeChange = onKeycodeChange,
                onDismiss = { expanded = false },
            )
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun KeycodeDetectButton(
    isDetecting: Boolean,
    onDetect: () -> Unit,
    onCancelDetect: () -> Unit,
) {
    if (isDetecting) {
        OutlinedButton(
            onClick = onCancelDetect,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Cancel")
        }
    } else {
        Button(
            onClick = onDetect,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Detect Button")
        }
    }
}

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownMenuBoxScope.KeycodeMenu(
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

data class KeycodeOption(
    val name: String,
    val code: Int,
    val category: String,
)

val KEYCODE_OPTIONS =
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

data class AppSettingsData(
    val currentKeycode: Int,
    val currentTheme: AppTheme,
    val currentMatchFormat: MatchFormat,
    val isMatchFormatLocked: Boolean = false,
    val isDetectingKeycode: Boolean = false,
    val onKeycodeChange: (Int) -> Unit,
    val onThemeChange: (AppTheme) -> Unit,
    val onMatchFormatChange: (MatchFormat) -> Unit,
    val onDetectKeycode: () -> Unit = {},
    val onCancelDetectKeycode: () -> Unit = {},
)

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchFormatDropdown(
    currentFormat: MatchFormat,
    onFormatChange: (MatchFormat) -> Unit,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val helpText =
        if (enabled) {
            "Choose the match format. Changes apply on new match."
        } else {
            "Reset the current match to change format."
        }

    Column {
        Text(
            text = "Match Format",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        MatchFormatMenuBox(currentFormat, onFormatChange, enabled, expanded) { expanded = it }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = helpText,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatchFormatMenuBox(
    currentFormat: MatchFormat,
    onFormatChange: (MatchFormat) -> Unit,
    enabled: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) onExpandedChange(!expanded) },
    ) {
        OutlinedTextField(
            value = currentFormat.displayName,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    disabledTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    disabledBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                ),
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            MatchFormat.entries.forEach { format ->
                DropdownMenuItem(
                    text = { Text(format.displayName) },
                    onClick = {
                        onFormatChange(format)
                        onExpandedChange(false)
                    },
                    colors =
                        MenuDefaults.itemColors(
                            textColor =
                                if (format == currentFormat) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                        ),
                )
            }
        }
    }
}
