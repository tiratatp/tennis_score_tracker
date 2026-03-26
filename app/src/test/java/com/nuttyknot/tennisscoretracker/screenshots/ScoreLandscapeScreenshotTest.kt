package com.nuttyknot.tennisscoretracker.screenshots

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.android.resources.ScreenOrientation
import com.nuttyknot.tennisscoretracker.AppTheme
import com.nuttyknot.tennisscoretracker.MatchState
import com.nuttyknot.tennisscoretracker.PlayerScore
import com.nuttyknot.tennisscoretracker.ui.score.ScoreScreenPreview
import com.nuttyknot.tennisscoretracker.ui.theme.TennisScoreTrackerTheme
import org.junit.Rule
import org.junit.Test

// 2003 Wimbledon Final: Serena d. Venus Williams 4-6, 6-4, 6-2
class ScoreLandscapeScreenshotTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.NEXUS_5.copy(orientation = ScreenOrientation.LANDSCAPE),
            theme = "android:Theme.Material.NoActionBar.Fullscreen",
            renderingMode = SessionParams.RenderingMode.NORMAL,
            useDeviceResolution = true,
        )

    @Test
    fun matchOver() {
        // 2003 Wimbledon Final result: Serena d. Venus 4-6, 6-4, 6-2
        val state =
            MatchState(
                userScore = PlayerScore.Love,
                opponentScore = PlayerScore.Love,
                userGames = 6,
                opponentGames = 2,
                userSets = 2,
                opponentSets = 1,
                setHistory = listOf(4 to 6, 6 to 4, 6 to 2),
                isUserServing = true,
                userName = "Serena",
                opponentName = "Venus",
                matchWinner = "Serena",
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
            MatchState(
                userScore = PlayerScore.Forty,
                opponentScore = PlayerScore.Love,
                userGames = 6,
                opponentGames = 2,
                userSets = 1,
                opponentSets = 1,
                setHistory = listOf(4 to 6, 6 to 4),
                isUserServing = true,
                userName = "Serena",
                opponentName = "Venus",
            )
        paparazzi.snapshot("inMatch") {
            TennisScoreTrackerTheme(appTheme = AppTheme.SKY_BLUE) {
                ScoreScreenPreview(state)
            }
        }
    }
}
