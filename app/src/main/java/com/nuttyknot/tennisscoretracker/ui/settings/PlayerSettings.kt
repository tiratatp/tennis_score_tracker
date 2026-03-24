package com.nuttyknot.tennisscoretracker.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.nuttyknot.tennisscoretracker.shared.R

private const val MAX_NAME_LENGTH = 30

@Suppress("FunctionName")
@Composable
fun PlayerSettings(data: PlayerSettingsData) {
    val context = LocalContext.current
    SettingsItem(
        label = stringResource(R.string.settings_your_name),
        value = data.userName,
        onValueChange = data.onUserNameChange,
        description = stringResource(R.string.settings_your_name_desc),
        config =
            SettingsItemConfig(
                keyboardType = KeyboardType.Text,
                validate = { v ->
                    when {
                        v.length > MAX_NAME_LENGTH ->
                            context.getString(R.string.validation_name_too_long, MAX_NAME_LENGTH)
                        else -> null
                    }
                },
            ),
    )

    SettingsItem(
        label = stringResource(R.string.settings_opponent_name),
        value = data.opponentName,
        onValueChange = data.onOpponentNameChange,
        description = stringResource(R.string.settings_opponent_name_desc),
        config =
            SettingsItemConfig(
                keyboardType = KeyboardType.Text,
                validate = { v ->
                    when {
                        v.length > MAX_NAME_LENGTH ->
                            context.getString(R.string.validation_name_too_long, MAX_NAME_LENGTH)
                        else -> null
                    }
                },
            ),
    )

    SettingsToggle(
        label = stringResource(R.string.settings_you_start_serving),
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
