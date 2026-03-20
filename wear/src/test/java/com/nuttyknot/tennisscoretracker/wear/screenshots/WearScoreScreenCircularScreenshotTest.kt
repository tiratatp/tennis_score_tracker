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

// Circular variants for UX/internal testing on round watch displays
class WearScoreScreenCircularScreenshotTest {
    companion object {
        // Galaxy Watch 7 (44mm): 480x480 px, ~327 PPI, round
        private val GALAXY_WATCH_7_ROUND =
            DeviceConfig(
                screenWidth = 480,
                screenHeight = 480,
                density = Density.XHIGH,
                ratio = ScreenRatio.NOTLONG,
                size = ScreenSize.SMALL,
                screenRound = ScreenRound.ROUND,
            )
    }

    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = GALAXY_WATCH_7_ROUND,
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
        paparazzi.snapshot("matchOverCircular") {
            WearTheme {
                WearScoreScreen(
                    scoreDisplay = score,
                    isConnected = true,
                    currentTime = "2:30 PM",
                    onNewMatch = {},
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
        paparazzi.snapshot("watchCircular") {
            WearTheme {
                WearScoreScreen(
                    scoreDisplay = score,
                    isConnected = true,
                    currentTime = "2:30 PM",
                    onNewMatch = {},
                    onUserScored = {},
                    onOpponentScored = {},
                    onUndo = {},
                )
            }
        }
    }

    @Test
    fun watchOpponentServing() {
        // Same scenario but Williams serving (40-30, 5-4 in 2nd set)
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
                isUserServing = false,
            )
        paparazzi.snapshot("watchOpponentServingCircular") {
            WearTheme {
                WearScoreScreen(
                    scoreDisplay = score,
                    isConnected = true,
                    currentTime = "2:30 PM",
                    onNewMatch = {},
                    onUserScored = {},
                    onOpponentScored = {},
                    onUndo = {},
                )
            }
        }
    }

    @Test
    fun watchLongNames() {
        // Long hyphenated names to verify ellipsis behavior
        val score =
            WearScoreDisplay(
                userName = "Sharapova-Williams",
                opponentName = "Alexandrova-Kuznetsova",
                userScore = "40",
                opponentScore = "30",
                userGames = 5,
                opponentGames = 4,
                userSets = 1,
                opponentSets = 0,
                setHistory = listOf(6 to 1),
                isUserServing = true,
            )
        paparazzi.snapshot("watchLongNamesCircular") {
            WearTheme {
                WearScoreScreen(
                    scoreDisplay = score,
                    isConnected = true,
                    currentTime = "2:30 PM",
                    onNewMatch = {},
                    onUserScored = {},
                    onOpponentScored = {},
                    onUndo = {},
                )
            }
        }
    }

    @Test
    fun watchLongNamesOpponentServing() {
        // Long names with opponent serving to verify dot placement on right side
        val score =
            WearScoreDisplay(
                userName = "Sharapova-Williams",
                opponentName = "Alexandrova-Kuznetsova",
                userScore = "40",
                opponentScore = "30",
                userGames = 5,
                opponentGames = 4,
                userSets = 1,
                opponentSets = 0,
                setHistory = listOf(6 to 1),
                isUserServing = false,
            )
        paparazzi.snapshot("watchLongNamesOpponentServingCircular") {
            WearTheme {
                WearScoreScreen(
                    scoreDisplay = score,
                    isConnected = true,
                    currentTime = "2:30 PM",
                    onNewMatch = {},
                    onUserScored = {},
                    onOpponentScored = {},
                    onUndo = {},
                )
            }
        }
    }
}
