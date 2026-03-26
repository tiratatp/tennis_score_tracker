package com.nuttyknot.tennisscoretracker.screenshots

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.android.resources.Density
import com.android.resources.ScreenOrientation
import com.nuttyknot.tennisscoretracker.AppTheme
import com.nuttyknot.tennisscoretracker.MatchState
import com.nuttyknot.tennisscoretracker.PlayerScore
import com.nuttyknot.tennisscoretracker.ui.score.ScoreScreenPreview
import com.nuttyknot.tennisscoretracker.ui.theme.TennisScoreTrackerTheme
import org.junit.Rule
import org.junit.Test

// 2017 Australian Open Final: Federer d. Nadal 6-4, 3-6, 6-1, 3-6, 6-3
class ScoreTablet7ScreenshotTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig =
                DeviceConfig.NEXUS_7.copy(
                    screenWidth = 1080,
                    screenHeight = 1920,
                    density = Density.XHIGH,
                    orientation = ScreenOrientation.LANDSCAPE,
                ),
            theme = "android:Theme.Material.NoActionBar.Fullscreen",
            renderingMode = SessionParams.RenderingMode.NORMAL,
            useDeviceResolution = true,
        )

    @Test
    fun inmatch() {
        // 5th set, Federer serving at 40-30, games 5-3
        val state =
            MatchState(
                userScore = PlayerScore.Forty,
                opponentScore = PlayerScore.Thirty,
                userGames = 5,
                opponentGames = 3,
                userSets = 2,
                opponentSets = 2,
                setHistory = listOf(6 to 4, 3 to 6, 6 to 1, 3 to 6),
                isUserServing = true,
                userName = "Federer",
                opponentName = "Nadal",
            )
        paparazzi.snapshot("inmatch") {
            TennisScoreTrackerTheme(appTheme = AppTheme.GRAND_SLAM) {
                ScoreScreenPreview(state)
            }
        }
    }
}
