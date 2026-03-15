package com.nuttyknot.tennisscoretracker.ui

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nuttyknot.tennisscoretracker.SettingsManager
import com.nuttyknot.tennisscoretracker.ui.theme.Black
import com.nuttyknot.tennisscoretracker.ui.theme.White
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val currentKeycode by settingsManager.keycodeFlow.collectAsState(initial = SettingsManager.DEFAULT_KEYCODE)
    val currentDoubleClick by settingsManager.doubleClickLatencyFlow.collectAsState(initial = SettingsManager.DEFAULT_DOUBLE_CLICK_LATENCY)
    val currentLongPress by settingsManager.longPressLatencyFlow.collectAsState(initial = SettingsManager.DEFAULT_LONG_PRESS_LATENCY)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Black)
            )
        },
        containerColor = Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsItem(
                label = "Target KeyCode",
                value = currentKeycode.toString(),
                onValueChange = { newValue ->
                    newValue.toIntOrNull()?.let {
                        coroutineScope.launch { settingsManager.updateKeycode(it) }
                    }
                },
                description = "Default VOLUME_UP is ${KeyEvent.KEYCODE_VOLUME_UP}"
            )

            SettingsItem(
                label = "Double Click Latency (ms)",
                value = currentDoubleClick.toString(),
                onValueChange = { newValue ->
                    newValue.toLongOrNull()?.let {
                        coroutineScope.launch { settingsManager.updateDoubleClickLatency(it) }
                    }
                },
                description = "Max time window for a double click. Default is 300."
            )

            SettingsItem(
                label = "Long Press Latency (ms)",
                value = currentLongPress.toString(),
                onValueChange = { newValue ->
                    newValue.toLongOrNull()?.let {
                        coroutineScope.launch { settingsManager.updateLongPressLatency(it) }
                    }
                },
                description = "Min time window to trigger long press. Default is 1000."
            )
        }
    }
}

@Composable
fun SettingsItem(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    description: String
) {
    Column {
        Text(text = label, color = White, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = White,
                unfocusedTextColor = White,
                focusedBorderColor = White,
                unfocusedBorderColor = White,
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = description, color = White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
    }
}
