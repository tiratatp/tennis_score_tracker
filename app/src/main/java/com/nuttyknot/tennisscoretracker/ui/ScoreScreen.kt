package com.nuttyknot.tennisscoretracker.ui

import android.content.res.Configuration
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuttyknot.tennisscoretracker.ScoreManager
import com.nuttyknot.tennisscoretracker.SettingsManager
import com.nuttyknot.tennisscoretracker.TennisMatchState
import com.nuttyknot.tennisscoretracker.ui.theme.Black
import com.nuttyknot.tennisscoretracker.ui.theme.White
import com.nuttyknot.tennisscoretracker.ui.theme.Yellow

// Constants moved to ScoreComponents.kt

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreScreen(
    scoreManager: ScoreManager,
    settingsManager: SettingsManager,
    onNavigateToSettings: () -> Unit,
    onNavigateToHelp: () -> Unit,
) {
    val state by scoreManager.matchState.collectAsState()

    val doubleClickLatency by settingsManager.doubleClickLatencyFlow
        .collectAsState(initial = SettingsManager.DEFAULT_DOUBLE_CLICK_LATENCY)
    val longPressLatency by settingsManager.longPressLatencyFlow
        .collectAsState(initial = SettingsManager.DEFAULT_LONG_PRESS_LATENCY)

    var showResetDialog by remember { mutableStateOf(false) }

    ResetConfirmationDialog(
        showDialog = showResetDialog,
        onConfirm = {
            scoreManager.reset()
            showResetDialog = false
        },
        onDismiss = { showResetDialog = false },
    )

    Scaffold(
        topBar = {
            ScoreTopBar(
                onNavigateToHelp = onNavigateToHelp,
                onNavigateToSettings = onNavigateToSettings,
                onResetClick = {
                    if (state.isScoreZero) {
                        scoreManager.reset()
                    } else {
                        showResetDialog = true
                    }
                },
            )
        },
        containerColor = Black,
    ) { paddingValues ->
        ScoreScreenContent(
            state = state,
            scoreManager = scoreManager,
            doubleClickLatency = doubleClickLatency,
            longPressLatency = longPressLatency,
            paddingValues = paddingValues,
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun ScoreScreenContent(
    state: TennisMatchState,
    scoreManager: ScoreManager,
    doubleClickLatency: Long,
    longPressLatency: Long,
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .pointerInput(doubleClickLatency, longPressLatency) {
                    detectTapGestures(
                        onTap = {
                            if (state.matchWinner == null) {
                                scoreManager.incrementUserScore()
                            }
                        },
                        onDoubleTap = {
                            if (state.matchWinner == null) {
                                scoreManager.incrementOpponentScore()
                            }
                        },
                        onLongPress = {
                            scoreManager.undo()
                        },
                    )
                },
    ) {
        val gameStatus = formatGameStatus(state, isLandscape)

        if (isLandscape) {
            LandscapeScoreContent(state, gameStatus, maxHeight.value, maxWidth.value, scoreManager)
        } else {
            PortraitScoreContent(state, gameStatus, maxHeight.value, maxWidth.value, scoreManager)
        }
    }
}

private fun formatGameStatus(
    state: TennisMatchState,
    isLandscape: Boolean,
): String {
    return when {
        state.matchWinner != null -> "MATCH OVER"
        state.setWinner != null -> "SET OVER"
        state.isDeuce -> "DEUCE"
        else -> {
            if (isLandscape) {
                "Sets: ${state.userSets} - ${state.opponentSets}\n" +
                    "Games: ${state.userGames} - ${state.opponentGames}"
            } else {
                "Sets: ${state.userSets} - ${state.opponentSets}  |  " +
                    "Games: ${state.userGames} - ${state.opponentGames}"
            }
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun LandscapeScoreContent(
    state: TennisMatchState,
    gameStatus: String,
    maxHeight: Float,
    maxWidth: Float,
    scoreManager: ScoreManager,
) {
    val rawSize = maxHeight / ScoreScreenConstants.LANDSCAPE_TEXT_SIZE_RATIO.toFloat()
    val maxSafeSize =
        (maxWidth - ScoreScreenConstants.MIDDLE_COLUMN_WIDTH.value) /
            ScoreScreenConstants.LANDSCAPE_MAX_SAFE_SIZE_FACTOR
    val mainTextSize = minOf(rawSize, maxSafeSize).sp
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // User Score (Left)
        Box(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            contentAlignment = Alignment.Center,
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

        // Game Status (Middle)
        StatusColumn(state = state, gameStatus = gameStatus, scoreManager = scoreManager)

        // Opponent Score (Right)
        Box(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            contentAlignment = Alignment.Center,
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
    }
}

@Suppress("FunctionName")
@Composable
private fun PortraitScoreContent(
    state: TennisMatchState,
    gameStatus: String,
    maxHeight: Float,
    maxWidth: Float,
    scoreManager: ScoreManager,
) {
    val rawSize = maxHeight / ScoreScreenConstants.PORTRAIT_TEXT_SIZE_RATIO.toFloat()
    val maxSafeSize = maxWidth / 2.0f
    val mainTextSize = minOf(rawSize, maxSafeSize).sp
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // User Score (Top)
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
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

        // Game Status
        StatusColumn(state = state, gameStatus = gameStatus, scoreManager = scoreManager)

        // Opponent Score (Bottom)
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
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
    }
}
