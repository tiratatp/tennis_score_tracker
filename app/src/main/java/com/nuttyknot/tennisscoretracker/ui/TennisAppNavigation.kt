package com.nuttyknot.tennisscoretracker.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nuttyknot.tennisscoretracker.ScoreManager
import com.nuttyknot.tennisscoretracker.SettingsManager

object Routes {
    const val SCORE_SCREEN = "score"
    const val SETTINGS_SCREEN = "settings"
}

@Composable
fun TennisAppNavigation(
    navController: NavHostController = rememberNavController(),
    scoreManager: ScoreManager,
    settingsManager: SettingsManager
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SCORE_SCREEN
    ) {
        composable(Routes.SCORE_SCREEN) {
            ScoreScreen(
                scoreManager = scoreManager,
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS_SCREEN) }
            )
        }
        composable(Routes.SETTINGS_SCREEN) {
            SettingsScreen(
                settingsManager = settingsManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
