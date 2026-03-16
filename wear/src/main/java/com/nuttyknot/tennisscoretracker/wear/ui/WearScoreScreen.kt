package com.nuttyknot.tennisscoretracker.wear.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import com.nuttyknot.tennisscoretracker.shared.WearScoreDisplay

private val SERVING_DOT_SIZE = 8.dp
private val SCORE_FONT_SIZE = 40.sp
private val DETAIL_FONT_SIZE = 14.sp
private val LABEL_FONT_SIZE = 11.sp
private val SCREEN_PADDING = 8.dp
private val INNER_PADDING = 4.dp
private val SPACER_HEIGHT = 2.dp

@OptIn(ExperimentalFoundationApi::class)
@Suppress("FunctionName", "LongParameterList")
@Composable
fun WearScoreScreen(
    scoreDisplay: WearScoreDisplay,
    isConnected: Boolean,
    isAmbient: Boolean = false,
    onUserScored: () -> Unit,
    onOpponentScored: () -> Unit,
    onUndo: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black),
    ) {
        if (isAmbient) {
            AmbientScoreContent(scoreDisplay)
        } else {
            ScoreContent(scoreDisplay, isConnected)

            if (isConnected && !scoreDisplay.isMatchOver) {
                TapZones(onUserScored, onOpponentScored, onUndo)
            }
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun AmbientScoreContent(scoreDisplay: WearScoreDisplay) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(SCREEN_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "${scoreDisplay.userScore} - ${scoreDisplay.opponentScore}",
            fontSize = SCORE_FONT_SIZE,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(SPACER_HEIGHT))
        Text(
            text =
                "G ${scoreDisplay.userGames}-${scoreDisplay.opponentGames}  " +
                    "S ${scoreDisplay.userSets}-${scoreDisplay.opponentSets}",
            fontSize = DETAIL_FONT_SIZE,
            color = Color.Gray,
            textAlign = TextAlign.Center,
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun ScoreContent(
    scoreDisplay: WearScoreDisplay,
    isConnected: Boolean,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(SCREEN_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (scoreDisplay.isMatchOver && scoreDisplay.matchWinner != null) {
            Text(
                text = "${scoreDisplay.matchWinner} wins!",
                fontSize = LABEL_FONT_SIZE,
                color = Color(COLOR_TENNIS_GREEN),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(SPACER_HEIGHT))
        }

        PlayerNames(scoreDisplay)
        Spacer(modifier = Modifier.height(SPACER_HEIGHT))
        PointScore(scoreDisplay)
        Spacer(modifier = Modifier.height(SPACER_HEIGHT))
        GamesAndSets(scoreDisplay)

        if (scoreDisplay.setHistory.isNotEmpty()) {
            Spacer(modifier = Modifier.height(SPACER_HEIGHT))
            Text(
                text = scoreDisplay.setHistory.joinToString("  ") { "${it.first}-${it.second}" },
                fontSize = LABEL_FONT_SIZE,
                color = Color.Gray,
                textAlign = TextAlign.Center,
            )
        }

        if (!isConnected) {
            Spacer(modifier = Modifier.height(SPACER_HEIGHT))
            Text(
                text = "Not connected",
                fontSize = LABEL_FONT_SIZE,
                color = Color(COLOR_DISCONNECTED_RED),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun PlayerNames(scoreDisplay: WearScoreDisplay) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        PlayerLabel(
            name = scoreDisplay.userName.ifEmpty { "You" },
            isServing = scoreDisplay.isUserServing,
        )
        PlayerLabel(
            name = scoreDisplay.opponentName.ifEmpty { "Opp" },
            isServing = !scoreDisplay.isUserServing,
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun PointScore(scoreDisplay: WearScoreDisplay) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = scoreDisplay.userScore,
            fontSize = SCORE_FONT_SIZE,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Text(
            text = " - ",
            fontSize = SCORE_FONT_SIZE,
            color = Color.Gray,
        )
        Text(
            text = scoreDisplay.opponentScore,
            fontSize = SCORE_FONT_SIZE,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun GamesAndSets(scoreDisplay: WearScoreDisplay) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Text(
            text = "G ${scoreDisplay.userGames}-${scoreDisplay.opponentGames}",
            fontSize = DETAIL_FONT_SIZE,
            color = Color.LightGray,
        )
        Text(
            text = "S ${scoreDisplay.userSets}-${scoreDisplay.opponentSets}",
            fontSize = DETAIL_FONT_SIZE,
            color = Color.LightGray,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Suppress("FunctionName")
@Composable
private fun TapZones(
    onUserScored: () -> Unit,
    onOpponentScored: () -> Unit,
    onUndo: () -> Unit,
) {
    val view = LocalView.current
    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .combinedClickable(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            onUserScored()
                        },
                        onLongClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            onUndo()
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .combinedClickable(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            onOpponentScored()
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
}

@Suppress("FunctionName")
@Composable
private fun PlayerLabel(
    name: String,
    isServing: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(INNER_PADDING),
    ) {
        if (isServing) {
            Box(
                modifier =
                    Modifier
                        .size(SERVING_DOT_SIZE)
                        .clip(CircleShape)
                        .background(Color(COLOR_TENNIS_GREEN)),
            )
        }
        Text(
            text = name,
            fontSize = LABEL_FONT_SIZE,
            color = Color.White,
            maxLines = 1,
        )
    }
}

private const val COLOR_TENNIS_GREEN = 0xFF4CAF50
private const val COLOR_DISCONNECTED_RED = 0xFFFF5252
