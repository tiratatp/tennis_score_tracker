package com.nuttyknot.tennisscoretracker.ui.score

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.nuttyknot.tennisscoretracker.TennisMatchState

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
            color = MaterialTheme.colorScheme.primary,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        if (state.isNewSet && state.matchWinner == null) {
            Spacer(modifier = Modifier.height(ScoreScreenConstants.VERTICAL_SPACING_MEDIUM))
            Button(
                onClick = { scoreManager.startNextSet() },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
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
