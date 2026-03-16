package com.nuttyknot.tennisscoretracker.ui.score

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit

@Suppress("FunctionName")
@Composable
fun StatusColumn(
    gameStatus: String,
    isMatchOver: Boolean = false,
    onViewSummary: () -> Unit = {},
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
            color = MaterialTheme.colorScheme.primary,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        if (isMatchOver) {
            Spacer(modifier = Modifier.height(ScoreScreenConstants.VERTICAL_SPACING_MEDIUM))
            Button(
                onClick = onViewSummary,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            ) {
                Text("Match Summary")
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
    val dotRadiusPx = with(LocalDensity.current) { ScoreScreenConstants.SERVING_DOT_RADIUS.toPx() }
    val dotColor = color

    Box(
        modifier = Modifier.fillMaxHeight().fillMaxWidth(),
        contentAlignment = finalAlignment,
    ) {
        Text(
            text = data.score,
            color = color,
            fontFamily = ScoreScreenConstants.JetBrainsMonoFamily,
            fontSize = mainTextSize,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            softWrap = false,
            modifier =
                Modifier.wrapContentWidth().drawBehind {
                    if (data.isServing) {
                        drawCircle(
                            color = dotColor,
                            radius = dotRadiusPx,
                            center = Offset(size.width - dotRadiusPx, dotRadiusPx),
                        )
                    }
                },
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun PortraitScoreColumn(
    data: ScoreDisplayData,
    mainTextSize: TextUnit,
    color: Color,
) {
    val dotRadiusPx = with(LocalDensity.current) { ScoreScreenConstants.SERVING_DOT_RADIUS.toPx() }
    val dotColor = color

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = data.score,
            color = color,
            fontFamily = ScoreScreenConstants.JetBrainsMonoFamily,
            fontSize = mainTextSize,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            softWrap = false,
            modifier =
                Modifier.drawBehind {
                    if (data.isServing) {
                        drawCircle(
                            color = dotColor,
                            radius = dotRadiusPx,
                            center = Offset(size.width - dotRadiusPx, dotRadiusPx),
                        )
                    }
                },
        )
    }
}

data class ScoreDisplayData(
    val score: String,
    val isServing: Boolean,
)
