package com.nuttyknot.tennisscoretracker.wear.screenshots

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.android.resources.Density
import com.android.resources.ScreenRatio
import com.android.resources.ScreenRound
import com.android.resources.ScreenSize
import com.nuttyknot.tennisscoretracker.wear.ui.DEFAULT_PRIMARY_COLOR
import com.nuttyknot.tennisscoretracker.wear.ui.DEFAULT_SECONDARY_COLOR
import com.nuttyknot.tennisscoretracker.wear.ui.HelpDialogContent
import com.nuttyknot.tennisscoretracker.wear.ui.WearTheme
import org.junit.Rule
import org.junit.Test

class WearHelpScreenshotTest {
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
    fun help() {
        paparazzi.snapshot("help") {
            WearTheme {
                HelpDialogContent(
                    userColor = DEFAULT_PRIMARY_COLOR,
                    opponentColor = DEFAULT_SECONDARY_COLOR,
                )
            }
        }
    }
}
