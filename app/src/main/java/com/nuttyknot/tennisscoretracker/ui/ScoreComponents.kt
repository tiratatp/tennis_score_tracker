package com.nuttyknot.tennisscoretracker.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuttyknot.tennisscoretracker.R
import com.nuttyknot.tennisscoretracker.TennisMatchState
import com.nuttyknot.tennisscoretracker.ui.theme.Black
import com.nuttyknot.tennisscoretracker.ui.theme.White
import com.nuttyknot.tennisscoretracker.ui.theme.Yellow

object ScoreScreenConstants {
    private val fontProvider =
        GoogleFont.Provider(
            providerAuthority = "com.google.android.gms.fonts",
            providerPackage = "com.google.android.gms",
            certificates = R.array.com_google_android_gms_fonts_certs,
        )

    private val fontName = GoogleFont("JetBrains Mono")

    val JetBrainsMonoFamily =
        FontFamily(
            Font(googleFont = fontName, fontProvider = fontProvider, weight = FontWeight.ExtraBold),
            Font(googleFont = fontName, fontProvider = fontProvider, weight = FontWeight.Black),
        )

    const val LANDSCAPE_TEXT_SIZE_RATIO = 1.5
    const val PORTRAIT_TEXT_SIZE_RATIO = 3.0
    val MIDDLE_COLUMN_WIDTH = 180.dp
    val STATUS_TEXT_SIZE_LANDSCAPE = 22.sp
    val STATUS_TEXT_SIZE_PORTRAIT = 28.sp
    val NAME_TEXT_SIZE = 24.sp
    val INDICATOR_SIZE = 24.sp
    const val NAME_ALPHA = 0.7f
    val SERVING_DOT_SPACING = 16.dp
    val VERTICAL_SPACING_LARGE = 32.dp
    val VERTICAL_SPACING_MEDIUM = 16.dp
    const val LANDSCAPE_MAX_SAFE_SIZE_FACTOR = 2.5f
}

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
                    tint = Yellow,
                )
            }
            IconButton(onClick = onResetClick) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = Yellow,
                )
            }
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Yellow,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Black),
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
                    Text("RESET", color = Yellow)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("CANCEL", color = White)
                }
            },
            containerColor = Black,
            titleContentColor = White,
            textContentColor = White,
        )
    }
}

@Suppress("FunctionName")
@Composable
fun StatusColumn(
    state: TennisMatchState,
    gameStatus: String,
    scoreManager: com.nuttyknot.tennisscoretracker.ScoreManager,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val fontSize =
        if (isLandscape) {
            ScoreScreenConstants.STATUS_TEXT_SIZE_LANDSCAPE
        } else {
            ScoreScreenConstants.STATUS_TEXT_SIZE_PORTRAIT
        }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            if (isLandscape) {
                Modifier.width(ScoreScreenConstants.MIDDLE_COLUMN_WIDTH)
            } else {
                Modifier
            },
    ) {
        Text(
            text = gameStatus,
            color = Yellow,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        if (state.isNewSet && state.matchWinner == null) {
            Spacer(modifier = Modifier.height(ScoreScreenConstants.VERTICAL_SPACING_MEDIUM))
            Button(
                onClick = { scoreManager.startNextSet() },
                colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = Black),
            ) {
                Text("START NEXT SET")
            }
        }
    }
}

@Suppress("FunctionName")
@Composable
fun ScoreColumn(
    data: ScoreDisplayData,
    mainTextSize: TextUnit,
    color: Color,
    alignment: Alignment = Alignment.Center,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        LandscapeScoreColumn(data, mainTextSize, color, alignment)
    } else {
        PortraitScoreColumn(data, mainTextSize, color)
    }
}

@Suppress("FunctionName")
@Composable
private fun LandscapeScoreColumn(
    data: ScoreDisplayData,
    mainTextSize: TextUnit,
    color: Color,
    alignment: Alignment,
) {
    val finalAlignment = if (data.score == "0") Alignment.Center else alignment

    Box(
        modifier = Modifier.fillMaxHeight().fillMaxWidth(),
        contentAlignment = finalAlignment,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.wrapContentWidth().fillMaxHeight(),
        ) {
            // Top spacer to push text to center
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = data.score,
                color = color,
                fontFamily = ScoreScreenConstants.JetBrainsMonoFamily,
                fontSize = mainTextSize,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.wrapContentWidth(),
            )

            // Bottom area for serving dot, also weighted to keep text centered
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.TopCenter,
            ) {
                if (data.isServing) {
                    Text(
                        text = "●",
                        color = color,
                        fontSize = ScoreScreenConstants.INDICATOR_SIZE,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun PortraitScoreColumn(
    data: ScoreDisplayData,
    mainTextSize: TextUnit,
    color: Color,
) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (data.isServing && !data.isUser) {
                    Text(
                        text = "●",
                        color = color,
                        fontSize = ScoreScreenConstants.INDICATOR_SIZE,
                        modifier = Modifier.padding(end = ScoreScreenConstants.SERVING_DOT_SPACING),
                    )
                }
            }

            // Transparent ghost text
            Text(
                text = data.score,
                color = Color.Transparent,
                fontFamily = ScoreScreenConstants.JetBrainsMonoFamily,
                fontSize = mainTextSize,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                softWrap = false,
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (data.isServing && data.isUser) {
                    Text(
                        text = "●",
                        color = color,
                        fontSize = ScoreScreenConstants.INDICATOR_SIZE,
                        modifier = Modifier.padding(start = ScoreScreenConstants.SERVING_DOT_SPACING),
                    )
                }
            }
        }

        // Actual score text, perfectly centered
        Text(
            text = data.score,
            color = color,
            fontFamily = ScoreScreenConstants.JetBrainsMonoFamily,
            fontSize = mainTextSize,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            softWrap = false,
        )
    }
}

data class ScoreDisplayData(
    val playerName: String,
    val score: String,
    val isServing: Boolean,
    val isUser: Boolean = false,
)
