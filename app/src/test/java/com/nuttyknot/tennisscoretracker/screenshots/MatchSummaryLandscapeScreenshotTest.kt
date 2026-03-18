package com.nuttyknot.tennisscoretracker.screenshots

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.android.resources.ScreenOrientation
import com.nuttyknot.tennisscoretracker.AppTheme
import com.nuttyknot.tennisscoretracker.PlayerScore
import com.nuttyknot.tennisscoretracker.TennisMatchState
import com.nuttyknot.tennisscoretracker.ui.summary.MatchSummaryPreview
import com.nuttyknot.tennisscoretracker.ui.theme.TennisScoreTrackerTheme
import org.junit.Rule
import org.junit.Test

// 2001 Wimbledon Final: Venus d. Henin 6-1, 3-6, 6-0
class MatchSummaryLandscapeScreenshotTest {
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
        // 2001 Wimbledon Final result: Venus d. Henin 6-1, 3-6, 6-0
        val state =
            TennisMatchState(
                userScore = PlayerScore.Love,
                opponentScore = PlayerScore.Love,
                userGames = 6,
                opponentGames = 0,
                userSets = 2,
                opponentSets = 1,
                setHistory = listOf(6 to 1, 3 to 6, 6 to 0),
                isUserServing = true,
                userName = "Venus",
                opponentName = "Henin",
                matchWinner = "Venus",
            )
        paparazzi.snapshot("matchOver") {
            TennisScoreTrackerTheme(appTheme = AppTheme.SKY_BLUE) {
                MatchSummaryPreview(state)
            }
        }
    }
}
