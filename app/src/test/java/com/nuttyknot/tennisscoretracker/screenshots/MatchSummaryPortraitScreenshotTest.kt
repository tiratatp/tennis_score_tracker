package com.nuttyknot.tennisscoretracker.screenshots

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.nuttyknot.tennisscoretracker.AppTheme
import com.nuttyknot.tennisscoretracker.PlayerScore
import com.nuttyknot.tennisscoretracker.TennisMatchState
import com.nuttyknot.tennisscoretracker.ui.summary.MatchSummaryPreview
import com.nuttyknot.tennisscoretracker.ui.theme.TennisScoreTrackerTheme
import org.junit.Rule
import org.junit.Test

// 2022 Australian Open Final: Nadal d. Medvedev 2-6, 6-7, 6-4, 6-4, 7-5
class MatchSummaryPortraitScreenshotTest {
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
        // 2022 Australian Open Final result: Nadal d. Medvedev 2-6, 6-7, 6-4, 6-4, 7-5
        val state =
            TennisMatchState(
                userScore = PlayerScore.Love,
                opponentScore = PlayerScore.Love,
                userGames = 7,
                opponentGames = 5,
                userSets = 3,
                opponentSets = 2,
                setHistory = listOf(2 to 6, 6 to 7, 6 to 4, 6 to 4, 7 to 5),
                isUserServing = true,
                userName = "Nadal",
                opponentName = "Medvedev",
                matchWinner = "Nadal",
            )
        paparazzi.snapshot("matchOver") {
            TennisScoreTrackerTheme(appTheme = AppTheme.SKY_BLUE) {
                MatchSummaryPreview(state)
            }
        }
    }
}
