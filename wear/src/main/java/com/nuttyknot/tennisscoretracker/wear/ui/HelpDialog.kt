package com.nuttyknot.tennisscoretracker.wear.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.AlertDialogDefaults
import androidx.wear.compose.material3.Text
import com.nuttyknot.tennisscoretracker.shared.R

private const val HELP_CORNER_RADIUS_BASE = 8f

@Suppress("FunctionName")
@Composable
internal fun HelpDialog(
    show: Boolean,
    userColor: Color,
    opponentColor: Color,
    onDismiss: () -> Unit,
) {
    val scale = screenScale()
    AlertDialog(
        visible = show,
        onDismissRequest = onDismiss,
        edgeButton = {
            AlertDialogDefaults.EdgeButton(onClick = onDismiss)
        },
        title = { Text(stringResource(R.string.help_how_to_play)) },
    ) {
        item {
            HelpTapZones(
                userColor = userColor,
                opponentColor = opponentColor,
                scale = scale,
                modifier = Modifier.heightIn(min = helpTapZoneMaxHeight(scale)),
            )
        }
        item {
            Text(
                text = stringResource(R.string.help_long_press_undo),
                fontSize = labelFontSize(scale),
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Renders dialog content in a plain Column — for Paparazzi screenshot tests. */
@Suppress("FunctionName")
@Composable
internal fun HelpDialogContent(
    userColor: Color,
    opponentColor: Color,
) {
    val scale = screenScale()
    val isRound = LocalConfiguration.current.isScreenRound
    val hPadding = if (isRound) roundHorizontalPadding(scale) else screenPadding(scale)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(horizontal = hPadding, vertical = screenPadding(scale)),
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.help_how_to_play),
            fontSize = detailFontSize(scale),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.weight(1f))
        HelpTapZones(
            userColor = userColor,
            opponentColor = opponentColor,
            scale = scale,
            modifier = Modifier.heightIn(min = helpTapZoneMaxHeight(scale)),
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.help_long_press_undo),
            fontSize = labelFontSize(scale),
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.weight(1f))
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
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
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
