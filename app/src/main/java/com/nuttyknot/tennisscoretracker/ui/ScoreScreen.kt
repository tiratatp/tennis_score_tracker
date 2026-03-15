package com.nuttyknot.tennisscoretracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuttyknot.tennisscoretracker.ScoreManager
import com.nuttyknot.tennisscoretracker.ui.theme.Black
import com.nuttyknot.tennisscoretracker.ui.theme.White
import com.nuttyknot.tennisscoretracker.ui.theme.Yellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreScreen(
    scoreManager: ScoreManager,
    onNavigateToSettings: () -> Unit
) {
    val state by scoreManager.matchState.collectAsState()
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tennis Score Tracker", color = White) },
                actions = {
                    IconButton(onClick = { scoreManager.reset() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = Yellow
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Yellow
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Black)
            )
        },
        containerColor = Black
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            val gameStatus = if (state.isDeuce) {
                "DEUCE"
            } else {
                "Sets: ${state.userSets} - ${state.opponentSets}  |  Games: ${state.userGames} - ${state.opponentGames}"
            }

            if (isLandscape) {
                // Landscape Layout: Side-by-Side
                val mainTextSize = (maxHeight.value / 1.1).sp
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Opponent Score (Left)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "OPPONENT", color = White.copy(alpha = 0.7f), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(text = state.opponentScore.display, color = White, fontSize = mainTextSize, fontWeight = FontWeight.ExtraBold)
                    }

                    // Game Status (Middle)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(180.dp)
                    ) {
                        Text(text = gameStatus, color = Yellow, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }

                    // User Score (Right)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = state.userScore.display, color = Yellow, fontSize = mainTextSize, fontWeight = FontWeight.ExtraBold)
                        Text(text = "YOU", color = Yellow.copy(alpha = 0.7f), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Portrait Layout: Vertical Stack
                val mainTextSize = (maxHeight.value / 3.5).sp
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Opponent Score
                    Text(text = "OPPONENT", color = White.copy(alpha = 0.7f), fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text(text = state.opponentScore.display, color = White, fontSize = mainTextSize, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)

                    Spacer(modifier = Modifier.height(32.dp))

                    // Game Status
                    Text(text = gameStatus, color = Yellow, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

                    Spacer(modifier = Modifier.height(16.dp))

                    // User Score
                    Text(text = state.userScore.display, color = Yellow, fontSize = mainTextSize, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                    Text(text = "YOU", color = Yellow.copy(alpha = 0.7f), fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
        }
    }
}
