package com.nuttyknot.tennisscoretracker

import org.junit.Assert.assertEquals
import org.junit.Test

class TennisScoreStateTest {
    @Test
    fun testTtsString() {
        var state = TennisMatchState(userScore = PlayerScore.Love, opponentScore = PlayerScore.Love)
        assertEquals("Love Love", state.toTtsString())

        state = TennisMatchState(userScore = PlayerScore.Fifteen, opponentScore = PlayerScore.Love)
        assertEquals("15 Love", state.toTtsString())

        state = TennisMatchState(userScore = PlayerScore.Thirty, opponentScore = PlayerScore.Thirty)
        assertEquals("30 all", state.toTtsString())

        state = TennisMatchState(userScore = PlayerScore.Forty, opponentScore = PlayerScore.Forty, isDeuce = true)
        assertEquals("Deuce", state.toTtsString())

        state = TennisMatchState(userScore = PlayerScore.Advantage, opponentScore = PlayerScore.Forty)
        assertEquals("Advantage User", state.toTtsString())
        
        state = TennisMatchState(userScore = PlayerScore.Forty, opponentScore = PlayerScore.Advantage)
        assertEquals("Advantage Opponent", state.toTtsString())
    }
}
