package com.nuttyknot.tennisscoretracker.scoring

import com.nuttyknot.tennisscoretracker.MatchFormat

data class FormatConfig(
    val gamesToWinSet: Int,
    val gamesToWinSetLong: Int,
    val setsToWinMatch: Int,
    val useAdvantageScoring: Boolean,
    val useMatchTiebreak: Boolean,
    val matchTiebreakPoints: Int,
    val regularTiebreakPoints: Int,
    val isRallyScoring: Boolean = false,
    val isSideOutScoring: Boolean = false,
    val pointsToWinGame: Int = 0,
    val pointsCap: Int = 0,
) {
    companion object {
        private const val GAMES_TO_WIN_SET_STANDARD = 6
        private const val GAMES_TO_WIN_SET_FAST = 8
        private const val SETS_TO_WIN_MATCH_STANDARD = 2
        private const val REGULAR_TIEBREAK_POINTS = 7
        private const val MATCH_TIEBREAK_POINTS = 10

        // Badminton
        private const val BWF_STANDARD_POINTS = 21
        private const val BWF_STANDARD_CAP = 30
        private const val BWF_SHORT_POINTS = 11
        private const val BWF_SHORT_CAP = 15
        private const val BWF_SHORT_GAMES_TO_WIN = 3

        // Pickleball
        private const val PB_POINTS_11 = 11
        private const val PB_POINTS_15 = 15
        private const val PB_POINTS_21 = 21

        @Suppress("CyclomaticComplexMethod", "LongMethod")
        fun forFormat(format: MatchFormat): FormatConfig =
            when (format) {
                MatchFormat.STANDARD ->
                    FormatConfig(
                        gamesToWinSet = GAMES_TO_WIN_SET_STANDARD,
                        gamesToWinSetLong = GAMES_TO_WIN_SET_STANDARD + 1,
                        setsToWinMatch = SETS_TO_WIN_MATCH_STANDARD,
                        useAdvantageScoring = true,
                        useMatchTiebreak = false,
                        matchTiebreakPoints = MATCH_TIEBREAK_POINTS,
                        regularTiebreakPoints = REGULAR_TIEBREAK_POINTS,
                    )
                MatchFormat.LEAGUE ->
                    FormatConfig(
                        gamesToWinSet = GAMES_TO_WIN_SET_STANDARD,
                        gamesToWinSetLong = GAMES_TO_WIN_SET_STANDARD + 1,
                        setsToWinMatch = SETS_TO_WIN_MATCH_STANDARD,
                        useAdvantageScoring = true,
                        useMatchTiebreak = true,
                        matchTiebreakPoints = MATCH_TIEBREAK_POINTS,
                        regularTiebreakPoints = REGULAR_TIEBREAK_POINTS,
                    )
                MatchFormat.FAST ->
                    FormatConfig(
                        gamesToWinSet = GAMES_TO_WIN_SET_FAST,
                        gamesToWinSetLong = GAMES_TO_WIN_SET_FAST + 1,
                        setsToWinMatch = 1,
                        useAdvantageScoring = false,
                        useMatchTiebreak = false,
                        matchTiebreakPoints = MATCH_TIEBREAK_POINTS,
                        regularTiebreakPoints = REGULAR_TIEBREAK_POINTS,
                    )
                MatchFormat.BWF_STANDARD ->
                    FormatConfig(
                        gamesToWinSet = 0, gamesToWinSetLong = 0,
                        setsToWinMatch = SETS_TO_WIN_MATCH_STANDARD,
                        useAdvantageScoring = false, useMatchTiebreak = false,
                        matchTiebreakPoints = 0, regularTiebreakPoints = 0,
                        isRallyScoring = true,
                        pointsToWinGame = BWF_STANDARD_POINTS,
                        pointsCap = BWF_STANDARD_CAP,
                    )
                MatchFormat.BWF_SHORT ->
                    FormatConfig(
                        gamesToWinSet = 0, gamesToWinSetLong = 0,
                        setsToWinMatch = BWF_SHORT_GAMES_TO_WIN,
                        useAdvantageScoring = false, useMatchTiebreak = false,
                        matchTiebreakPoints = 0, regularTiebreakPoints = 0,
                        isRallyScoring = true,
                        pointsToWinGame = BWF_SHORT_POINTS,
                        pointsCap = BWF_SHORT_CAP,
                    )
                MatchFormat.PB_RALLY_11 ->
                    FormatConfig(
                        gamesToWinSet = 0, gamesToWinSetLong = 0,
                        setsToWinMatch = SETS_TO_WIN_MATCH_STANDARD,
                        useAdvantageScoring = false, useMatchTiebreak = false,
                        matchTiebreakPoints = 0, regularTiebreakPoints = 0,
                        isRallyScoring = true,
                        pointsToWinGame = PB_POINTS_11,
                    )
                MatchFormat.PB_RALLY_15 ->
                    FormatConfig(
                        gamesToWinSet = 0, gamesToWinSetLong = 0,
                        setsToWinMatch = SETS_TO_WIN_MATCH_STANDARD,
                        useAdvantageScoring = false, useMatchTiebreak = false,
                        matchTiebreakPoints = 0, regularTiebreakPoints = 0,
                        isRallyScoring = true,
                        pointsToWinGame = PB_POINTS_15,
                    )
                MatchFormat.PB_RALLY_21 ->
                    FormatConfig(
                        gamesToWinSet = 0, gamesToWinSetLong = 0,
                        setsToWinMatch = SETS_TO_WIN_MATCH_STANDARD,
                        useAdvantageScoring = false, useMatchTiebreak = false,
                        matchTiebreakPoints = 0, regularTiebreakPoints = 0,
                        isRallyScoring = true,
                        pointsToWinGame = PB_POINTS_21,
                    )
                MatchFormat.PB_SIDEOUT ->
                    FormatConfig(
                        gamesToWinSet = 0, gamesToWinSetLong = 0,
                        setsToWinMatch = SETS_TO_WIN_MATCH_STANDARD,
                        useAdvantageScoring = false, useMatchTiebreak = false,
                        matchTiebreakPoints = 0, regularTiebreakPoints = 0,
                        isSideOutScoring = true,
                        pointsToWinGame = PB_POINTS_11,
                    )
            }
    }
}
