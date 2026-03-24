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
import androidx.compose.ui.res.stringResource
import com.nuttyknot.tennisscoretracker.shared.R

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("FunctionName")
@Composable
fun ScoreTopBar(
    onNavigateToHelp: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onResetClick: () -> Unit,
) {
    TopAppBar(
        title = { Text(stringResource(R.string.app_name), color = MaterialTheme.colorScheme.onBackground) },
        actions = {
            IconButton(onClick = onNavigateToHelp) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(R.string.help),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onResetClick) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.reset),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings),
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
            title = { Text(stringResource(R.string.reset_score_title)) },
            text = { Text(stringResource(R.string.reset_score_message)) },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(stringResource(R.string.reset_action), color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel_action), color = MaterialTheme.colorScheme.onBackground)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
        )
    }
}
