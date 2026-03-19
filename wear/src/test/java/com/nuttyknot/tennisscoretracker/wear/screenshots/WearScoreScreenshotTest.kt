package com.nuttyknot.tennisscoretracker.wear.screenshots

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.android.resources.Density
import com.android.resources.ScreenRatio
import com.android.resources.ScreenRound
import com.android.resources.ScreenSize
import com.nuttyknot.tennisscoretracker.shared.WearScoreDisplay
import com.nuttyknot.tennisscoretracker.wear.ui.WearScoreScreen
import com.nuttyknot.tennisscoretracker.wear.ui.WearTheme
import org.junit.Rule
import org.junit.Test

// 2004 Wimbledon Women's Final: Sharapova d. S. Williams 6-1, 6-4
class WearScoreScreenshotTest {
    companion object {
        // Galaxy Watch 7 (44mm): 480x480 px, ~327 PPI, round
        private val GALAXY_WATCH_7 =
            DeviceConfig(
                screenWidth = 480,
                screenHeight = 480,
                density = Density.XHIGH,
                ratio = ScreenRatio.NOTLONG,
                size = ScreenSize.SMALL,
                screenRound = ScreenRound.NOTROUND,
            )
    }

    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = GALAXY_WATCH_7,
            theme = "android:Theme.DeviceDefault",
            renderingMode = SessionParams.RenderingMode.NORMAL,
        )

    @Test
    fun matchOver() {
        // 2004 Wimbledon Women's Final result: Sharapova d. S. Williams 6-1, 6-4
        val score =
            WearScoreDisplay(
                userName = "Sharapova",
                opponentName = "Williams",
                userScore = "0",
                opponentScore = "0",
                userGames = 6,
                opponentGames = 4,
                userSets = 2,
                opponentSets = 0,
                setHistory = listOf(6 to 1, 6 to 4),
                isUserServing = true,
                isMatchOver = true,
                matchWinner = "Sharapova",
            )
        paparazzi.snapshot("matchOver") {
            WearTheme {
                WearScoreScreen(
                    scoreDisplay = score,
                    isConnected = true,
                    onUserScored = {},
                    onOpponentScored = {},
                    onUndo = {},
                )
            }
        }
    }

    @Test
    fun watch() {
        // Match point: Sharapova serving 40-30, 5-4 in 2nd set, won 1st 6-1
        val score =
            WearScoreDisplay(
                userName = "Sharapova",
                opponentName = "Williams",
                userScore = "40",
                opponentScore = "30",
                userGames = 5,
                opponentGames = 4,
                userSets = 1,
                opponentSets = 0,
                setHistory = listOf(6 to 1),
                isUserServing = true,
            )
        paparazzi.snapshot("watch") {
            WearTheme {
                WearScoreScreen(
                    scoreDisplay = score,
                    isConnected = true,
                    onUserScored = {},
                    onOpponentScored = {},
                    onUndo = {},
                )
            }
        }
    }
}
