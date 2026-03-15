package com.nuttyknot.tennisscoretracker

import org.junit.Assert.assertEquals
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

        state = TennisMatchState(userScore = PlayerScore.Forty, opponentScore = PlayerScore.Forty, isDeuce = true)
        assertEquals("Deuce", generateAnnouncement(state))

        state = TennisMatchState(userScore = PlayerScore.Advantage, opponentScore = PlayerScore.Forty, userName = "Alcaraz")
        assertEquals("Advantage, Alcaraz", generateAnnouncement(state))
        
        state = TennisMatchState(userScore = PlayerScore.Forty, opponentScore = PlayerScore.Advantage, opponentName = "Sinner")
        assertEquals("Advantage, Sinner", generateAnnouncement(state))
    }

    @Test
    fun testServerFirstRule() {
        // Opponent serving, Receiver (User) wins first point
        var state = TennisMatchState(
            userScore = PlayerScore.Fifteen,
            opponentScore = PlayerScore.Love,
            isUserServing = false
        )
        assertEquals("Love 15", generateAnnouncement(state))

        // Opponent serving, Server (Opponent) wins first point
        state = TennisMatchState(
            userScore = PlayerScore.Love,
            opponentScore = PlayerScore.Fifteen,
            isUserServing = false
        )
        assertEquals("15 Love", generateAnnouncement(state))

        // Tied scores should still be "All"
        state = TennisMatchState(
            userScore = PlayerScore.Fifteen,
            opponentScore = PlayerScore.Fifteen,
            isUserServing = false
        )
        assertEquals("15 All", generateAnnouncement(state))
    }
}
