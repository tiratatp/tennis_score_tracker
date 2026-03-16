package com.nuttyknot.tennisscoretracker.wear.ui

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

@Suppress("FunctionName")
@Composable
fun WearTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme(),
        content = content,
    )
}
