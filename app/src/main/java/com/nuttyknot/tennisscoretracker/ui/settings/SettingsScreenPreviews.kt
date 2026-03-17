package com.nuttyknot.tennisscoretracker.ui.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable

@Suppress("FunctionName")
@Composable
internal fun SettingsScreenPreview(
    playerData: PlayerSettingsData,
    appData: AppSettingsData,
    latencyData: LatencySettingsData,
) {
    Scaffold(
        topBar = { SettingsTopBar(onNavigateBack = {}) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        SettingsLayout(
            paddingValues = paddingValues,
            playerData = playerData,
            appData = appData,
            latencyData = latencyData,
        )
    }
}
