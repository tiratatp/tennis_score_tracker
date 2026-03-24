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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import com.nuttyknot.tennisscoretracker.shared.R

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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = SCREEN_PADDING,
                        end = SCREEN_PADDING,
                        top = SCREEN_PADDING * 6,
                        bottom = SCREEN_PADDING * 4,
                    ),
        ) {
            Text(
                text = stringResource(R.string.help_how_to_play),
                fontSize = DETAIL_FONT_SIZE,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.weight(1f))
            HelpTapZones(userColor, opponentColor, modifier = Modifier.height(HELP_TAP_ZONE_MAX_HEIGHT))
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.help_long_press_undo),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.help_tap_dismiss),
                fontSize = LABEL_FONT_SIZE,
                color = Color.White.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Suppress("FunctionName", "LongMethod")
@Composable
private fun HelpTapZones(
    userColor: Color,
    opponentColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                        shape = RoundedCornerShape(8.dp),
                    )
                    .padding(horizontal = 4.dp),
        ) {
            Text(
                text = stringResource(R.string.help_tap_left),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = userColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.help_your_point),
                fontSize = 12.sp,
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
                        shape = RoundedCornerShape(8.dp),
                    )
                    .padding(horizontal = 4.dp),
        ) {
            Text(
                text = stringResource(R.string.help_tap_right),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = opponentColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.help_opponent_point),
                fontSize = 12.sp,
                color = opponentColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
