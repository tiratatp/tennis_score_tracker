package com.nuttyknot.tennisscoretracker.ui.summary

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nuttyknot.tennisscoretracker.TennisMatchState
import com.nuttyknot.tennisscoretracker.shared.R

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MatchSummaryPreview(state: TennisMatchState) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.match_summary_title)) },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
            onNewMatch = {},
            paddingValues = paddingValues,
        )
    }
}
