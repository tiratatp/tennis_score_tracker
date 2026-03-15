package com.nuttyknot.tennisscoretracker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class ScoreManagerTest {
    @Test
    fun testRegularScoring() {
        val manager = ScoreManager()
        
        manager.incrementUserScore()
        assertEquals(PlayerScore.Fifteen, manager.matchState.value.userScore)
        assertEquals(PlayerScore.Love, manager.matchState.value.opponentScore)
        
        manager.incrementOpponentScore()
        assertEquals(PlayerScore.Fifteen, manager.matchState.value.opponentScore)
        
        manager.incrementUserScore()
        assertEquals(PlayerScore.Thirty, manager.matchState.value.userScore)
        
        manager.incrementUserScore()
        assertEquals(PlayerScore.Forty, manager.matchState.value.userScore)
        
        manager.incrementUserScore() // Game win
        assertEquals(PlayerScore.Love, manager.matchState.value.userScore)
        assertEquals(PlayerScore.Love, manager.matchState.value.opponentScore)
        assertEquals(1, manager.matchState.value.userGames)
    }

    @Test
    fun testDeuceAndAdvantage() {
        val manager = ScoreManager()
        
        for (i in 1..3) {
            manager.incrementUserScore() // 40
            manager.incrementOpponentScore() // 40
        }
        
        assertTrue(manager.matchState.value.isDeuce)
        
        manager.incrementUserScore() // Ad user
        assertEquals(PlayerScore.Advantage, manager.matchState.value.userScore)
        assertFalse(manager.matchState.value.isDeuce)
        
        manager.incrementOpponentScore() // Back to Deuce
        assertEquals(PlayerScore.Forty, manager.matchState.value.userScore)
        assertEquals(PlayerScore.Forty, manager.matchState.value.opponentScore)
        assertTrue(manager.matchState.value.isDeuce)
        
        manager.incrementOpponentScore() // Ad opp
        assertEquals(PlayerScore.Advantage, manager.matchState.value.opponentScore)
        
        manager.incrementOpponentScore() // Game win opp
        assertEquals(PlayerScore.Love, manager.matchState.value.userScore)
        assertEquals(PlayerScore.Love, manager.matchState.value.opponentScore)
        assertEquals(1, manager.matchState.value.opponentGames)
    }

    @Test
    fun testUndoStack() {
        val manager = ScoreManager()
        manager.incrementUserScore() // User: 15
        manager.incrementUserScore() // User: 30
        
        assertEquals(PlayerScore.Thirty, manager.matchState.value.userScore)
        
        manager.undo() // User: 15
        assertEquals(PlayerScore.Fifteen, manager.matchState.value.userScore)
        
        manager.undo() // User: Love
        assertEquals(PlayerScore.Love, manager.matchState.value.userScore)
    }
}
