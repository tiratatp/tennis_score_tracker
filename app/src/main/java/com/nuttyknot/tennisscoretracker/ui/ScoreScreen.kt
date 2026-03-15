package com.nuttyknot.tennisscoretracker.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuttyknot.tennisscoretracker.ScoreManager
import com.nuttyknot.tennisscoretracker.TennisMatchState
import com.nuttyknot.tennisscoretracker.ui.theme.Black
import com.nuttyknot.tennisscoretracker.ui.theme.White
import com.nuttyknot.tennisscoretracker.ui.theme.Yellow

private object ScoreScreenConstants {
    const val LANDSCAPE_TEXT_SIZE_RATIO = 1.1
    const val PORTRAIT_TEXT_SIZE_RATIO = 3.5
    val MIDDLE_COLUMN_WIDTH = 180.dp
    val STATUS_TEXT_SIZE_LANDSCAPE = 22.sp
    val STATUS_TEXT_SIZE_PORTRAIT = 28.sp
    val NAME_TEXT_SIZE = 24.sp
    val INDICATOR_SIZE = 24.sp
    const val NAME_ALPHA = 0.7f
    val SERVING_DOT_SPACING = 8.dp
    val VERTICAL_SPACING_LARGE = 32.dp
    val VERTICAL_SPACING_MEDIUM = 16.dp
}

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreScreen(
    scoreManager: ScoreManager,
    onNavigateToSettings: () -> Unit,
) {
    val state by scoreManager.matchState.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tennis Score Tracker", color = White) },
                actions = {
                    IconButton(onClick = { scoreManager.reset() }) {
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
        },
        containerColor = Black,
    ) { paddingValues ->
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
        ) {
            val gameStatus = formatGameStatus(state)

            if (isLandscape) {
                LandscapeScoreContent(state, gameStatus, maxHeight.value, scoreManager)
            } else {
                PortraitScoreContent(state, gameStatus, maxHeight.value, scoreManager)
            }
        }
    }
}

private fun formatGameStatus(state: TennisMatchState): String {
    return when {
        state.matchWinner != null -> "MATCH OVER"
        state.setWinner != null -> "SET OVER"
        state.isDeuce -> "DEUCE"
        else ->
            "Sets: ${state.userSets} - ${state.opponentSets}  |  " +
                "Games: ${state.userGames} - ${state.opponentGames}"
    }
}

@Suppress("FunctionName")
@Composable
private fun LandscapeScoreContent(
    state: TennisMatchState,
    gameStatus: String,
    maxHeight: Float,
    scoreManager: ScoreManager,
) {
    val mainTextSize = (maxHeight / ScoreScreenConstants.LANDSCAPE_TEXT_SIZE_RATIO).sp
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Opponent Score (Left)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
        ) {
            ScoreColumn(
                data =
                    ScoreDisplayData(
                        playerName = state.opponentName.ifEmpty { "OPPONENT" },
                        score = state.opponentScore.display,
                        isServing = !state.isUserServing,
                    ),
                mainTextSize = mainTextSize,
                color = White,
            )
        }

        // Game Status (Middle)
        StatusColumn(state = state, gameStatus = gameStatus, scoreManager = scoreManager)

        // User Score (Right)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
        ) {
            ScoreColumn(
                data =
                    ScoreDisplayData(
                        playerName = state.userName.ifEmpty { "YOU" },
                        score = state.userScore.display,
                        isServing = state.isUserServing,
                        isUser = true,
                    ),
                mainTextSize = mainTextSize,
                color = Yellow,
            )
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun PortraitScoreContent(
    state: TennisMatchState,
    gameStatus: String,
    maxHeight: Float,
    scoreManager: ScoreManager,
) {
    val mainTextSize = (maxHeight / ScoreScreenConstants.PORTRAIT_TEXT_SIZE_RATIO).sp
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Opponent Score
        ScoreColumn(
            data =
                ScoreDisplayData(
                    playerName = state.opponentName.ifEmpty { "OPPONENT" },
                    score = state.opponentScore.display,
                    isServing = !state.isUserServing,
                ),
            mainTextSize = mainTextSize,
            color = White,
        )

        Spacer(modifier = Modifier.height(ScoreScreenConstants.VERTICAL_SPACING_LARGE))

        // Game Status
        StatusColumn(state = state, gameStatus = gameStatus, scoreManager = scoreManager)

        Spacer(modifier = Modifier.height(ScoreScreenConstants.VERTICAL_SPACING_MEDIUM))

        // User Score
        ScoreColumn(
            data =
                ScoreDisplayData(
                    playerName = state.userName.ifEmpty { "YOU" },
                    score = state.userScore.display,
                    isServing = state.isUserServing,
                    isUser = true,
                ),
            mainTextSize = mainTextSize,
            color = Yellow,
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun ScoreColumn(
    data: ScoreDisplayData,
    mainTextSize: androidx.compose.ui.unit.TextUnit,
    color: androidx.compose.ui.graphics.Color,
) {
    if (!data.isUser) {
        Text(
            text = data.playerName.uppercase(),
            color = color.copy(alpha = ScoreScreenConstants.NAME_ALPHA),
            fontSize = ScoreScreenConstants.NAME_TEXT_SIZE,
            fontWeight = FontWeight.Bold,
        )
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (data.isServing && !data.isUser) {
            Text(
                text = "●",
                color = color,
                fontSize = ScoreScreenConstants.INDICATOR_SIZE,
            )
            Spacer(modifier = Modifier.width(ScoreScreenConstants.SERVING_DOT_SPACING))
        }
        Text(
            text = data.score,
            color = color,
            fontSize = mainTextSize,
            fontWeight = FontWeight.ExtraBold,
        )
        if (data.isServing && data.isUser) {
            Spacer(modifier = Modifier.width(ScoreScreenConstants.SERVING_DOT_SPACING))
            Text(
                text = "●",
                color = color,
                fontSize = ScoreScreenConstants.INDICATOR_SIZE,
            )
        }
    }
    if (data.isUser) {
        Text(
            text = data.playerName.uppercase(),
            color = color.copy(alpha = ScoreScreenConstants.NAME_ALPHA),
            fontSize = ScoreScreenConstants.NAME_TEXT_SIZE,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun StatusColumn(
    state: TennisMatchState,
    gameStatus: String,
    scoreManager: ScoreManager,
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

private data class ScoreDisplayData(
    val playerName: String,
    val score: String,
    val isServing: Boolean,
    val isUser: Boolean = false,
)
