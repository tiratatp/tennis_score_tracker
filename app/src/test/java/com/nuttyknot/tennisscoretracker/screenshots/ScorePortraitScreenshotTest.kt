package com.nuttyknot.tennisscoretracker.screenshots

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.nuttyknot.tennisscoretracker.AppTheme
import com.nuttyknot.tennisscoretracker.PlayerScore
import com.nuttyknot.tennisscoretracker.TennisMatchState
import com.nuttyknot.tennisscoretracker.ui.score.ScoreScreenPreview
import com.nuttyknot.tennisscoretracker.ui.theme.TennisScoreTrackerTheme
import org.junit.Rule
import org.junit.Test

// 2008 Wimbledon Men's Final: Nadal d. Federer 6-4, 6-4, 6-7, 6-7, 9-7
class ScorePortraitScreenshotTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.NEXUS_5,
            theme = "android:Theme.Material.NoActionBar.Fullscreen",
            renderingMode = SessionParams.RenderingMode.NORMAL,
            useDeviceResolution = true,
        )

    @Test
    fun matchOver() {
        // 2008 Wimbledon Men's Final result: Nadal d. Federer 6-4, 6-4, 6-7, 6-7, 9-7
        val state =
            TennisMatchState(
                userScore = PlayerScore.Love,
                opponentScore = PlayerScore.Love,
                userGames = 9,
                opponentGames = 7,
                userSets = 3,
                opponentSets = 2,
                setHistory = listOf(6 to 4, 6 to 4, 6 to 7, 6 to 7, 9 to 7),
                isUserServing = true,
                userName = "Nadal",
                opponentName = "Federer",
                matchWinner = "Nadal",
            )
        paparazzi.snapshot("matchOver") {
            TennisScoreTrackerTheme(appTheme = AppTheme.SKY_BLUE) {
                ScoreScreenPreview(state)
            }
        }
    }

    @Test
    fun inMatch() {
        val state =
            TennisMatchState(
                userScore = PlayerScore.Forty,
                opponentScore = PlayerScore.Fifteen,
                userGames = 9,
                opponentGames = 7,
                userSets = 2,
                opponentSets = 2,
                setHistory = listOf(6 to 4, 6 to 4, 6 to 7, 6 to 7),
                isUserServing = true,
                userName = "Nadal",
                opponentName = "Federer",
            )
        paparazzi.snapshot("inMatch") {
            TennisScoreTrackerTheme(appTheme = AppTheme.SKY_BLUE) {
                ScoreScreenPreview(state)
            }
        }
    }
}
