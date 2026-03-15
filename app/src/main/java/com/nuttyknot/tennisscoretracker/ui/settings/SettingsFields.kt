package com.nuttyknot.tennisscoretracker.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

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
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
        )
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
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    focusedBorderColor =
                        if (hasError) Color.Red else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor =
                        if (hasError) {
                            Color.Red
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        },
                    cursorColor = MaterialTheme.colorScheme.primary,
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
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
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
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    uncheckedTrackColor = MaterialTheme.colorScheme.background,
                ),
        )
    }
}

data class SettingsItemConfig(
    val keyboardType: KeyboardType = KeyboardType.Number,
    val validate: ((String) -> String?)? = null,
)
