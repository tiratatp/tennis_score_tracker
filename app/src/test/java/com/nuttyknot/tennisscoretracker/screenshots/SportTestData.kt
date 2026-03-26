package com.nuttyknot.tennisscoretracker.screenshots

import com.nuttyknot.tennisscoretracker.MatchFormat
import com.nuttyknot.tennisscoretracker.MatchState
import com.nuttyknot.tennisscoretracker.PlayerScore

object SportTestData {
    // 2008 Wimbledon Men's Final: Nadal vs Federer
    val tennisInMatch =
        MatchState(
            userScore = PlayerScore.Forty,
            opponentScore = PlayerScore.Fifteen,
            userGames = 9,
            opponentGames = 7,
            userSets = 2,
            opponentSets = 2,
            setHistory = listOf(6 to 4, 6 to 4, 6 to 7, 6 to 7),
            isUserServing = true,
            userName = "Nadal",
            opponentName = "Federer",
        )

    val tennisMatchOver =
        MatchState(
            userScore = PlayerScore.Love,
            opponentScore = PlayerScore.Love,
            userGames = 9,
            opponentGames = 7,
            userSets = 3,
            opponentSets = 2,
            setHistory = listOf(6 to 4, 6 to 4, 6 to 7, 6 to 7, 9 to 7),
            isUserServing = true,
            userName = "Nadal",
            opponentName = "Federer",
            matchWinner = "Nadal",
        )

    val badmintonInMatch =
        MatchState(
            userScore = PlayerScore.TiebreakScore(18),
            opponentScore = PlayerScore.TiebreakScore(15),
            userGames = 0,
            opponentGames = 0,
            userSets = 1,
            opponentSets = 0,
            setHistory = listOf(21 to 11),
            isUserServing = true,
            userName = "Axelsen",
            opponentName = "Momota",
            matchFormat = MatchFormat.BWF_STANDARD,
        )

    val badmintonMatchOver =
        MatchState(
            userScore = PlayerScore.Love,
            opponentScore = PlayerScore.Love,
            userGames = 0,
            opponentGames = 0,
            userSets = 2,
            opponentSets = 0,
            setHistory = listOf(21 to 11, 21 to 15),
            isUserServing = true,
            userName = "Axelsen",
            opponentName = "Momota",
            matchWinner = "Axelsen",
            matchFormat = MatchFormat.BWF_STANDARD,
        )

    val pickleballInMatch =
        MatchState(
            userScore = PlayerScore.TiebreakScore(8),
            opponentScore = PlayerScore.TiebreakScore(6),
            userGames = 0,
            opponentGames = 0,
            userSets = 1,
            opponentSets = 0,
            setHistory = listOf(11 to 8),
            isUserServing = true,
            userName = "B. Johns",
            opponentName = "Waters",
            matchFormat = MatchFormat.PB_RALLY_11,
        )

    val pickleballMatchOver =
        MatchState(
            userScore = PlayerScore.Love,
            opponentScore = PlayerScore.Love,
            userGames = 0,
            opponentGames = 0,
            userSets = 2,
            opponentSets = 0,
            setHistory = listOf(11 to 8, 11 to 6),
            isUserServing = true,
            userName = "B. Johns",
            opponentName = "Waters",
            matchWinner = "B. Johns",
            matchFormat = MatchFormat.PB_RALLY_11,
        )

    fun inMatchForSport(sport: String): MatchState =
        when (sport) {
            "TENNIS" -> tennisInMatch
            "BADMINTON" -> badmintonInMatch
            "PICKLEBALL" -> pickleballInMatch
            else -> tennisInMatch
        }

    fun matchOverForSport(sport: String): MatchState =
        when (sport) {
            "TENNIS" -> tennisMatchOver
            "BADMINTON" -> badmintonMatchOver
            "PICKLEBALL" -> pickleballMatchOver
            else -> tennisMatchOver
        }
}
