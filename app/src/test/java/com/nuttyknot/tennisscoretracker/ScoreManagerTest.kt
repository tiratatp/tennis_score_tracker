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

    @Test
    fun testReset() {
        val manager = ScoreManager()
        manager.incrementUserScore() // User: 15
        manager.incrementOpponentScore() // Opponent: 15
        manager.incrementUserScore() // User: 30
        
        manager.reset()
        
        assertEquals(PlayerScore.Love, manager.matchState.value.userScore)
        assertEquals(PlayerScore.Love, manager.matchState.value.opponentScore)
        assertEquals(0, manager.matchState.value.userGames)
        assertEquals(0, manager.matchState.value.opponentGames)
        assertEquals(0, manager.matchState.value.userSets)
        assertEquals(0, manager.matchState.value.opponentSets)
        assertFalse(manager.matchState.value.isDeuce)
        
        // Ensure history is cleared by checking undo doesn't do anything (or doesn't crash)
        manager.undo()
        assertEquals(PlayerScore.Love, manager.matchState.value.userScore)
    }

    @Test
    fun testSetWinAndReset() {
        val manager = ScoreManager()
        
        // Win 6 games to 0
        for (game in 1..6) {
            for (point in 1..4) {
                manager.incrementUserScore()
            }
        }
        
        assertEquals(6, manager.matchState.value.userGames)
        assertEquals(0, manager.matchState.value.opponentGames)
        assertEquals(1, manager.matchState.value.userSets)
        assertEquals("User", manager.matchState.value.setWinner)
        assertTrue(manager.matchState.value.isNewSet)
        
        // First click after set win should reset games
        manager.incrementUserScore()
        
        assertEquals(0, manager.matchState.value.userGames)
        assertEquals(0, manager.matchState.value.opponentGames)
        assertEquals(PlayerScore.Love, manager.matchState.value.userScore)
        assertEquals(1, manager.matchState.value.userSets)
        assertEquals(null, manager.matchState.value.setWinner)
    }
}
