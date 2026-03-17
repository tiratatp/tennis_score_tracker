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
            deviceConfig = DeviceConfig.PIXEL_6,
            theme = "android:Theme.Material.NoActionBar.Fullscreen",
            renderingMode = SessionParams.RenderingMode.NORMAL,
        )

    @Test
    fun matchOver() {
        // 2004 Wimbledon Women's Final result: Sharapova d. S. Williams 6-1, 6-4
        val state =
            TennisMatchState(
                userScore = PlayerScore.Love,
                opponentScore = PlayerScore.Love,
                userGames = 6,
                opponentGames = 4,
                userSets = 2,
                opponentSets = 0,
                setHistory = listOf(6 to 1, 6 to 4),
                isUserServing = true,
                userName = "Sharapova",
                opponentName = "Williams",
                matchWinner = "Sharapova",
            )
        paparazzi.snapshot("matchOver") {
            TennisScoreTrackerTheme(appTheme = AppTheme.SKY_BLUE) {
                ScoreScreenPreview(state)
            }
        }
    }

    @Test
    fun portrait() {
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
        paparazzi.snapshot("portrait") {
            TennisScoreTrackerTheme(appTheme = AppTheme.SKY_BLUE) {
                ScoreScreenPreview(state)
            }
        }
    }
}
