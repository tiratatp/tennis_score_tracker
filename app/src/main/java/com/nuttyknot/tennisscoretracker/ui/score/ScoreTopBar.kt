package com.nuttyknot.tennisscoretracker.ui.score

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.nuttyknot.tennisscoretracker.R
import com.nuttyknot.tennisscoretracker.shared.R as SharedR

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("FunctionName")
@Composable
fun ScoreTopBar(
    onNavigateToHelp: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onResetClick: () -> Unit,
) {
    TopAppBar(
        title = { Text(stringResource(SharedR.string.app_name), color = MaterialTheme.colorScheme.onBackground) },
        actions = {
            IconButton(onClick = onNavigateToHelp) {
                Icon(
                    painter = painterResource(R.drawable.ic_info_24),
                    contentDescription = stringResource(SharedR.string.help),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onResetClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_refresh_24),
                    contentDescription = stringResource(SharedR.string.reset),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings_24),
                    contentDescription = stringResource(SharedR.string.settings),
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
            title = { Text(stringResource(SharedR.string.reset_score_title)) },
            text = { Text(stringResource(SharedR.string.reset_score_message)) },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(stringResource(SharedR.string.reset_action), color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(SharedR.string.cancel_action), color = MaterialTheme.colorScheme.onBackground)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
        )
    }
}
