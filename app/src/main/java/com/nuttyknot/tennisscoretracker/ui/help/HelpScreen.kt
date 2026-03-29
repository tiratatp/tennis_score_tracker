package com.nuttyknot.tennisscoretracker.ui.help

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuttyknot.tennisscoretracker.R
import com.nuttyknot.tennisscoretracker.ui.AppFooter
import com.nuttyknot.tennisscoretracker.shared.R as SharedR

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onDismiss: () -> Unit) {
    BackHandler { onDismiss() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(SharedR.string.help_title)) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painterResource(R.drawable.ic_arrow_back_24),
                            contentDescription = stringResource(SharedR.string.back),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        HelpScreenContent(
            paddingValues = paddingValues,
            onDismiss = onDismiss,
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun HelpScreenContent(
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    onDismiss: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_info_24),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(SharedR.string.help_welcome),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(24.dp))

        ScoringGestures()

        HardwareButtons()

        MatchFormat()

        VoiceAnnouncements()

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Text(stringResource(SharedR.string.help_got_it), fontSize = 18.sp)
        }

        AppFooter()
    }
}

@Suppress("FunctionName")
@Composable
private fun ScoringGestures() {
    HelpSection(
        title = stringResource(SharedR.string.help_scoring_gestures),
        description = stringResource(SharedR.string.help_scoring_gestures_desc),
    )

    GestureItem(
        icon = R.drawable.ic_keyboard_arrow_up_24,
        label = stringResource(SharedR.string.help_tap_your_side),
        action = stringResource(SharedR.string.help_award_your_point),
        description = stringResource(SharedR.string.help_tap_your_side_desc),
    )

    GestureItem(
        icon = R.drawable.ic_keyboard_arrow_up_24,
        label = stringResource(SharedR.string.help_tap_opponent_side),
        action = stringResource(SharedR.string.help_award_opponent_point),
        description = stringResource(SharedR.string.help_tap_opponent_desc),
    )

    GestureItem(
        icon = R.drawable.ic_refresh_24,
        label = stringResource(SharedR.string.help_long_press),
        action = stringResource(SharedR.string.help_undo),
        description = stringResource(SharedR.string.help_long_press_desc),
    )

    Spacer(modifier = Modifier.height(16.dp))
}

@Suppress("FunctionName")
@Composable
private fun HardwareButtons() {
    HelpSection(
        title = stringResource(SharedR.string.help_hardware_buttons),
        description = stringResource(SharedR.string.help_hardware_buttons_desc),
    )

    InfoRow(
        text = stringResource(SharedR.string.help_hardware_button_actions),
    )

    InfoRow(
        text = stringResource(SharedR.string.help_hardware_button_types),
    )

    Text(
        text = stringResource(SharedR.string.help_hardware_button_keycode),
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Suppress("FunctionName")
@Composable
private fun MatchFormat() {
    HelpSection(
        title = stringResource(SharedR.string.help_match_format),
        description = stringResource(SharedR.string.help_match_format_desc),
    )

    InfoRow(text = stringResource(SharedR.string.help_best_of_3))
    InfoRow(text = stringResource(SharedR.string.help_tiebreak))

    Spacer(modifier = Modifier.height(24.dp))
}

@Suppress("FunctionName")
@Composable
private fun VoiceAnnouncements() {
    HelpSection(
        title = stringResource(SharedR.string.help_voice_announcements),
        description = stringResource(SharedR.string.help_voice_announcements_desc),
    )

    InfoRow(text = stringResource(SharedR.string.help_volume_tip))

    Spacer(modifier = Modifier.height(24.dp))
}

@Suppress("FunctionName")
@Composable
private fun InfoRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_info_24),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.secondary,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Suppress("FunctionName")
@Composable
fun HelpSection(
    title: String,
    description: String,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Suppress("FunctionName")
@Composable
fun GestureItem(
    icon: Int,
    label: String,
    action: String,
    description: String,
    isDouble: Boolean = false,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(80.dp),
        ) {
            Row {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.secondary,
                )
                if (isDouble) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = action,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
