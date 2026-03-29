package com.nuttyknot.tennisscoretracker.ui.summary

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.nuttyknot.tennisscoretracker.MatchState
import com.nuttyknot.tennisscoretracker.R
import com.nuttyknot.tennisscoretracker.shared.R as SharedR

@Suppress("FunctionName")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MatchSummaryPreview(state: MatchState) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(SharedR.string.match_summary_title)) },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            painterResource(R.drawable.ic_arrow_back_24),
                            contentDescription = stringResource(SharedR.string.back),
                        )
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
