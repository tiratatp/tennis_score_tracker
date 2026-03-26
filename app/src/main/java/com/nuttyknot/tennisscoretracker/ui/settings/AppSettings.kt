@file:Suppress("TooManyFunctions")

package com.nuttyknot.tennisscoretracker.ui.settings

import android.view.KeyEvent
import androidx.annotation.StringRes
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nuttyknot.tennisscoretracker.AppTheme
import com.nuttyknot.tennisscoretracker.MatchFormat
import com.nuttyknot.tennisscoretracker.Sport
import com.nuttyknot.tennisscoretracker.shared.R

@Composable
fun sportDisplayName(sport: Sport): String =
    when (sport) {
        Sport.TENNIS -> stringResource(R.string.sport_tennis)
        Sport.BADMINTON -> stringResource(R.string.sport_badminton)
        Sport.PICKLEBALL -> stringResource(R.string.sport_pickleball)
    }

@Composable
fun matchFormatDisplayName(format: MatchFormat): String =
    when (format) {
        MatchFormat.STANDARD -> stringResource(R.string.match_format_standard)
        MatchFormat.LEAGUE -> stringResource(R.string.match_format_league)
        MatchFormat.FAST -> stringResource(R.string.match_format_fast)
        MatchFormat.BWF_STANDARD -> stringResource(R.string.match_format_bwf_standard)
        MatchFormat.BWF_SHORT -> stringResource(R.string.match_format_bwf_short)
        MatchFormat.PB_RALLY_11 -> stringResource(R.string.match_format_pb_rally_11)
        MatchFormat.PB_RALLY_15 -> stringResource(R.string.match_format_pb_rally_15)
        MatchFormat.PB_RALLY_21 -> stringResource(R.string.match_format_pb_rally_21)
        MatchFormat.PB_SIDEOUT -> stringResource(R.string.match_format_pb_sideout)
    }

@Composable
fun appThemeDisplayName(theme: AppTheme): String =
    when (theme) {
        AppTheme.GRAND_SLAM -> stringResource(R.string.theme_grand_slam)
        AppTheme.MIAMI_NIGHT -> stringResource(R.string.theme_miami_night)
        AppTheme.COLORBLIND_SAFE -> stringResource(R.string.theme_colorblind_safe)
        AppTheme.SKY_BLUE -> stringResource(R.string.theme_sky_blue)
    }

@Suppress("FunctionName")
@Composable
fun AppSettings(data: AppSettingsData) {
    SportDropdown(
        currentSport = data.currentSport,
        onSportChange = data.onSportChange,
        enabled = !data.isSportLocked,
    )

    MatchFormatDropdown(
        currentFormat = data.currentMatchFormat,
        onFormatChange = data.onMatchFormatChange,
        enabled = !data.isMatchFormatLocked,
        sport = data.currentSport,
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

    SettingsToggle(
        label = stringResource(R.string.settings_score_announcements),
        checked = data.ttsEnabled,
        onCheckedChange = data.onTtsEnabledChange,
    )

    if (data.ttsEnabled && data.availableVoices.isNotEmpty()) {
        AnnouncerVoiceDropdown(
            currentVoice = data.announcerVoice,
            availableVoices = data.availableVoices,
            onVoiceChange = data.onAnnouncerVoiceChange,
        )
    }
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
            text = stringResource(R.string.settings_color_scheme),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = appThemeDisplayName(currentTheme),
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
                        text = { Text(appThemeDisplayName(theme)) },
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
            text = stringResource(R.string.settings_color_scheme_desc),
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
            stringResource(R.string.settings_keycode_detecting)
        } else {
            selectedOption?.let {
                "${stringResource(it.nameRes)} (${it.code})"
            } ?: "${KeyEvent.keyCodeToString(currentKeycode)} ($currentKeycode)"
        }

    Column {
        Text(
            text = stringResource(R.string.settings_target_keycode),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        KeycodeSelector(displayText, currentKeycode, isDetecting, onKeycodeChange)
        Spacer(modifier = Modifier.height(4.dp))
        KeycodeDetectButton(isDetecting, onDetect, onCancelDetect)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.settings_keycode_desc),
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
            Text(stringResource(R.string.cancel))
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
            Text(stringResource(R.string.settings_detect_button))
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
        var lastCategory = -1
        KEYCODE_OPTIONS.forEach { option ->
            if (option.categoryRes != lastCategory) {
                lastCategory = option.categoryRes
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(lastCategory),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                    onClick = {},
                    enabled = false,
                )
            }
            DropdownMenuItem(
                text = {
                    Text("${stringResource(option.nameRes)} (${option.code})")
                },
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
    @StringRes val nameRes: Int,
    val code: Int,
    @StringRes val categoryRes: Int,
)

val KEYCODE_OPTIONS =
    listOf(
        // Camera Shutter Buttons
        KeycodeOption(R.string.keycode_volume_up, KeyEvent.KEYCODE_VOLUME_UP, R.string.keycode_category_camera),
        KeycodeOption(R.string.keycode_enter, KeyEvent.KEYCODE_ENTER, R.string.keycode_category_camera),
        KeycodeOption(R.string.keycode_volume_down, KeyEvent.KEYCODE_VOLUME_DOWN, R.string.keycode_category_camera),
        // Media Remotes
        KeycodeOption(R.string.keycode_media_next, KeyEvent.KEYCODE_MEDIA_NEXT, R.string.keycode_category_media),
        KeycodeOption(
            R.string.keycode_media_previous,
            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            R.string.keycode_category_media,
        ),
        KeycodeOption(
            R.string.keycode_media_play_pause,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            R.string.keycode_category_media,
        ),
        KeycodeOption(R.string.keycode_media_stop, KeyEvent.KEYCODE_MEDIA_STOP, R.string.keycode_category_media),
        // Presentation Clickers
        KeycodeOption(R.string.keycode_page_up, KeyEvent.KEYCODE_PAGE_UP, R.string.keycode_category_presentation),
        KeycodeOption(R.string.keycode_page_down, KeyEvent.KEYCODE_PAGE_DOWN, R.string.keycode_category_presentation),
        KeycodeOption(R.string.keycode_dpad_left, KeyEvent.KEYCODE_DPAD_LEFT, R.string.keycode_category_presentation),
        KeycodeOption(
            R.string.keycode_dpad_right,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            R.string.keycode_category_presentation,
        ),
        KeycodeOption(R.string.keycode_space, KeyEvent.KEYCODE_SPACE, R.string.keycode_category_presentation),
        // VR / Mini Gamepads
        KeycodeOption(R.string.keycode_button_a, KeyEvent.KEYCODE_BUTTON_A, R.string.keycode_category_gamepad),
        KeycodeOption(R.string.keycode_button_b, KeyEvent.KEYCODE_BUTTON_B, R.string.keycode_category_gamepad),
        KeycodeOption(R.string.keycode_button_c, KeyEvent.KEYCODE_BUTTON_C, R.string.keycode_category_gamepad),
        KeycodeOption(R.string.keycode_escape, KeyEvent.KEYCODE_ESCAPE, R.string.keycode_category_gamepad),
    )

data class AppSettingsData(
    val currentKeycode: Int,
    val currentTheme: AppTheme,
    val currentSport: Sport = Sport.TENNIS,
    val isSportLocked: Boolean = false,
    val currentMatchFormat: MatchFormat,
    val isMatchFormatLocked: Boolean = false,
    val ttsEnabled: Boolean = true,
    val announcerVoice: String = "",
    val availableVoices: List<String> = emptyList(),
    val isDetectingKeycode: Boolean = false,
    val onKeycodeChange: (Int) -> Unit,
    val onThemeChange: (AppTheme) -> Unit,
    val onSportChange: (Sport) -> Unit = {},
    val onMatchFormatChange: (MatchFormat) -> Unit,
    val onTtsEnabledChange: (Boolean) -> Unit = {},
    val onAnnouncerVoiceChange: (String) -> Unit = {},
    val onDetectKeycode: () -> Unit = {},
    val onCancelDetectKeycode: () -> Unit = {},
)

@Suppress("FunctionName", "LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SportDropdown(
    currentSport: Sport,
    onSportChange: (Sport) -> Unit,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val helpText =
        if (enabled) {
            stringResource(R.string.settings_sport_help)
        } else {
            stringResource(R.string.settings_sport_locked)
        }

    Column {
        Text(
            text = stringResource(R.string.settings_sport),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = !expanded },
        ) {
            OutlinedTextField(
                value = sportDisplayName(currentSport),
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
                onDismissRequest = { expanded = false },
            ) {
                Sport.entries.forEach { sport ->
                    DropdownMenuItem(
                        text = { Text(sportDisplayName(sport)) },
                        onClick = {
                            onSportChange(sport)
                            expanded = false
                        },
                        colors =
                            MenuDefaults.itemColors(
                                textColor =
                                    if (sport == currentSport) {
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
            text = helpText,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchFormatDropdown(
    currentFormat: MatchFormat,
    onFormatChange: (MatchFormat) -> Unit,
    enabled: Boolean = true,
    sport: Sport = Sport.TENNIS,
) {
    var expanded by remember { mutableStateOf(false) }
    val helpText =
        if (enabled) {
            stringResource(R.string.settings_match_format_help)
        } else {
            stringResource(R.string.settings_match_format_locked)
        }

    Column {
        Text(
            text = stringResource(R.string.settings_match_format),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        MatchFormatMenuBox(currentFormat, onFormatChange, enabled, expanded, sport) { expanded = it }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = helpText,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Suppress("FunctionName", "LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatchFormatMenuBox(
    currentFormat: MatchFormat,
    onFormatChange: (MatchFormat) -> Unit,
    enabled: Boolean,
    expanded: Boolean,
    sport: Sport,
    onExpandedChange: (Boolean) -> Unit,
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) onExpandedChange(!expanded) },
    ) {
        OutlinedTextField(
            value = matchFormatDisplayName(currentFormat),
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
            MatchFormat.entries.filter { it.sport == sport }.forEach { format ->
                DropdownMenuItem(
                    text = { Text(matchFormatDisplayName(format)) },
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

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncerVoiceDropdown(
    currentVoice: String,
    availableVoices: List<String>,
    onVoiceChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val defaultLabel = stringResource(R.string.settings_voice_default)
    val displayValue = if (currentVoice.isEmpty()) defaultLabel else currentVoice

    Column {
        Text(
            text = stringResource(R.string.settings_announcer_voice),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = displayValue,
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
            VoiceMenuItems(expanded, currentVoice, availableVoices, defaultLabel, onVoiceChange) { expanded = false }
        }
    }
}

@Suppress("FunctionName", "LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownMenuBoxScope.VoiceMenuItems(
    expanded: Boolean,
    currentVoice: String,
    availableVoices: List<String>,
    defaultLabel: String,
    onVoiceChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val options = listOf("" to defaultLabel) + availableVoices.map { it to it }
    ExposedDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        options.forEach { (value, label) ->
            DropdownMenuItem(
                text = { Text(label) },
                onClick = {
                    onVoiceChange(value)
                    onDismiss()
                },
                colors =
                    MenuDefaults.itemColors(
                        textColor =
                            if (value == currentVoice) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                    ),
            )
        }
    }
}
