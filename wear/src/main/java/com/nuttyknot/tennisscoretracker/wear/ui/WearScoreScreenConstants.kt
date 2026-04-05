package com.nuttyknot.tennisscoretracker.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val REFERENCE_SCREEN_WIDTH_DP = 240f
private const val MIN_SCALE = 0.75f
private const val MAX_SCALE = 1.2f
private const val SCORE_FONT_SIZE_BASE = 40f
private const val DETAIL_FONT_SIZE_BASE = 14f
private const val LABEL_FONT_SIZE_BASE = 12f
private const val SCREEN_PADDING_BASE = 8f
private const val INNER_PADDING_BASE = 4f
private const val SPACER_HEIGHT_BASE = 2f
private const val HELP_TAP_ZONE_MAX_HEIGHT_BASE = 72f
private const val ROUND_HORIZONTAL_PADDING_BASE = 14f
private const val MIN_FONT_SIZE_SP = 10f

@Composable
internal fun screenScale(): Float {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.toFloat()
    return (screenWidthDp / REFERENCE_SCREEN_WIDTH_DP).coerceIn(MIN_SCALE, MAX_SCALE)
}

internal fun scoreFontSize(scale: Float): TextUnit = (SCORE_FONT_SIZE_BASE * scale).sp

internal fun detailFontSize(scale: Float): TextUnit = (DETAIL_FONT_SIZE_BASE * scale).sp

internal fun labelFontSize(scale: Float): TextUnit = maxOf(LABEL_FONT_SIZE_BASE * scale, MIN_FONT_SIZE_SP).sp

internal fun screenPadding(scale: Float): Dp = (SCREEN_PADDING_BASE * scale).dp

internal fun innerPadding(scale: Float): Dp = (INNER_PADDING_BASE * scale).dp

internal fun spacerHeight(scale: Float): Dp = (SPACER_HEIGHT_BASE * scale).dp

internal fun helpTapZoneMaxHeight(scale: Float): Dp = (HELP_TAP_ZONE_MAX_HEIGHT_BASE * scale).dp

internal fun roundHorizontalPadding(scale: Float): Dp = (ROUND_HORIZONTAL_PADDING_BASE * scale).dp

internal const val SCOREBOARD_MUTED_ALPHA = 0.5f
internal const val TIME_TEXT_ALPHA = 0.35f
internal const val COLOR_SKY_BLUE = 0xFF38BDF8
internal const val COLOR_TENNIS_GREEN = 0xFF4CAF50
internal val DEFAULT_PRIMARY_COLOR = Color(COLOR_SKY_BLUE)
internal val DEFAULT_SECONDARY_COLOR = Color.White
internal const val TOP_SECTION_WEIGHT = 0.55f
internal const val BOTTOM_SECTION_WEIGHT = 1.5f
