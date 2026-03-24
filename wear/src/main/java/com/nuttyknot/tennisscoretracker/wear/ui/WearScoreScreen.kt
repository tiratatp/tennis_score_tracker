@file:Suppress("TooManyFunctions")

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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.CurvedTextStyle
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeSource
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.TimeTextDefaults
import androidx.wear.compose.material3.timeTextCurvedText
import com.nuttyknot.tennisscoretracker.shared.WearScoreDisplay

private val SERVING_DOT_SIZE = 10.dp
private val SCORE_GAP = 16.dp
private val SCOREBOARD_COLUMN_GAP = 8.dp
private val SCOREBOARD_ROW_GAP = 1.dp
private const val PILL_CORNER_PERCENT = 50
private val BUTTON_HORIZONTAL_PADDING = 12.dp
private val BUTTON_VERTICAL_PADDING = 4.dp

@OptIn(ExperimentalFoundationApi::class)
@Suppress("FunctionName", "LongParameterList")
@Composable
fun WearScoreScreen(
    scoreDisplay: WearScoreDisplay,
    isConnected: Boolean,
    isAmbient: Boolean = false,
    showHelp: Boolean = false,
    timeSource: TimeSource = TimeTextDefaults.rememberTimeSource(TimeTextDefaults.timeFormat()),
    onDismissHelp: () -> Unit = {},
    onShowHelp: () -> Unit = {},
    onNewMatch: () -> Unit = {},
    onUserScored: () -> Unit,
    onOpponentScored: () -> Unit,
    onUndo: () -> Unit,
) {
    AppScaffold {
        ScreenScaffold(timeText = { WearTimeText(timeSource) }) {
            WearScoreContent(
                scoreDisplay = scoreDisplay,
                isConnected = isConnected,
                isAmbient = isAmbient,
                showHelp = showHelp,
                onShowHelp = onShowHelp,
                onDismissHelp = onDismissHelp,
                onNewMatch = onNewMatch,
                onUserScored = onUserScored,
                onOpponentScored = onOpponentScored,
                onUndo = onUndo,
            )
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun WearTimeText(timeSource: TimeSource) {
    val isRound = LocalConfiguration.current.isScreenRound
    if (isRound) {
        TimeText(timeSource = timeSource) { time ->
            timeTextCurvedText(
                time,
                CurvedTextStyle(
                    color = Color.White.copy(alpha = TIME_TEXT_ALPHA),
                    fontSize = DETAIL_FONT_SIZE,
                    fontWeight = FontWeight.Normal,
                ),
            )
        }
    } else {
        Text(
            text = timeSource.currentTime(),
            fontSize = DETAIL_FONT_SIZE,
            color = Color.White.copy(alpha = TIME_TEXT_ALPHA),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Suppress("FunctionName", "LongParameterList")
@Composable
private fun WearScoreContent(
    scoreDisplay: WearScoreDisplay,
    isConnected: Boolean,
    isAmbient: Boolean,
    showHelp: Boolean,
    onShowHelp: () -> Unit,
    onDismissHelp: () -> Unit,
    onNewMatch: () -> Unit,
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
            ScoreContent(scoreDisplay, isConnected, onNewMatch)

            if (isConnected && !scoreDisplay.isMatchOver) {
                TapZones(onUserScored, onOpponentScored, onUndo)
            }

            if (!showHelp) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Text(
                        text = "?",
                        fontSize = DETAIL_FONT_SIZE,
                        color = Color.White.copy(alpha = TIME_TEXT_ALPHA),
                        modifier =
                            Modifier
                                .padding(bottom = SCREEN_PADDING)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = onShowHelp,
                                ),
                    )
                }
            }

            if (showHelp) {
                val userColor = scoreDisplay.primaryColorArgb?.let { Color(it) } ?: DEFAULT_PRIMARY_COLOR
                val opponentColor =
                    scoreDisplay.secondaryColorArgb?.let { Color(it) } ?: DEFAULT_SECONDARY_COLOR
                WearHelpOverlay(
                    userColor = userColor,
                    opponentColor = opponentColor,
                    onDismiss = onDismissHelp,
                )
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
    ) {
        // Score content — centered in remaining space
        Column(
            modifier = Modifier.weight(1f),
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
}

@Suppress("FunctionName")
@Composable
private fun ScoreContent(
    scoreDisplay: WearScoreDisplay,
    isConnected: Boolean,
    onNewMatch: () -> Unit,
) {
    val userColor = scoreDisplay.primaryColorArgb?.let { Color(it) } ?: DEFAULT_PRIMARY_COLOR
    val opponentColor = scoreDisplay.secondaryColorArgb?.let { Color(it) } ?: DEFAULT_SECONDARY_COLOR

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(SCREEN_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top section: status — pushed to bottom of its space
        Column(
            modifier = Modifier.weight(TOP_SECTION_WEIGHT),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
        ) {
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
        }

        // Player names — PINNED at the boundary between weights
        PlayerNames(scoreDisplay, userColor, opponentColor, showServing = !scoreDisplay.isMatchOver)

        // Bottom section: point score + scoreboard + footer — pushed to top of its space
        Column(
            modifier = Modifier.weight(BOTTOM_SECTION_WEIGHT),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            if (!scoreDisplay.isMatchOver) {
                Spacer(modifier = Modifier.height(SPACER_HEIGHT))
                PointScore(scoreDisplay, userColor, opponentColor)
                Spacer(modifier = Modifier.height(SPACER_HEIGHT))
            } else {
                Spacer(modifier = Modifier.height(INNER_PADDING))
            }
            WearScoreboardTable(scoreDisplay, userColor, opponentColor)

            ScoreFooter(scoreDisplay, isConnected, onNewMatch)
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun ScoreFooter(
    scoreDisplay: WearScoreDisplay,
    isConnected: Boolean,
    onNewMatch: () -> Unit,
) {
    if (scoreDisplay.isMatchOver && isConnected) {
        val view = LocalView.current
        Spacer(modifier = Modifier.height(INNER_PADDING))
        Text(
            text = "New Match",
            fontSize = LABEL_FONT_SIZE,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .background(
                        color = Color(COLOR_TENNIS_GREEN),
                        shape = RoundedCornerShape(PILL_CORNER_PERCENT),
                    )
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        onNewMatch()
                    }
                    .padding(horizontal = BUTTON_HORIZONTAL_PADDING, vertical = BUTTON_VERTICAL_PADDING),
        )
    } else if (!isConnected) {
        Spacer(modifier = Modifier.height(SPACER_HEIGHT))
        Text(
            text = "Not connected",
            fontSize = LABEL_FONT_SIZE,
            color = Color(COLOR_DISCONNECTED_RED),
            textAlign = TextAlign.Center,
        )
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
    showServing: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SCORE_GAP),
    ) {
        PlayerLabel(
            name = scoreDisplay.userName.ifEmpty { "You" },
            isServing = showServing && scoreDisplay.isUserServing,
            color = userColor,
        )
        PlayerLabel(
            name = scoreDisplay.opponentName.ifEmpty { "Opp" },
            isServing = showServing && !scoreDisplay.isUserServing,
            color = opponentColor,
            dotOnRight = true,
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
private fun RowScope.PlayerLabel(
    name: String,
    isServing: Boolean,
    color: Color,
    dotOnRight: Boolean = false,
) {
    val maxNameWidth = LocalConfiguration.current.screenWidthDp.dp * NAME_MAX_WIDTH_FRACTION
    val dotColor = if (isServing) color else Color.Transparent
    val dot =
        @Composable {
            Box(
                modifier =
                    Modifier
                        .size(SERVING_DOT_SIZE)
                        .clip(CircleShape)
                        .background(dotColor),
            )
        }
    Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(
                INNER_PADDING,
                if (dotOnRight) Alignment.Start else Alignment.End,
            ),
    ) {
        if (!dotOnRight) dot()
        Text(
            text = name,
            fontSize = LABEL_FONT_SIZE,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false).widthIn(max = maxNameWidth),
        )
        if (dotOnRight) dot()
    }
}

private const val COLOR_DISCONNECTED_RED = 0xFFFF5252
