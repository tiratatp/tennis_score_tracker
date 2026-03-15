package com.nuttyknot.tennisscoretracker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TennisScoreStateTest {
    @Test
    fun testTtsString() {
        var state = TennisMatchState(userScore = PlayerScore.Love, opponentScore = PlayerScore.Love)
        assertEquals("Love All", generateAnnouncement(state))

        state = TennisMatchState(userScore = PlayerScore.Fifteen, opponentScore = PlayerScore.Love)
        assertEquals("15 Love", generateAnnouncement(state))

        state = TennisMatchState(userScore = PlayerScore.Thirty, opponentScore = PlayerScore.Thirty)
        assertEquals("30 All", generateAnnouncement(state))

        state =
            TennisMatchState(
                userScore = PlayerScore.Forty,
                opponentScore = PlayerScore.Forty,
            )
        assertTrue(state.isDeuce)
        assertEquals("Deuce", generateAnnouncement(state))

        state =
            TennisMatchState(
                userScore = PlayerScore.Advantage,
                opponentScore = PlayerScore.Forty,
                userName = "Alcaraz",
            )
        assertEquals("Advantage, Alcaraz", generateAnnouncement(state))

        state =
            TennisMatchState(
                userScore = PlayerScore.Forty,
                opponentScore = PlayerScore.Advantage,
                opponentName = "Sinner",
            )
        assertEquals("Advantage, Sinner", generateAnnouncement(state))
    }

    @Test
    fun testAdvantageDisplay() {
        assertEquals("AD", PlayerScore.Advantage.display)
        assertEquals("Advantage", PlayerScore.Advantage.tts)
    }

    @Test
    fun testAdvantageAnnouncementsWithoutNames() {
        var state =
            TennisMatchState(
                userScore = PlayerScore.Advantage,
                opponentScore = PlayerScore.Forty,
                isUserServing = true,
                userName = "",
            )
        // Note: The user explicitly asked NOT to change announcement text,
        // so it should remain "Ad-In" even though display is "AD"
        assertEquals("Ad-In", generateAnnouncement(state))

        state =
            TennisMatchState(
                userScore = PlayerScore.Forty,
                opponentScore = PlayerScore.Advantage,
                isUserServing = true,
                opponentName = "",
            )
        assertEquals("Ad-Out", generateAnnouncement(state))
    }

    @Test
    fun testTiebreakScoreAnnouncement() {
        // Server-first ordering: server score announced first
        val state =
            TennisMatchState(
                userScore = PlayerScore.TiebreakScore(3),
                opponentScore = PlayerScore.TiebreakScore(2),
                isUserServing = true,
            )
        assertEquals("3 2", generateAnnouncement(state))

        // Opponent serving: opponent score first
        val state2 =
            TennisMatchState(
                userScore = PlayerScore.TiebreakScore(3),
                opponentScore = PlayerScore.TiebreakScore(2),
                isUserServing = false,
            )
        assertEquals("2 3", generateAnnouncement(state2))

        // Tied tiebreak scores
        val state3 =
            TennisMatchState(
                userScore = PlayerScore.TiebreakScore(4),
                opponentScore = PlayerScore.TiebreakScore(4),
                isUserServing = true,
            )
        assertEquals("4 All", generateAnnouncement(state3))
    }

    @Test
    fun testGameWinningAnnouncement() {
        val state =
            TennisMatchState(
                userGames = 2,
                opponentGames = 1,
                gameWinner = "Player 1",
                userName = "Player 1",
                opponentName = "Player 2",
            )
        assertEquals("Game, Player 1. Player 1 leads 2 games to 1", generateAnnouncement(state))
    }

    @Test
    fun testGameWinningAnnouncementGamesTied() {
        val state =
            TennisMatchState(
                userGames = 3,
                opponentGames = 3,
                gameWinner = "Player 1",
                userName = "Player 1",
                opponentName = "Player 2",
            )
        assertEquals("Game, Player 1. Games are 3 All", generateAnnouncement(state))
    }

    @Test
    fun testGameWinningAnnouncementSingleGame() {
        val state =
            TennisMatchState(
                userGames = 1,
                opponentGames = 0,
                gameWinner = "Player 1",
                userName = "Player 1",
                opponentName = "Player 2",
            )
        assertEquals("Game, Player 1. Player 1 leads 1 game to 0", generateAnnouncement(state))
    }

    @Test
    fun testSetWinningAnnouncement() {
        val state =
            TennisMatchState(
                userGames = 6,
                opponentGames = 4,
                userSets = 1,
                setWinner = "Player 1",
                setHistory = listOf(6 to 4),
                userName = "Player 1",
            )
        assertEquals("Game and First Set, Player 1, 6 4", generateAnnouncement(state))
    }

    @Test
    fun testMatchWinningAnnouncement() {
        val state =
            TennisMatchState(
                userSets = 2,
                matchWinner = "Player 1",
                setHistory = listOf(6 to 4, 6 to 3),
                userName = "Player 1",
            )
        assertEquals("Game, Set, and Match, Player 1. 6 4, 6 3", generateAnnouncement(state))
    }

    @Test
    fun testNewSetAnnouncement() {
        val state =
            TennisMatchState(
                userScore = PlayerScore.Love,
                opponentScore = PlayerScore.Love,
                isNewSet = true,
                userSets = 1,
                opponentSets = 0,
                isUserServing = true,
                userName = "Player 1",
            )
        assertEquals("Second Set, Player 1 to serve. Play.", generateAnnouncement(state))
    }

    @Test
    fun testMatchTiebreakNewSetAnnouncement() {
        val state =
            TennisMatchState(
                userScore = PlayerScore.Love,
                opponentScore = PlayerScore.Love,
                isNewSet = true,
                isMatchTiebreak = true,
                userSets = 1,
                opponentSets = 1,
                isUserServing = true,
                userName = "Player 1",
            )
        assertEquals("Match Tiebreak, Player 1 to serve", generateAnnouncement(state))
    }

    @Test
    fun testIsScoreZero() {
        val initial = TennisMatchState()
        assertTrue(initial.isScoreZero)

        val afterPoint =
            TennisMatchState(userScore = PlayerScore.Fifteen)
        assertFalse(afterPoint.isScoreZero)

        val afterGame =
            TennisMatchState(userGames = 1)
        assertFalse(afterGame.isScoreZero)

        val afterSet =
            TennisMatchState(userSets = 1, setHistory = listOf(6 to 4))
        assertFalse(afterSet.isScoreZero)
    }

    @Test
    fun testServerFirstRule() {
        // Opponent serving, Receiver (User) wins first point
        var state =
            TennisMatchState(
                userScore = PlayerScore.Fifteen,
                opponentScore = PlayerScore.Love,
                isUserServing = false,
            )
        assertEquals("Love 15", generateAnnouncement(state))

        // Opponent serving, Server (Opponent) wins first point
        state =
            TennisMatchState(
                userScore = PlayerScore.Love,
                opponentScore = PlayerScore.Fifteen,
                isUserServing = false,
            )
        assertEquals("15 Love", generateAnnouncement(state))

        // Tied scores should still be "All"
        state =
            TennisMatchState(
                userScore = PlayerScore.Fifteen,
                opponentScore = PlayerScore.Fifteen,
                isUserServing = false,
            )
        assertEquals("15 All", generateAnnouncement(state))
    }
}
