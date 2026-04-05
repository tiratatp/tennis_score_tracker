package com.nuttyknot.tennisscoretracker.wear.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.AlertDialogDefaults
import androidx.wear.compose.material3.Text
import com.nuttyknot.tennisscoretracker.shared.R

@Suppress("FunctionName")
@Composable
internal fun EndMatchDialog(
    show: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val view = LocalView.current
    AlertDialog(
        visible = show,
        onDismissRequest = onDismiss,
        confirmButton = {
            AlertDialogDefaults.ConfirmButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    onConfirm()
                },
            )
        },
        title = { Text(stringResource(R.string.end_match_confirm)) },
        dismissButton = {
            AlertDialogDefaults.DismissButton(onClick = onDismiss)
        },
    )
}

/** Renders dialog content in a plain layout — for Paparazzi screenshot tests. */
@Suppress("FunctionName")
@Composable
internal fun EndMatchDialogContent() {
    val scale = screenScale()
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.end_match_confirm),
            fontSize = scoreFontSize(scale),
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}
