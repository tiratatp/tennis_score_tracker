package com.nuttyknot.tennisscoretracker.ui.score

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuttyknot.tennisscoretracker.MatchFormat
import com.nuttyknot.tennisscoretracker.ScoreModel
import com.nuttyknot.tennisscoretracker.SettingsManager
import com.nuttyknot.tennisscoretracker.TennisMatchState

// Constants moved to ScoreComponents.kt

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreScreen(
    scoreModel: ScoreModel,
    settingsManager: SettingsManager,
    onNavigateToSettings: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToSummary: () -> Unit = {},
) {
    val state by scoreModel.matchState.collectAsState()

    val doubleClickLatency by settingsManager.doubleClickLatencyFlow
        .collectAsState(initial = SettingsManager.DEFAULT_DOUBLE_CLICK_LATENCY)
    val longPressLatency by settingsManager.longPressLatencyFlow
        .collectAsState(initial = SettingsManager.DEFAULT_LONG_PRESS_LATENCY)

    var showResetDialog by rememberSaveable { mutableStateOf(false) }

    ResetConfirmationDialog(
        showDialog = showResetDialog,
        onConfirm = {
            scoreModel.reset()
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
                        scoreModel.reset()
                    } else {
                        showResetDialog = true
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        ScoreScreenContent(
            state = state,
            scoreModel = scoreModel,
            doubleClickLatency = doubleClickLatency,
            longPressLatency = longPressLatency,
            paddingValues = paddingValues,
            onNavigateToSummary = onNavigateToSummary,
        )
    }
}

@Suppress("FunctionName", "LongParameterList")
@Composable
private fun ScoreScreenContent(
    state: TennisMatchState,
    scoreModel: ScoreModel,
    doubleClickLatency: Long,
    longPressLatency: Long,
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    onNavigateToSummary: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(
                    horizontal = 16.dp,
                    vertical = if (isLandscape) 0.dp else 16.dp,
                )
                .pointerInput(doubleClickLatency, longPressLatency, state.matchWinner) {
                    detectTapGestures(
                        onTap = {
                            if (state.matchWinner == null) {
                                scoreModel.incrementUserScore()
                            }
                        },
                        onDoubleTap = {
                            if (state.matchWinner == null) {
                                scoreModel.incrementOpponentScore()
                            }
                        },
                        onLongPress = {
                            scoreModel.undo()
                        },
                    )
                },
    ) {
        val gameStatus = formatGameStatus(state)

        if (isLandscape) {
            LandscapeScoreContent(state, gameStatus, maxHeight.value, maxWidth.value, onNavigateToSummary)
        } else {
            PortraitScoreContent(state, gameStatus, maxHeight.value, maxWidth.value, onNavigateToSummary)
        }
    }
}

@Composable
private fun formatGameStatus(state: TennisMatchState): AnnotatedString {
    val primaryColor = MaterialTheme.colorScheme.primary
    return when {
        state.matchWinner != null -> AnnotatedString("MATCH OVER")
        state.setWinner != null -> AnnotatedString("SET OVER")
        state.isMatchTiebreak -> AnnotatedString("MATCH TIEBREAK")
        state.isDeuce -> AnnotatedString("DEUCE")
        state.matchFormat == MatchFormat.FAST ->
            AnnotatedString("${state.userGames}-${state.opponentGames}")
        else ->
            buildAnnotatedString {
                val mutedColor = primaryColor.copy(alpha = 0.5f)
                if (state.setHistory.isNotEmpty()) {
                    withStyle(SpanStyle(color = mutedColor)) {
                        append(state.setHistory.joinToString("  ") { "${it.first}-${it.second}" })
                    }
                    append("  ")
                }
                withStyle(SpanStyle(color = primaryColor)) {
                    append("${state.userGames}-${state.opponentGames}")
                }
            }
    }
}

@Suppress("FunctionName")
@Composable
private fun LandscapeScoreContent(
    state: TennisMatchState,
    gameStatus: AnnotatedString,
    maxHeight: Float,
    maxWidth: Float,
    onNavigateToSummary: () -> Unit,
) {
    val rawSize = maxHeight / ScoreScreenConstants.LANDSCAPE_TEXT_SIZE_RATIO.toFloat()
    val maxSafeSize =
        (maxWidth - ScoreScreenConstants.MIDDLE_COLUMN_WIDTH.value) /
            ScoreScreenConstants.LANDSCAPE_MAX_SAFE_SIZE_FACTOR
    val mainTextSize = minOf(rawSize, maxSafeSize).sp
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // User Score (Left)
        Box(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            contentAlignment = Alignment.CenterStart,
        ) {
            ScoreColumn(
                data =
                    ScoreDisplayData(
                        score = state.userScore.display,
                        isServing = state.isUserServing,
                    ),
                mainTextSize = mainTextSize,
                color = MaterialTheme.colorScheme.primary,
                alignment = Alignment.CenterStart,
            )
        }

        // Game Status (Middle)
        StatusColumn(
            gameStatus = gameStatus,
            isMatchOver = state.matchWinner != null,
            onViewSummary = onNavigateToSummary,
        )

        // Opponent Score (Right)
        Box(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            contentAlignment = Alignment.CenterEnd,
        ) {
            ScoreColumn(
                data =
                    ScoreDisplayData(
                        score = state.opponentScore.display,
                        isServing = !state.isUserServing,
                    ),
                mainTextSize = mainTextSize,
                color = MaterialTheme.colorScheme.secondary,
                alignment = Alignment.CenterEnd,
            )
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun PortraitScoreContent(
    state: TennisMatchState,
    gameStatus: AnnotatedString,
    maxHeight: Float,
    maxWidth: Float,
    onNavigateToSummary: () -> Unit,
) {
    val rawSize = maxHeight / ScoreScreenConstants.PORTRAIT_TEXT_SIZE_RATIO.toFloat()
    val maxSafeSize = maxWidth / ScoreScreenConstants.PORTRAIT_MAX_SAFE_SIZE_FACTOR
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
                        score = state.userScore.display,
                        isServing = state.isUserServing,
                    ),
                mainTextSize = mainTextSize,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        // Game Status
        StatusColumn(
            gameStatus = gameStatus,
            isMatchOver = state.matchWinner != null,
            onViewSummary = onNavigateToSummary,
        )

        // Opponent Score (Bottom)
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            ScoreColumn(
                data =
                    ScoreDisplayData(
                        score = state.opponentScore.display,
                        isServing = !state.isUserServing,
                    ),
                mainTextSize = mainTextSize,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}
