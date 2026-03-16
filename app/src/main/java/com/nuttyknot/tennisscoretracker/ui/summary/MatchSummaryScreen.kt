package com.nuttyknot.tennisscoretracker.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuttyknot.tennisscoretracker.ScoreModel
import com.nuttyknot.tennisscoretracker.TennisMatchState
import com.nuttyknot.tennisscoretracker.ui.score.ScoreScreenConstants

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchSummaryScreen(
    scoreModel: ScoreModel,
    onNewMatch: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by scoreModel.matchState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match Summary") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        MatchSummaryContent(
            state = state,
            onNewMatch = onNewMatch,
            paddingValues = paddingValues,
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun MatchSummaryContent(
    state: TennisMatchState,
    onNewMatch: () -> Unit,
    paddingValues: PaddingValues,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = state.matchWinner ?: "",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${state.userSets} - ${state.opponentSets}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = state.setHistory.joinToString("   ") { "${it.first}-${it.second}" },
            fontFamily = ScoreScreenConstants.JetBrainsMonoFamily,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNewMatch,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Text("New Match", fontSize = 18.sp)
        }
    }
}
