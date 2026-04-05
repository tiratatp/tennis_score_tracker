package com.nuttyknot.tennisscoretracker.wear.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Text
import com.nuttyknot.tennisscoretracker.shared.R

private const val HELP_CORNER_RADIUS_BASE = 8f
private const val HELP_DISMISS_SPACER_MULTIPLIER = 4
private const val HELP_TOP_PADDING_ROUND_MULTIPLIER = 6
private const val HELP_TOP_PADDING_SQUARE_MULTIPLIER = 3
private const val HELP_BOTTOM_PADDING_ROUND_MULTIPLIER = 4
private const val HELP_BOTTOM_PADDING_SQUARE_MULTIPLIER = 2

@Suppress("FunctionName")
@Composable
internal fun WearHelpOverlay(
    userColor: Color,
    opponentColor: Color,
    onDismiss: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.93f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss,
                ),
    ) {
        HelpContent(userColor, opponentColor)
    }
}

@Suppress("FunctionName")
@Composable
private fun HelpContent(
    userColor: Color,
    opponentColor: Color,
) {
    val scale = screenScale()
    val isRound = LocalConfiguration.current.isScreenRound
    val topPadding =
        if (isRound) {
            screenPadding(scale) * HELP_TOP_PADDING_ROUND_MULTIPLIER
        } else {
            screenPadding(scale) * HELP_TOP_PADDING_SQUARE_MULTIPLIER
        }
    val bottomPadding =
        if (isRound) {
            screenPadding(scale) * HELP_BOTTOM_PADDING_ROUND_MULTIPLIER
        } else {
            screenPadding(scale) * HELP_BOTTOM_PADDING_SQUARE_MULTIPLIER
        }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    start = screenPadding(scale),
                    end = screenPadding(scale),
                    top = topPadding,
                    bottom = bottomPadding,
                ),
    ) {
        Text(
            text = stringResource(R.string.help_how_to_play),
            fontSize = detailFontSize(scale),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.weight(1f))
        HelpTapZones(
            userColor,
            opponentColor,
            scale,
            modifier = Modifier.height(helpTapZoneMaxHeight(scale)),
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.help_long_press_undo),
            fontSize = labelFontSize(scale),
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(spacerHeight(scale) * HELP_DISMISS_SPACER_MULTIPLIER))
        Text(
            text = stringResource(R.string.help_tap_dismiss),
            fontSize = labelFontSize(scale),
            color = Color.White.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
        )
    }
}

@Suppress("FunctionName", "LongMethod")
@Composable
private fun HelpTapZones(
    userColor: Color,
    opponentColor: Color,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(innerPadding(scale)),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = userColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape((HELP_CORNER_RADIUS_BASE * scale).dp),
                    ).padding(horizontal = innerPadding(scale)),
        ) {
            Text(
                text = stringResource(R.string.help_tap_left),
                fontSize = labelFontSize(scale),
                fontWeight = FontWeight.Bold,
                color = userColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.help_your_point),
                fontSize = labelFontSize(scale),
                color = userColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = opponentColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape((HELP_CORNER_RADIUS_BASE * scale).dp),
                    ).padding(horizontal = innerPadding(scale)),
        ) {
            Text(
                text = stringResource(R.string.help_tap_right),
                fontSize = labelFontSize(scale),
                fontWeight = FontWeight.Bold,
                color = opponentColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.help_opponent_point),
                fontSize = labelFontSize(scale),
                color = opponentColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
