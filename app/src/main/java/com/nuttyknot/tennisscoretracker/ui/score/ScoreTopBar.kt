package com.nuttyknot.tennisscoretracker.ui.score

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.nuttyknot.tennisscoretracker.ui.theme.Black
import com.nuttyknot.tennisscoretracker.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("FunctionName")
@Composable
fun ScoreTopBar(
    onNavigateToHelp: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onResetClick: () -> Unit,
) {
    TopAppBar(
        title = { Text("Tennis Score Tracker", color = White) },
        actions = {
            IconButton(onClick = onNavigateToHelp) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Help",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onResetClick) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}

@Suppress("FunctionName")
@Composable
fun ResetConfirmationDialog(
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Reset Score?") },
            text = { Text("Are you sure you want to reset the current match score?") },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("RESET", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("CANCEL", color = MaterialTheme.colorScheme.onBackground)
                }
            },
            containerColor = Black,
            titleContentColor = White,
            textContentColor = White,
        )
    }
}
