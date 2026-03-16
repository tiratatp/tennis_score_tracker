package com.nuttyknot.tennisscoretracker.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType

@Suppress("FunctionName")
@Composable
fun PlayerSettings(data: PlayerSettingsData) {
    SettingsItem(
        label = "Your Name",
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
        label = "You start serving",
        checked = data.initialServerIsUser,
        onCheckedChange = data.onInitialServerChange,
    )
}

data class PlayerSettingsData(
    val userName: String,
    val opponentName: String,
    val initialServerIsUser: Boolean,
    val onUserNameChange: (String) -> Unit,
    val onOpponentNameChange: (String) -> Unit,
    val onInitialServerChange: (Boolean) -> Unit,
)
