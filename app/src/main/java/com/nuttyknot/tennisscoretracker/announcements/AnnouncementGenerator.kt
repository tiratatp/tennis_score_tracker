package com.nuttyknot.tennisscoretracker.announcements

import com.nuttyknot.tennisscoretracker.MatchState
import com.nuttyknot.tennisscoretracker.Sport

fun generateAnnouncement(state: MatchState): String =
    when (state.matchFormat.sport) {
        Sport.TENNIS -> generateTennisAnnouncement(state)
        Sport.BADMINTON, Sport.PICKLEBALL -> generateRallyAnnouncement(state)
    }

private object OrdinalConstants {
    const val THIRD = 3
    const val FOURTH = 4
    const val FIFTH = 5
}

internal fun getOrdinalString(value: Int): String =
    when (value) {
        1 -> "First"
        2 -> "Second"
        OrdinalConstants.THIRD -> "Third"
        OrdinalConstants.FOURTH -> "Fourth"
        OrdinalConstants.FIFTH -> "Fifth"
        else -> "$value"
    }
