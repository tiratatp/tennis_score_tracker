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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.CurvedTextStyle
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeSource
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.TimeTextDefaults
import androidx.wear.compose.material3.timeTextCurvedText
import com.nuttyknot.tennisscoretracker.shared.R
import com.nuttyknot.tennisscoretracker.shared.WearScoreDisplay
import kotlinx.coroutines.delay

private const val SERVING_DOT_SIZE_BASE = 10f
private const val SCORE_GAP_BASE = 16f
private const val SCOREBOARD_COLUMN_GAP_BASE = 8f
private const val SCOREBOARD_ROW_GAP_BASE = 1f
private const val PILL_CORNER_PERCENT = 50
private const val BUTTON_HORIZONTAL_PADDING_BASE = 12f
private const val BUTTON_VERTICAL_PADDING_BASE = 4f
private val QUESTION_MARK_ICON_SIZE = 20.dp

@OptIn(ExperimentalFoundationApi::class)
@Suppress("FunctionName", "LongParameterList")
@Composable
fun WearScoreScreen(
    scoreDisplay: WearScoreDisplay,
    isConnected: Boolean,
    isAmbient: Boolean = false,
    burnInProtectionRequired: Boolean = false,
    lowBitAmbient: Boolean = false,
    ambientOffset: IntOffset = IntOffset.Zero,
    showHelp: Boolean = false,
    timeSource: TimeSource = TimeTextDefaults.rememberTimeSource(TimeTextDefaults.timeFormat()),
    onDismissHelp: () -> Unit = {},
    onShowHelp: () -> Unit = {},
    onNewMatch: () -> Unit = {},
    onEndMatch: () -> Unit = {},
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
                burnInProtectionRequired = burnInProtectionRequired,
                lowBitAmbient = lowBitAmbient,
                ambientOffset = ambientOffset,
                showHelp = showHelp,
                onShowHelp = onShowHelp,
                onDismissHelp = onDismissHelp,
                onNewMatch = onNewMatch,
                onEndMatch = onEndMatch,
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
    val scale = screenScale()
    if (isRound) {
        TimeText(timeSource = timeSource) { time ->
            timeTextCurvedText(
                time,
                CurvedTextStyle(
                    color = Color.White.copy(alpha = TIME_TEXT_ALPHA),
                    fontSize = detailFontSize(scale),
                    fontWeight = FontWeight.Normal,
                ),
            )
        }
    } else {
        Text(
            text = timeSource.currentTime(),
            fontSize = detailFontSize(scale),
            color = Color.White.copy(alpha = TIME_TEXT_ALPHA),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
    burnInProtectionRequired: Boolean,
    lowBitAmbient: Boolean,
    ambientOffset: IntOffset,
    showHelp: Boolean,
    onShowHelp: () -> Unit,
    onDismissHelp: () -> Unit,
    onNewMatch: () -> Unit,
    onEndMatch: () -> Unit,
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
            AmbientScoreContent(
                scoreDisplay = scoreDisplay,
                burnInProtectionRequired = burnInProtectionRequired,
                lowBitAmbient = lowBitAmbient,
                ambientOffset = ambientOffset,
            )
        } else {
            var confirmingEndMatch by remember { mutableStateOf(false) }
            val showEndButton = isConnected && !scoreDisplay.isMatchOver

            ScoreContent(scoreDisplay, isConnected, onNewMatch)

            if (showEndButton) {
                TapZones(scoreDisplay, onUserScored, onOpponentScored, onUndo)
            }

            if (!showHelp && !confirmingEndMatch) {
                BottomActionBar(
                    showEndButton = showEndButton,
                    onEndMatchTapped = { confirmingEndMatch = true },
                    onShowHelp = onShowHelp,
                )
            }

            if (confirmingEndMatch) {
                EndMatchConfirmOverlay(
                    onConfirm = {
                        onEndMatch()
                        confirmingEndMatch = false
                    },
                    onDismiss = { confirmingEndMatch = false },
                )
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
private fun AmbientScoreContent(
    scoreDisplay: WearScoreDisplay,
    burnInProtectionRequired: Boolean,
    lowBitAmbient: Boolean,
    ambientOffset: IntOffset,
) {
    val scale = screenScale()
    val isRound = LocalConfiguration.current.isScreenRound
    val hPadding = if (isRound) roundHorizontalPadding(scale) else screenPadding(scale)
    val scoreFontWeight = if (lowBitAmbient) FontWeight.Normal else FontWeight.Bold
    val offsetModifier =
        if (burnInProtectionRequired) {
            Modifier.offset { ambientOffset }
        } else {
            Modifier
        }
    Column(
        modifier =
            offsetModifier
                .fillMaxSize()
                .padding(horizontal = hPadding, vertical = screenPadding(scale)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top spacer — same weight as interactive top section
        Spacer(modifier = Modifier.weight(TOP_SECTION_WEIGHT))

        // Player names with serving dot in white
        PlayerNames(scoreDisplay, Color.White, Color.Gray)

        // Bottom section — same weight as interactive, pushed to top
        Column(
            modifier = Modifier.weight(BOTTOM_SECTION_WEIGHT),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(modifier = Modifier.height(spacerHeight(scale)))
            PointScore(scoreDisplay, Color.White, Color.Gray, scoreFontWeight)
            Spacer(modifier = Modifier.height(spacerHeight(scale)))
            WearScoreboardTable(scoreDisplay, Color.White, Color.Gray, scoreFontWeight)
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
    val scale = screenScale()
    val isRound = LocalConfiguration.current.isScreenRound
    val hPadding = if (isRound) roundHorizontalPadding(scale) else screenPadding(scale)
    val userColor = scoreDisplay.primaryColorArgb?.let { Color(it) } ?: DEFAULT_PRIMARY_COLOR
    val opponentColor = scoreDisplay.secondaryColorArgb?.let { Color(it) } ?: DEFAULT_SECONDARY_COLOR

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = hPadding, vertical = screenPadding(scale)),
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
                    stringResource(R.string.winner_announcement, scoreDisplay.matchWinner!!)
                } else {
                    " "
                }
            Text(
                text = statusText,
                fontSize = labelFontSize(scale),
                color = if (statusText != " ") Color(COLOR_TENNIS_GREEN) else Color.Transparent,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(spacerHeight(scale)))
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
                Spacer(modifier = Modifier.height(spacerHeight(scale)))
                PointScore(scoreDisplay, userColor, opponentColor)
                Spacer(modifier = Modifier.height(spacerHeight(scale)))
            } else {
                Spacer(modifier = Modifier.height(innerPadding(scale)))
            }
            WearScoreboardTable(scoreDisplay, userColor, opponentColor)

            ScoreFooter(scoreDisplay, isConnected, onNewMatch)
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun BottomActionBar(
    showEndButton: Boolean,
    onEndMatchTapped: () -> Unit,
    onShowHelp: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showEndButton) {
                IconButton(
                    onClick = onEndMatchTapped,
                    modifier = Modifier.size(IconButtonDefaults.SmallButtonSize),
                    colors =
                        IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White.copy(alpha = TIME_TEXT_ALPHA),
                        ),
                ) {
                    Icon(
                        painter = painterResource(com.nuttyknot.tennisscoretracker.wear.R.drawable.ic_close),
                        contentDescription = stringResource(R.string.end_match),
                        modifier = Modifier.size(IconButtonDefaults.SmallIconSize),
                    )
                }
            }
            IconButton(
                onClick = onShowHelp,
                modifier = Modifier.size(IconButtonDefaults.SmallButtonSize),
                colors =
                    IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White.copy(alpha = TIME_TEXT_ALPHA),
                    ),
            ) {
                Icon(
                    painter = painterResource(com.nuttyknot.tennisscoretracker.wear.R.drawable.ic_question_mark),
                    contentDescription = stringResource(R.string.help_button),
                    modifier = Modifier.size(QUESTION_MARK_ICON_SIZE),
                )
            }
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun EndMatchConfirmOverlay(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val scale = screenScale()
    val view = LocalView.current

    LaunchedEffect(Unit) {
        delay(END_MATCH_CONFIRM_TIMEOUT)
        onDismiss()
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = OVERLAY_ALPHA))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    onConfirm()
                },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.end_match_confirm),
            fontSize = labelFontSize(scale),
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier =
                Modifier
                    .background(
                        color = Color(COLOR_DISCONNECTED_RED),
                        shape = RoundedCornerShape(PILL_CORNER_PERCENT),
                    ).padding(
                        horizontal = (BUTTON_HORIZONTAL_PADDING_BASE * scale).dp,
                        vertical = (BUTTON_VERTICAL_PADDING_BASE * scale).dp,
                    ),
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun ScoreFooter(
    scoreDisplay: WearScoreDisplay,
    isConnected: Boolean,
    onNewMatch: () -> Unit,
) {
    val scale = screenScale()
    if (scoreDisplay.isMatchOver && isConnected) {
        val view = LocalView.current
        Box(
            modifier =
                Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        onNewMatch()
                    },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.new_match),
                fontSize = labelFontSize(scale),
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier =
                    Modifier
                        .background(
                            color = Color(COLOR_TENNIS_GREEN),
                            shape = RoundedCornerShape(PILL_CORNER_PERCENT),
                        ).padding(
                            horizontal = (BUTTON_HORIZONTAL_PADDING_BASE * scale).dp,
                            vertical = (BUTTON_VERTICAL_PADDING_BASE * scale).dp,
                        ),
            )
        }
    } else if (!isConnected) {
        Spacer(modifier = Modifier.height(spacerHeight(scale)))
        Text(
            text = stringResource(R.string.not_connected),
            fontSize = labelFontSize(scale),
            color = Color(COLOR_DISCONNECTED_RED),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun WearScoreboardTable(
    scoreDisplay: WearScoreDisplay,
    userColor: Color,
    opponentColor: Color,
    scoreFontWeight: FontWeight = FontWeight.Bold,
) {
    val scale = screenScale()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // User row
        Row(
            horizontalArrangement = Arrangement.spacedBy((SCOREBOARD_COLUMN_GAP_BASE * scale).dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            scoreDisplay.setHistory.forEach { (userSet, _) ->
                Text(
                    text = "$userSet",
                    color = userColor.copy(alpha = SCOREBOARD_MUTED_ALPHA),
                    fontSize = detailFontSize(scale),
                    fontWeight = scoreFontWeight,
                    fontFamily = FontFamily.Monospace,
                )
            }
            if (!scoreDisplay.isMatchOver) {
                Text(
                    text = "${scoreDisplay.userGames}",
                    color = userColor,
                    fontSize = detailFontSize(scale),
                    fontWeight = scoreFontWeight,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }

        Spacer(modifier = Modifier.height((SCOREBOARD_ROW_GAP_BASE * scale).dp))

        // Opponent row
        Row(
            horizontalArrangement = Arrangement.spacedBy((SCOREBOARD_COLUMN_GAP_BASE * scale).dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            scoreDisplay.setHistory.forEach { (_, oppSet) ->
                Text(
                    text = "$oppSet",
                    color = opponentColor.copy(alpha = SCOREBOARD_MUTED_ALPHA),
                    fontSize = detailFontSize(scale),
                    fontWeight = scoreFontWeight,
                    fontFamily = FontFamily.Monospace,
                )
            }
            if (!scoreDisplay.isMatchOver) {
                Text(
                    text = "${scoreDisplay.opponentGames}",
                    color = opponentColor,
                    fontSize = detailFontSize(scale),
                    fontWeight = scoreFontWeight,
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
    val scale = screenScale()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy((SCORE_GAP_BASE * scale).dp),
    ) {
        PlayerLabel(
            name = scoreDisplay.userName.ifEmpty { stringResource(R.string.default_user_name) },
            isServing = showServing && scoreDisplay.isUserServing,
            color = userColor,
        )
        PlayerLabel(
            name = scoreDisplay.opponentName.ifEmpty { stringResource(R.string.default_opponent_name_short) },
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
    scoreFontWeight: FontWeight = FontWeight.Bold,
) {
    val scale = screenScale()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = scoreDisplay.userScore,
            fontSize = scoreFontSize(scale),
            fontWeight = scoreFontWeight,
            fontFamily = FontFamily.Monospace,
            color = userColor,
        )
        Spacer(modifier = Modifier.size((SCORE_GAP_BASE * scale).dp))
        Text(
            text = scoreDisplay.opponentScore,
            fontSize = scoreFontSize(scale),
            fontWeight = scoreFontWeight,
            fontFamily = FontFamily.Monospace,
            color = opponentColor,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Suppress("FunctionName")
@Composable
private fun TapZones(
    scoreDisplay: WearScoreDisplay,
    onUserScored: () -> Unit,
    onOpponentScored: () -> Unit,
    onUndo: () -> Unit,
) {
    val view = LocalView.current
    val userName = scoreDisplay.userName.ifEmpty { stringResource(R.string.default_user_name) }
    val opponentName = scoreDisplay.opponentName.ifEmpty { stringResource(R.string.default_opponent_name_short) }
    val userDescription = stringResource(R.string.score_point_description, userName)
    val opponentDescription = stringResource(R.string.score_point_description, opponentName)
    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .semantics {
                        contentDescription = userDescription
                    }.combinedClickable(
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
                    .semantics {
                        contentDescription = opponentDescription
                    }.combinedClickable(
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
    val scale = screenScale()
    val dotColor = if (isServing) color else Color.Transparent
    val dot =
        @Composable {
            Box(
                modifier =
                    Modifier
                        .size((SERVING_DOT_SIZE_BASE * scale).dp)
                        .clip(CircleShape)
                        .background(dotColor),
            )
        }
    Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(
                innerPadding(scale),
                if (dotOnRight) Alignment.Start else Alignment.End,
            ),
    ) {
        if (!dotOnRight) dot()
        Text(
            text = name,
            fontSize = labelFontSize(scale),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (dotOnRight) dot()
    }
}

private const val COLOR_DISCONNECTED_RED = 0xFFFF5252
private const val END_MATCH_CONFIRM_TIMEOUT = 3000L
private const val OVERLAY_ALPHA = 0.93f
