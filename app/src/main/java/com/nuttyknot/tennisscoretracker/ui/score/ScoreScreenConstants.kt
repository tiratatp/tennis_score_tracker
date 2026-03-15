package com.nuttyknot.tennisscoretracker.ui.score

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuttyknot.tennisscoretracker.R

object ScoreScreenConstants {
    private val fontProvider =
        GoogleFont.Provider(
            providerAuthority = "com.google.android.gms.fonts",
            providerPackage = "com.google.android.gms",
            certificates = R.array.com_google_android_gms_fonts_certs,
        )

    private val fontName = GoogleFont("JetBrains Mono")

    val JetBrainsMonoFamily =
        FontFamily(
            Font(googleFont = fontName, fontProvider = fontProvider, weight = FontWeight.ExtraBold),
            Font(googleFont = fontName, fontProvider = fontProvider, weight = FontWeight.Black),
        )

    const val LANDSCAPE_TEXT_SIZE_RATIO = 1.3
    const val PORTRAIT_TEXT_SIZE_RATIO = 1.1
    val MIDDLE_COLUMN_WIDTH = 180.dp
    val STATUS_TEXT_SIZE_LANDSCAPE = 18.sp
    val STATUS_TEXT_SIZE_PORTRAIT = 24.sp
    val NAME_TEXT_SIZE = 24.sp
    const val NAME_ALPHA = 0.7f
    val SERVING_DOT_RADIUS = 8.dp
    val VERTICAL_SPACING_LARGE = 32.dp
    val VERTICAL_SPACING_MEDIUM = 16.dp
    const val PORTRAIT_MAX_SAFE_SIZE_FACTOR = 1.5f
    const val LANDSCAPE_MAX_SAFE_SIZE_FACTOR = 2.5f
}
