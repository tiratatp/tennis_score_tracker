package com.nuttyknot.tennisscoretracker.ui.score

import android.content.res.Configuration
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.nuttyknot.tennisscoretracker.TennisMatchState

@Suppress("FunctionName")
@Composable
internal fun ScoreScreenPreview(state: TennisMatchState) {
    Scaffold(
        topBar = {
            ScoreTopBar(
                onNavigateToHelp = {},
                onNavigateToSettings = {},
                onResetClick = {},
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(
                        horizontal = 16.dp,
                        vertical = if (isLandscape) 0.dp else 16.dp,
                    ),
        ) {
            if (isLandscape) {
                LandscapeScoreContent(state, maxHeight.value, maxWidth.value) {}
            } else {
                PortraitScoreContent(state, maxHeight.value, maxWidth.value) {}
            }
        }
    }
}
