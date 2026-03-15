package com.nuttyknot.tennisscoretracker.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType

@Suppress("FunctionName")
@Composable
fun LatencySettings(data: LatencySettingsData) {
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

data class LatencySettingsData(
    val currentDoubleClick: Long,
    val currentLongPress: Long,
    val onDoubleClickChange: (String) -> Unit,
    val onLongPressChange: (String) -> Unit,
)
