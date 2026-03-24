package com.nuttyknot.tennisscoretracker.ui.score

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import com.nuttyknot.tennisscoretracker.MatchFormat
import com.nuttyknot.tennisscoretracker.shared.R

@Suppress("FunctionName", "LongParameterList")
@Composable
fun ScoreboardTable(
    userGames: Int,
    opponentGames: Int,
    setHistory: List<Pair<Int, Int>>,
    userColor: Color,
    opponentColor: Color,
    isMatchOver: Boolean = false,
    statusText: String? = null,
    onViewSummary: () -> Unit = {},
    userName: String = "",
    opponentName: String = "",
    isUserServing: Boolean = false,
    matchFormat: MatchFormat = MatchFormat.STANDARD,
    scaleFactor: Float = 1f,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            if (isLandscape) {
                Modifier.width(ScoreScreenConstants.MIDDLE_COLUMN_WIDTH * scaleFactor)
            } else {
                Modifier
            },
    ) {
        Text(
            text = statusText ?: " ",
            color = if (statusText != null) MaterialTheme.colorScheme.primary else Color.Transparent,
            fontSize = ScoreScreenConstants.SCOREBOARD_FONT_SIZE * scaleFactor,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(ScoreScreenConstants.SCOREBOARD_ROW_GAP * scaleFactor))

        Scoreboard(
            userGames = userGames,
            opponentGames = opponentGames,
            setHistory = setHistory,
            userColor = userColor,
            opponentColor = opponentColor,
            isMatchOver = isMatchOver,
            userName = userName,
            opponentName = opponentName,
            isUserServing = isUserServing,
            matchFormat = matchFormat,
            scaleFactor = scaleFactor,
        )

        Spacer(modifier = Modifier.height(ScoreScreenConstants.VERTICAL_SPACING_MEDIUM * scaleFactor))
        Button(
            onClick = onViewSummary,
            enabled = isMatchOver,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = if (isMatchOver) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (isMatchOver) MaterialTheme.colorScheme.onPrimary else Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = Color.Transparent,
                ),
        ) {
            Text(stringResource(R.string.match_summary_button))
        }
    }
}

@Suppress("FunctionName", "LongParameterList")
@Composable
fun Scoreboard(
    userGames: Int,
    opponentGames: Int,
    setHistory: List<Pair<Int, Int>>,
    userColor: Color,
    opponentColor: Color,
    isMatchOver: Boolean = false,
    userName: String = "",
    opponentName: String = "",
    isUserServing: Boolean = false,
    matchFormat: MatchFormat = MatchFormat.STANDARD,
    scaleFactor: Float = 1f,
) {
    val fontSize = ScoreScreenConstants.SCOREBOARD_FONT_SIZE * scaleFactor

    val maxSetColumns =
        when (matchFormat) {
            MatchFormat.FAST -> 0
            else -> 2
        }

    val hasNames = userName.isNotEmpty() || opponentName.isNotEmpty()
    val rowModifier = if (hasNames) Modifier.fillMaxWidth() else Modifier

    val rows = @Composable {
        // User row
        ScoreboardRow(
            name = userName,
            isServing = userName.isNotEmpty() && isUserServing,
            games = setHistory.map { it.first },
            currentGames = userGames,
            color = userColor,
            fontSize = fontSize,
            modifier = rowModifier,
            maxSetColumns = maxSetColumns,
            isMatchOver = isMatchOver,
            scaleFactor = scaleFactor,
        )

        Spacer(modifier = Modifier.height(ScoreScreenConstants.SCOREBOARD_ROW_GAP * scaleFactor))

        // Opponent row
        ScoreboardRow(
            name = opponentName,
            isServing = opponentName.isNotEmpty() && !isUserServing,
            games = setHistory.map { it.second },
            currentGames = opponentGames,
            color = opponentColor,
            fontSize = fontSize,
            modifier = rowModifier,
            maxSetColumns = maxSetColumns,
            isMatchOver = isMatchOver,
            scaleFactor = scaleFactor,
        )
    }

    if (hasNames) {
        Column(modifier = Modifier.width(IntrinsicSize.Max)) {
            rows()
        }
    } else {
        rows()
    }
}

@Suppress("FunctionName", "LongParameterList", "LongMethod")
@Composable
private fun ScoreboardRow(
    name: String,
    isServing: Boolean,
    games: List<Int>,
    currentGames: Int,
    color: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    maxSetColumns: Int = 0,
    isMatchOver: Boolean = false,
    scaleFactor: Float = 1f,
) {
    val columnGap = ScoreScreenConstants.SCOREBOARD_COLUMN_GAP * scaleFactor
    val dotSize = ScoreScreenConstants.SCOREBOARD_SERVING_DOT_SIZE * scaleFactor

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (name.isNotEmpty()) {
            if (!isMatchOver) {
                Box(
                    modifier = Modifier.size(dotSize),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isServing) {
                        Box(
                            modifier =
                                Modifier
                                    .size(dotSize)
                                    .clip(CircleShape)
                                    .background(color),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(columnGap))
            }
            Text(
                text = name,
                color = color.copy(alpha = ScoreScreenConstants.NAME_ALPHA),
                fontSize = fontSize,
                maxLines = 1,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
            )
        }
        repeat(maxSetColumns - games.size) {
            Spacer(modifier = Modifier.width(columnGap))
            Text(
                text = " ",
                color = Color.Transparent,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = ScoreScreenConstants.JetBrainsMonoFamily,
            )
        }
        games.forEach { setGames ->
            Spacer(modifier = Modifier.width(columnGap))
            Text(
                text = "$setGames",
                color = color.copy(alpha = ScoreScreenConstants.SCOREBOARD_MUTED_ALPHA),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = ScoreScreenConstants.JetBrainsMonoFamily,
            )
        }
        if (!isMatchOver) {
            Spacer(modifier = Modifier.width(columnGap))
            Text(
                text = "$currentGames",
                color = color,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = ScoreScreenConstants.JetBrainsMonoFamily,
            )
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
    scaleFactor: Float = 1f,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        LandscapeScoreColumn(data, mainTextSize, color, alignment, scaleFactor)
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
    scaleFactor: Float = 1f,
) {
    val finalAlignment = if (data.score == "0") Alignment.Center else alignment

    Column(
        modifier = Modifier.fillMaxHeight().fillMaxWidth(),
        horizontalAlignment =
            when (finalAlignment) {
                Alignment.CenterStart -> Alignment.Start
                Alignment.CenterEnd -> Alignment.End
                else -> Alignment.CenterHorizontally
            },
        verticalArrangement = Arrangement.Center,
    ) {
        if (data.name.isNotEmpty()) {
            PlayerNameLabel(
                name = data.name,
                isServing = data.isServing,
                color = color,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                scaleFactor = scaleFactor,
            )
        }
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
    }
}

@Suppress("FunctionName")
@Composable
private fun PortraitScoreColumn(
    data: ScoreDisplayData,
    mainTextSize: TextUnit,
    color: Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        if (data.name.isNotEmpty()) {
            PlayerNameLabel(
                name = data.name,
                isServing = data.isServing,
                color = color,
            )
        }
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

@Suppress("FunctionName")
@Composable
private fun PlayerNameLabel(
    name: String,
    isServing: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    scaleFactor: Float = 1f,
) {
    val dotRadius = ScoreScreenConstants.SERVING_DOT_RADIUS * scaleFactor
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dotRadius),
    ) {
        if (isServing) {
            Box(
                modifier =
                    Modifier
                        .size(dotRadius * 2)
                        .clip(CircleShape)
                        .background(color),
            )
        }
        Text(
            text = name,
            color = color.copy(alpha = ScoreScreenConstants.NAME_ALPHA),
            fontSize = ScoreScreenConstants.NAME_TEXT_SIZE * scaleFactor,
            maxLines = 1,
        )
    }
}

data class ScoreDisplayData(
    val score: String,
    val isServing: Boolean,
    val name: String = "",
)
