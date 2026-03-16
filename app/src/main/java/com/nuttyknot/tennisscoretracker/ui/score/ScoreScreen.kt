package com.nuttyknot.tennisscoretracker.ui.score

import android.content.res.Configuration
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuttyknot.tennisscoretracker.MatchFormat
import com.nuttyknot.tennisscoretracker.ScoreModel
import com.nuttyknot.tennisscoretracker.TennisMatchState

// Constants moved to ScoreComponents.kt

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreScreen(
    scoreModel: ScoreModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToSummary: () -> Unit = {},
) {
    val state by scoreModel.matchState.collectAsState()

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
            paddingValues = paddingValues,
            onNavigateToSummary = onNavigateToSummary,
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun ScoreScreenContent(
    state: TennisMatchState,
    scoreModel: ScoreModel,
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
                ),
    ) {
        val gameStatus = formatGameStatus(state)

        if (isLandscape) {
            LandscapeScoreContent(state, gameStatus, maxHeight.value, maxWidth.value, onNavigateToSummary)
        } else {
            PortraitScoreContent(state, gameStatus, maxHeight.value, maxWidth.value, onNavigateToSummary)
        }

        TapZones(
            isLandscape = isLandscape,
            matchWinner = state.matchWinner,
            onUserScored = { scoreModel.incrementUserScore() },
            onOpponentScored = { scoreModel.incrementOpponentScore() },
            onUndo = { scoreModel.undo() },
        )
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
                        name = state.userName.ifEmpty { "You" },
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
                        name = state.opponentName.ifEmpty { "Opp" },
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
                        name = state.userName.ifEmpty { "You" },
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
                        name = state.opponentName.ifEmpty { "Opp" },
                    ),
                mainTextSize = mainTextSize,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Suppress("FunctionName", "LongParameterList")
@Composable
private fun TapZones(
    isLandscape: Boolean,
    matchWinner: String?,
    onUserScored: () -> Unit,
    onOpponentScored: () -> Unit,
    onUndo: () -> Unit,
) {
    val view = LocalView.current

    @Composable
    fun TapZone(
        onClick: () -> Unit,
        modifier: Modifier,
    ) {
        Box(
            modifier =
                modifier
                    .combinedClickable(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            onClick()
                        },
                        onLongClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            onUndo()
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ),
        )
    }

    if (isLandscape) {
        Row(modifier = Modifier.fillMaxSize()) {
            TapZone(
                onClick = { if (matchWinner == null) onUserScored() },
                modifier = Modifier.fillMaxHeight().weight(1f),
            )
            TapZone(
                onClick = { if (matchWinner == null) onOpponentScored() },
                modifier = Modifier.fillMaxHeight().weight(1f),
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            TapZone(
                onClick = { if (matchWinner == null) onUserScored() },
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
            TapZone(
                onClick = { if (matchWinner == null) onOpponentScored() },
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
        }
    }
}
