package com.nuttyknot.tennisscoretracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nuttyknot.tennisscoretracker.ScoreModel
import com.nuttyknot.tennisscoretracker.SettingsManager
import com.nuttyknot.tennisscoretracker.ui.help.HelpScreen
import com.nuttyknot.tennisscoretracker.ui.score.ScoreScreen
import com.nuttyknot.tennisscoretracker.ui.settings.SettingsScreen
import com.nuttyknot.tennisscoretracker.ui.summary.MatchSummaryScreen
import kotlinx.coroutines.launch

@Suppress("FunctionName")
@Composable
fun TennisAppNavigation(
    navController: NavHostController = rememberNavController(),
    scoreModel: ScoreModel,
    settingsManager: SettingsManager,
    onRouteChange: (String) -> Unit = {},
) {
    val hasSeenHelp by settingsManager.hasSeenHelpFlow.collectAsState(initial = true)
    val matchState by scoreModel.matchState.collectAsState()
    val scope = rememberCoroutineScope()
    var hasNavigatedToSummary by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { entry ->
            entry.destination.route?.let { onRouteChange(it) }
        }
    }

    LaunchedEffect(hasSeenHelp) {
        if (!hasSeenHelp) {
            navController.navigate(Routes.HELP_SCREEN) {
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(matchState.matchWinner) {
        if (matchState.matchWinner != null && !hasNavigatedToSummary) {
            hasNavigatedToSummary = true
            navController.navigate(Routes.MATCH_SUMMARY_SCREEN) {
                launchSingleTop = true
            }
        } else if (matchState.matchWinner == null) {
            hasNavigatedToSummary = false
        }
    }

    TennisNavHost(navController, scoreModel, settingsManager, scope)
}

@Suppress("FunctionName")
@Composable
private fun TennisNavHost(
    navController: NavHostController,
    scoreModel: ScoreModel,
    settingsManager: SettingsManager,
    scope: kotlinx.coroutines.CoroutineScope,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SCORE_SCREEN,
    ) {
        composable(Routes.SCORE_SCREEN) {
            ScoreScreen(
                scoreModel = scoreModel,
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS_SCREEN) },
                onNavigateToHelp = { navController.navigate(Routes.HELP_SCREEN) },
                onNavigateToSummary = {
                    navController.navigate(Routes.MATCH_SUMMARY_SCREEN) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(Routes.SETTINGS_SCREEN) {
            SettingsScreen(
                scoreModel = scoreModel,
                settingsManager = settingsManager,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(Routes.HELP_SCREEN) {
            HelpScreen(
                onDismiss = {
                    scope.launch {
                        settingsManager.updateHasSeenHelp(true)
                    }
                    navController.popBackStack()
                },
            )
        }
        composable(Routes.MATCH_SUMMARY_SCREEN) {
            MatchSummaryScreen(
                scoreModel = scoreModel,
                onNewMatch = {
                    scoreModel.reset()
                    navController.popBackStack(Routes.SCORE_SCREEN, inclusive = false)
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
