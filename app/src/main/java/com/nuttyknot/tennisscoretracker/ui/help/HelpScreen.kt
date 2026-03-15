package com.nuttyknot.tennisscoretracker.ui.help

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onDismiss: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & Instructions") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome to TennisDroid",
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
            Text("Got it", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "made with ♥ by NuttyKnot",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 8.dp),
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun ScoringGestures() {
    HelpSection(
        title = "Scoring Gestures",
        description =
            "These gestures work both on-screen (tap) and with a hardware/Bluetooth button.",
    )

    GestureItem(
        icon = Icons.Default.KeyboardArrowUp,
        label = "Tap / Click",
        action = "Increase User Score",
        description = "Tap once to give yourself a point.",
    )

    GestureItem(
        icon = Icons.Default.KeyboardArrowUp,
        label = "Double Tap / Click",
        action = "Increase Opponent Score",
        description = "Double tap to give the opponent a point.",
        isDouble = true,
    )

    GestureItem(
        icon = Icons.Default.Refresh,
        label = "Long Press",
        action = "Undo",
        description = "Hold down to undo the last action.",
    )

    Spacer(modifier = Modifier.height(16.dp))
}

@Suppress("FunctionName")
@Composable
private fun HardwareButtons() {
    HelpSection(
        title = "Hardware Buttons",
        description = "Use a Bluetooth remote so you can score without touching the screen.",
    )

    InfoRow(
        text = "Volume Down or Bluetooth Camera Shutter button can be used.",
    )

    Text(
        text = "If your button doesn't work, change the Target KeyCode in Settings.",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Suppress("FunctionName")
@Composable
private fun MatchFormat() {
    HelpSection(
        title = "Match Format",
        description = "Standard tennis rules are applied automatically.",
    )

    InfoRow(text = "Best of 3 sets.")
    InfoRow(text = "Tiebreak is played at 6-6 in each set.")

    Spacer(modifier = Modifier.height(24.dp))
}

@Suppress("FunctionName")
@Composable
private fun VoiceAnnouncements() {
    HelpSection(
        title = "Voice Announcements",
        description = "Scores are announced aloud after each point.",
    )

    InfoRow(text = "Turn up the volume or pair a Bluetooth speaker for best results.")

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
            imageVector = Icons.Default.Info,
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
    icon: ImageVector,
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
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.secondary,
                )
                if (isDouble) {
                    Icon(
                        imageVector = icon,
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
