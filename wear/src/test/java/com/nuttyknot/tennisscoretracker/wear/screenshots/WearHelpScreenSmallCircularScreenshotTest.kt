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

// Small circular screen test to verify help dialog text doesn't get cut off
class WearHelpScreenSmallCircularScreenshotTest {
    companion object {
        // Pixel Watch: 384x384 px, XHIGH density -> 192dp, round
        private val PIXEL_WATCH_ROUND =
            DeviceConfig(
                screenWidth = 384,
                screenHeight = 384,
                density = Density.XHIGH,
                ratio = ScreenRatio.NOTLONG,
                size = ScreenSize.SMALL,
                screenRound = ScreenRound.ROUND,
                softButtons = false,
            )
    }

    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = PIXEL_WATCH_ROUND,
            theme = "android:Theme.DeviceDefault",
            renderingMode = SessionParams.RenderingMode.NORMAL,
        )

    @Test
    fun help() {
        paparazzi.snapshot("helpSmallCircular") {
            WearTheme {
                HelpDialogContent(
                    userColor = DEFAULT_PRIMARY_COLOR,
                    opponentColor = DEFAULT_SECONDARY_COLOR,
                )
            }
        }
    }
}
