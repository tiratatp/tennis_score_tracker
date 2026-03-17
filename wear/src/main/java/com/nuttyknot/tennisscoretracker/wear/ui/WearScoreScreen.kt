package com.nuttyknot.tennisscoretracker.wear.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import com.nuttyknot.tennisscoretracker.shared.WearScoreDisplay

private val SERVING_DOT_SIZE = 10.dp
private val SCORE_FONT_SIZE = 40.sp
private val DETAIL_FONT_SIZE = 14.sp
private val LABEL_FONT_SIZE = 11.sp
private val SCREEN_PADDING = 8.dp
private val INNER_PADDING = 4.dp
private val SPACER_HEIGHT = 2.dp
private val SCORE_GAP = 16.dp
private val SCOREBOARD_COLUMN_GAP = 8.dp
private val SCOREBOARD_ROW_GAP = 1.dp
private const val SCOREBOARD_MUTED_ALPHA = 0.5f
private const val COLOR_SKY_BLUE = 0xFF38BDF8
private val DEFAULT_PRIMARY_COLOR = Color(COLOR_SKY_BLUE)
private val DEFAULT_SECONDARY_COLOR = Color.White

@OptIn(ExperimentalFoundationApi::class)
@Suppress("FunctionName", "LongParameterList")
@Composable
fun WearScoreScreen(
    scoreDisplay: WearScoreDisplay,
    isConnected: Boolean,
    isAmbient: Boolean = false,
    showHelp: Boolean = false,
    onDismissHelp: () -> Unit = {},
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

            if (showHelp) {
                WearHelpOverlay(onDismiss = onDismissHelp)
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
            fontFamily = FontFamily.Monospace,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(SPACER_HEIGHT))
        Row(
            horizontalArrangement = Arrangement.spacedBy(INNER_PADDING * 2),
        ) {
            scoreDisplay.setHistory.forEach { (user, opp) ->
                Text(
                    text = "$user-$opp",
                    fontSize = DETAIL_FONT_SIZE,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Gray,
                )
            }
            if (!scoreDisplay.isMatchOver) {
                Text(
                    text = "${scoreDisplay.userGames}-${scoreDisplay.opponentGames}",
                    fontSize = DETAIL_FONT_SIZE,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Gray,
                )
            }
        }
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
        val userColor = scoreDisplay.primaryColorArgb?.let { Color(it) } ?: DEFAULT_PRIMARY_COLOR
        val opponentColor = scoreDisplay.secondaryColorArgb?.let { Color(it) } ?: DEFAULT_SECONDARY_COLOR

        val statusText =
            if (scoreDisplay.isMatchOver && scoreDisplay.matchWinner != null) {
                "${scoreDisplay.matchWinner} wins!"
            } else {
                " "
            }
        Text(
            text = statusText,
            fontSize = LABEL_FONT_SIZE,
            color = if (statusText != " ") Color(COLOR_TENNIS_GREEN) else Color.Transparent,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(SPACER_HEIGHT))

        PlayerNames(scoreDisplay, userColor, opponentColor)
        Spacer(modifier = Modifier.height(SPACER_HEIGHT))
        PointScore(scoreDisplay, userColor, opponentColor)
        Spacer(modifier = Modifier.height(SPACER_HEIGHT))
        WearScoreboardTable(scoreDisplay, userColor, opponentColor)

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
private fun WearScoreboardTable(
    scoreDisplay: WearScoreDisplay,
    userColor: Color,
    opponentColor: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // User row
        Row(
            horizontalArrangement = Arrangement.spacedBy(SCOREBOARD_COLUMN_GAP),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            scoreDisplay.setHistory.forEach { (userSet, _) ->
                Text(
                    text = "$userSet",
                    color = userColor.copy(alpha = SCOREBOARD_MUTED_ALPHA),
                    fontSize = DETAIL_FONT_SIZE,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                )
            }
            if (!scoreDisplay.isMatchOver) {
                Text(
                    text = "${scoreDisplay.userGames}",
                    color = userColor,
                    fontSize = DETAIL_FONT_SIZE,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }

        Spacer(modifier = Modifier.height(SCOREBOARD_ROW_GAP))

        // Opponent row
        Row(
            horizontalArrangement = Arrangement.spacedBy(SCOREBOARD_COLUMN_GAP),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            scoreDisplay.setHistory.forEach { (_, oppSet) ->
                Text(
                    text = "$oppSet",
                    color = opponentColor.copy(alpha = SCOREBOARD_MUTED_ALPHA),
                    fontSize = DETAIL_FONT_SIZE,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                )
            }
            if (!scoreDisplay.isMatchOver) {
                Text(
                    text = "${scoreDisplay.opponentGames}",
                    color = opponentColor,
                    fontSize = DETAIL_FONT_SIZE,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun PlayerNames(
    scoreDisplay: WearScoreDisplay,
    userColor: Color,
    opponentColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        PlayerLabel(
            name = scoreDisplay.userName.ifEmpty { "You" },
            isServing = scoreDisplay.isUserServing,
            color = userColor,
        )
        PlayerLabel(
            name = scoreDisplay.opponentName.ifEmpty { "Opp" },
            isServing = !scoreDisplay.isUserServing,
            color = opponentColor,
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun PointScore(
    scoreDisplay: WearScoreDisplay,
    userColor: Color,
    opponentColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = scoreDisplay.userScore,
            fontSize = SCORE_FONT_SIZE,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = userColor,
        )
        Spacer(modifier = Modifier.size(SCORE_GAP))
        Text(
            text = scoreDisplay.opponentScore,
            fontSize = SCORE_FONT_SIZE,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = opponentColor,
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
    color: Color,
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
                        .background(color),
            )
        }
        Text(
            text = name,
            fontSize = LABEL_FONT_SIZE,
            color = color,
            maxLines = 1,
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun WearHelpOverlay(onDismiss: () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(SCREEN_PADDING * 2),
        ) {
            Text(
                text = "How to Play",
                fontSize = DETAIL_FONT_SIZE,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap left = your point\nTap right = opponent's point\nLong press = undo",
                fontSize = LABEL_FONT_SIZE,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap to dismiss",
                fontSize = LABEL_FONT_SIZE,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

private const val COLOR_TENNIS_GREEN = 0xFF4CAF50
private const val COLOR_DISCONNECTED_RED = 0xFFFF5252
