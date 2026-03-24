package com.nuttyknot.tennisscoretracker.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.nuttyknot.tennisscoretracker.SettingsManager
import com.nuttyknot.tennisscoretracker.shared.R

private const val DOUBLE_CLICK_MIN = 100L
private const val DOUBLE_CLICK_MAX = 1000L
private const val LONG_PRESS_MIN = 300L
private const val LONG_PRESS_MAX = 3000L

@Suppress("FunctionName")
@Composable
fun LatencySettings(data: LatencySettingsData) {
    val context = LocalContext.current
    SettingsItem(
        label = stringResource(R.string.settings_double_click_latency),
        value = data.currentDoubleClick.toString(),
        onValueChange = data.onDoubleClickChange,
        description = stringResource(R.string.settings_double_click_desc, SettingsManager.DEFAULT_DOUBLE_CLICK_LATENCY),
        config =
            SettingsItemConfig(
                validate = { v ->
                    val num = v.toLongOrNull()
                    when {
                        v.isBlank() -> context.getString(R.string.validation_latency_required)
                        num == null -> context.getString(R.string.validation_must_be_number)
                        num < DOUBLE_CLICK_MIN -> context.getString(R.string.validation_min_ms, DOUBLE_CLICK_MIN)
                        num > DOUBLE_CLICK_MAX -> context.getString(R.string.validation_max_ms, DOUBLE_CLICK_MAX)
                        else -> null
                    }
                },
            ),
    )

    SettingsItem(
        label = stringResource(R.string.settings_long_press_latency),
        value = data.currentLongPress.toString(),
        onValueChange = data.onLongPressChange,
        description = stringResource(R.string.settings_long_press_desc, SettingsManager.DEFAULT_LONG_PRESS_LATENCY),
        config =
            SettingsItemConfig(
                keyboardType = KeyboardType.Number,
                validate = { v ->
                    val num = v.toLongOrNull()
                    when {
                        v.isBlank() -> context.getString(R.string.validation_latency_required)
                        num == null -> context.getString(R.string.validation_must_be_number)
                        num < LONG_PRESS_MIN -> context.getString(R.string.validation_min_ms, LONG_PRESS_MIN)
                        num > LONG_PRESS_MAX -> context.getString(R.string.validation_max_ms, LONG_PRESS_MAX)
                        num <= data.currentDoubleClick ->
                            context.getString(R.string.validation_greater_than_double_click, data.currentDoubleClick)
                        else -> null
                    }
                },
            ),
    )
}

data class LatencySettingsData(
    val currentDoubleClick: Long,
    val currentLongPress: Long,
    val onDoubleClickChange: (String) -> Unit,
    val onLongPressChange: (String) -> Unit,
)
