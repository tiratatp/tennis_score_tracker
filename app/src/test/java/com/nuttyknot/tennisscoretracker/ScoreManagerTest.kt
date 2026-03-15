package com.nuttyknot.tennisscoretracker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScoreManagerTest {
    private lateinit var scoreManager: ScoreManager

    @Before
    fun setup() {
        scoreManager = ScoreManager()
        scoreManager.updateMatchParameters(userName = "Player 1", opponentName = "Player 2")
    }

    private fun state() = scoreManager.matchState.value

    @Test
    fun `test initial state`() {
        assertEquals(PlayerScore.Love, state().userScore)
        assertEquals(PlayerScore.Love, state().opponentScore)
        assertEquals(0, state().userGames)
        assertEquals(0, state().opponentGames)
        assertEquals(0, state().userSets)
        assertEquals(0, state().opponentSets)
    }

    @Test
    fun `test standard game win`() {
        // User scores 4 times (15, 30, 40, Game)
        scoreManager.incrementUserScore()
        assertEquals(PlayerScore.Fifteen, state().userScore)

        scoreManager.incrementUserScore()
        assertEquals(PlayerScore.Thirty, state().userScore)

        scoreManager.incrementUserScore()
        assertEquals(PlayerScore.Forty, state().userScore)

        scoreManager.incrementUserScore()
        // Game won by user
        assertEquals(PlayerScore.Love, state().userScore)
        assertEquals(1, state().userGames)
        assertEquals("Player 1", state().gameWinner)
    }

    @Test
    fun `test deuce and advantage`() {
        // Reach 40-40
        repeat(3) { scoreManager.incrementUserScore() }
        repeat(3) { scoreManager.incrementOpponentScore() }

        assertTrue(state().isDeuce)
        assertEquals(PlayerScore.Forty, state().userScore)
        assertEquals(PlayerScore.Forty, state().opponentScore)

        // User Advantage
        scoreManager.incrementUserScore()
        assertFalse(state().isDeuce)
        assertEquals(PlayerScore.Advantage, state().userScore)
        assertEquals(PlayerScore.Forty, state().opponentScore)

        // Back to Deuce
        scoreManager.incrementOpponentScore()
        assertTrue(state().isDeuce)
        assertEquals(PlayerScore.Forty, state().userScore)
        assertEquals(PlayerScore.Forty, state().opponentScore)

        // Opponent Advantage
        scoreManager.incrementOpponentScore()
        assertEquals(PlayerScore.Advantage, state().opponentScore)

        // Opponent Game
        scoreManager.incrementOpponentScore()
        assertEquals(1, state().opponentGames)
        assertEquals("Player 2", state().gameWinner)
        assertEquals(PlayerScore.Love, state().opponentScore)
    }

    @Test
    fun `test standard set win`() {
        // User wins 6 games straight
        repeat(6) {
            repeat(4) { scoreManager.incrementUserScore() }
        }

        assertEquals(1, state().userSets)
        assertEquals(6, state().userGames)
        assertEquals(0, state().opponentGames)
        assertEquals("Player 1", state().setWinner)

        scoreManager.startNextSet()
        assertEquals(0, state().userGames)
        assertEquals(0, state().opponentGames)
    }

    @Test
    fun `test set win at 7-5`() {
        // Both reach 5-5
        repeat(5) {
            repeat(4) { scoreManager.incrementUserScore() }
            repeat(4) { scoreManager.incrementOpponentScore() }
        }

        assertEquals(5, state().userGames)
        assertEquals(5, state().opponentGames)
        assertNull(state().setWinner)

        // User wins 6th game -> 6-5 (no set win yet)
        repeat(4) { scoreManager.incrementUserScore() }
        assertEquals(6, state().userGames)
        assertEquals(5, state().opponentGames)
        assertNull(state().setWinner)

        // User wins 7th game -> 7-5 (set win)
        repeat(4) { scoreManager.incrementUserScore() }
        assertEquals(7, state().userGames)
        assertEquals(5, state().opponentGames)
        assertEquals("Player 1", state().setWinner)
    }

    @Test
    fun `test tiebreaker played at 6-6`() {
        // Both reach 6-6
        repeat(6) {
            repeat(4) { scoreManager.incrementUserScore() }
            repeat(4) { scoreManager.incrementOpponentScore() }
        }

        // Should enter tiebreaker. Scores should be TiebreakScore(0)
        assertTrue(state().userScore is PlayerScore.TiebreakScore)
        assertEquals(0, (state().userScore as PlayerScore.TiebreakScore).points)
        assertEquals(6, state().userGames)
        assertEquals(6, state().opponentGames)

        val firstServerUser = state().isUserServing

        // User wins first point (1-0), serve should switch
        scoreManager.incrementUserScore()
        assertEquals(1, (state().userScore as PlayerScore.TiebreakScore).points)
        assertEquals(0, (state().opponentScore as PlayerScore.TiebreakScore).points)
        assertEquals(!firstServerUser, state().isUserServing) // Changed server

        // Opponent wins next point (1-1), serve should NOT switch (sum = 2)
        scoreManager.incrementOpponentScore()
        assertEquals(!firstServerUser, state().isUserServing)

        // Opponent wins next point (1-2), serve SHOULD switch (sum = 3)
        scoreManager.incrementOpponentScore()
        assertEquals(1, (state().userScore as PlayerScore.TiebreakScore).points)
        assertEquals(2, (state().opponentScore as PlayerScore.TiebreakScore).points)
        assertEquals(firstServerUser, state().isUserServing) // Switched back

        // User wins next 6 points to win 7-2
        repeat(6) {
            scoreManager.incrementUserScore()
        }

        // Tiebreaker won by user. Games: 7-6. Set winner: User
        assertEquals(7, state().userGames)
        assertEquals(6, state().opponentGames)
        assertEquals("Player 1", state().setWinner)
        assertEquals(1, state().userSets)

        // First server of next set should be the player who RECEIVED the first point of the tiebreak
        assertEquals(!firstServerUser, state().isUserServing)
    }

    @Test
    fun `test match win`() {
        // Player 1 wins first Set (6-0)
        repeat(6) {
            repeat(4) { scoreManager.incrementUserScore() }
        }
        assertEquals(1, state().userSets)
        assertEquals("Player 1", state().setWinner)
        assertNull(state().matchWinner)

        scoreManager.startNextSet()

        // Player 1 wins second Set (6-0)
        repeat(6) {
            repeat(4) { scoreManager.incrementUserScore() }
        }
        assertEquals(2, state().userSets)
        assertNull(state().setWinner) // Should be null when match is won
        assertEquals("Player 1", state().matchWinner)
    }

    @Test
    fun `test undo restores previous state`() {
        scoreManager.incrementUserScore()
        assertEquals(PlayerScore.Fifteen, state().userScore)

        scoreManager.incrementUserScore()
        assertEquals(PlayerScore.Thirty, state().userScore)

        scoreManager.undo()
        assertEquals(PlayerScore.Fifteen, state().userScore)

        scoreManager.undo()
        assertEquals(PlayerScore.Love, state().userScore)
    }

    @Test
    fun `test undo on empty history does nothing`() {
        val stateBefore = state()
        scoreManager.undo()
        assertEquals(stateBefore.userScore, state().userScore)
        assertEquals(stateBefore.opponentScore, state().opponentScore)
    }

    @Test
    fun `test reset clears to initial state`() {
        // Play some points
        repeat(3) { scoreManager.incrementUserScore() }
        repeat(2) { scoreManager.incrementOpponentScore() }

        scoreManager.reset()

        assertEquals(PlayerScore.Love, state().userScore)
        assertEquals(PlayerScore.Love, state().opponentScore)
        assertEquals(0, state().userGames)
        assertEquals(0, state().opponentGames)
        assertEquals(0, state().userSets)
        assertEquals(0, state().opponentSets)
        assertEquals("Player 1", state().userName)
        assertEquals("Player 2", state().opponentName)
    }

    @Test
    fun `test server alternates each game`() {
        val initialServer = state().isUserServing

        // User wins first game
        repeat(4) { scoreManager.incrementUserScore() }
        assertEquals(!initialServer, state().isUserServing)

        // Opponent wins second game
        repeat(4) { scoreManager.incrementOpponentScore() }
        assertEquals(initialServer, state().isUserServing)
    }

    @Test
    fun `test scoring blocked after match won`() {
        // User wins match (2 sets of 6-0)
        repeat(6) { repeat(4) { scoreManager.incrementUserScore() } }
        scoreManager.startNextSet()
        repeat(6) { repeat(4) { scoreManager.incrementUserScore() } }

        assertEquals("Player 1", state().matchWinner)
        val stateAfterWin = state()

        // Attempt to score more points
        scoreManager.incrementUserScore()
        scoreManager.incrementOpponentScore()

        assertEquals(stateAfterWin.userScore, state().userScore)
        assertEquals(stateAfterWin.opponentScore, state().opponentScore)
        assertEquals(stateAfterWin.userGames, state().userGames)
    }

    @Test
    fun `test extended tiebreak past 6-6`() {
        // Both reach 6-6 in games
        repeat(6) {
            repeat(4) { scoreManager.incrementUserScore() }
            repeat(4) { scoreManager.incrementOpponentScore() }
        }

        assertTrue(state().userScore is PlayerScore.TiebreakScore)

        // Reach 6-6 in tiebreak points
        repeat(6) {
            scoreManager.incrementUserScore()
            scoreManager.incrementOpponentScore()
        }
        assertEquals(6, (state().userScore as PlayerScore.TiebreakScore).points)
        assertEquals(6, (state().opponentScore as PlayerScore.TiebreakScore).points)
        assertNull(state().setWinner) // Not won yet, need 2-point lead

        // Reach 7-7
        scoreManager.incrementUserScore()
        scoreManager.incrementOpponentScore()
        assertNull(state().setWinner)

        // User wins 9-7
        scoreManager.incrementUserScore()
        assertNull(state().setWinner)
        scoreManager.incrementUserScore()
        assertEquals("Player 1", state().setWinner)
        assertEquals(7, state().userGames)
        assertEquals(6, state().opponentGames)
    }
}
