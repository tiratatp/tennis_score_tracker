package com.nuttyknot.tennisscoretracker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScoreModelTest {
    private lateinit var scoreModel: ScoreModel

    @Before
    fun setup() {
        scoreModel = ScoreModel()
        scoreModel.updateMatchParameters(userName = "Player 1", opponentName = "Player 2")
    }

    private fun state() = scoreModel.matchState.value

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
        scoreModel.incrementUserScore()
        assertEquals(PlayerScore.Fifteen, state().userScore)

        scoreModel.incrementUserScore()
        assertEquals(PlayerScore.Thirty, state().userScore)

        scoreModel.incrementUserScore()
        assertEquals(PlayerScore.Forty, state().userScore)

        scoreModel.incrementUserScore()
        // Game won by user
        assertEquals(PlayerScore.Love, state().userScore)
        assertEquals(1, state().userGames)
        assertEquals("Player 1", state().gameWinner)
    }

    @Test
    fun `test deuce and advantage`() {
        // Reach 40-40
        repeat(3) { scoreModel.incrementUserScore() }
        repeat(3) { scoreModel.incrementOpponentScore() }

        assertTrue(state().isDeuce)
        assertEquals(PlayerScore.Forty, state().userScore)
        assertEquals(PlayerScore.Forty, state().opponentScore)

        // User Advantage
        scoreModel.incrementUserScore()
        assertFalse(state().isDeuce)
        assertEquals(PlayerScore.Advantage, state().userScore)
        assertEquals(PlayerScore.Forty, state().opponentScore)

        // Back to Deuce
        scoreModel.incrementOpponentScore()
        assertTrue(state().isDeuce)
        assertEquals(PlayerScore.Forty, state().userScore)
        assertEquals(PlayerScore.Forty, state().opponentScore)

        // Opponent Advantage
        scoreModel.incrementOpponentScore()
        assertEquals(PlayerScore.Advantage, state().opponentScore)

        // Opponent Game
        scoreModel.incrementOpponentScore()
        assertEquals(1, state().opponentGames)
        assertEquals("Player 2", state().gameWinner)
        assertEquals(PlayerScore.Love, state().opponentScore)
    }

    @Test
    fun `test standard set win`() {
        // User wins 6 games straight
        repeat(6) {
            repeat(4) { scoreModel.incrementUserScore() }
        }

        assertEquals(1, state().userSets)
        assertEquals(6, state().userGames)
        assertEquals(0, state().opponentGames)
        assertEquals("Player 1", state().setWinner)

        scoreModel.startNextSet()
        assertEquals(0, state().userGames)
        assertEquals(0, state().opponentGames)
    }

    @Test
    fun `test set win at 7-5`() {
        // Both reach 5-5
        repeat(5) {
            repeat(4) { scoreModel.incrementUserScore() }
            repeat(4) { scoreModel.incrementOpponentScore() }
        }

        assertEquals(5, state().userGames)
        assertEquals(5, state().opponentGames)
        assertNull(state().setWinner)

        // User wins 6th game -> 6-5 (no set win yet)
        repeat(4) { scoreModel.incrementUserScore() }
        assertEquals(6, state().userGames)
        assertEquals(5, state().opponentGames)
        assertNull(state().setWinner)

        // User wins 7th game -> 7-5 (set win)
        repeat(4) { scoreModel.incrementUserScore() }
        assertEquals(7, state().userGames)
        assertEquals(5, state().opponentGames)
        assertEquals("Player 1", state().setWinner)
    }

    @Test
    fun `test tiebreaker played at 6-6`() {
        // Both reach 6-6
        repeat(6) {
            repeat(4) { scoreModel.incrementUserScore() }
            repeat(4) { scoreModel.incrementOpponentScore() }
        }

        // Should enter tiebreaker. Scores should be TiebreakScore(0)
        assertTrue(state().userScore is PlayerScore.TiebreakScore)
        assertEquals(0, (state().userScore as PlayerScore.TiebreakScore).points)
        assertEquals(6, state().userGames)
        assertEquals(6, state().opponentGames)

        val firstServerUser = state().isUserServing

        // User wins first point (1-0), serve should switch
        scoreModel.incrementUserScore()
        assertEquals(1, (state().userScore as PlayerScore.TiebreakScore).points)
        assertEquals(0, (state().opponentScore as PlayerScore.TiebreakScore).points)
        assertEquals(!firstServerUser, state().isUserServing) // Changed server

        // Opponent wins next point (1-1), serve should NOT switch (sum = 2)
        scoreModel.incrementOpponentScore()
        assertEquals(!firstServerUser, state().isUserServing)

        // Opponent wins next point (1-2), serve SHOULD switch (sum = 3)
        scoreModel.incrementOpponentScore()
        assertEquals(1, (state().userScore as PlayerScore.TiebreakScore).points)
        assertEquals(2, (state().opponentScore as PlayerScore.TiebreakScore).points)
        assertEquals(firstServerUser, state().isUserServing) // Switched back

        // User wins next 6 points to win 7-2
        repeat(6) {
            scoreModel.incrementUserScore()
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
            repeat(4) { scoreModel.incrementUserScore() }
        }
        assertEquals(1, state().userSets)
        assertEquals("Player 1", state().setWinner)
        assertNull(state().matchWinner)

        scoreModel.startNextSet()

        // Player 1 wins second Set (6-0)
        repeat(6) {
            repeat(4) { scoreModel.incrementUserScore() }
        }
        assertEquals(2, state().userSets)
        assertNull(state().setWinner) // Should be null when match is won
        assertEquals("Player 1", state().matchWinner)
    }

    @Test
    fun `test undo restores previous state`() {
        scoreModel.incrementUserScore()
        assertEquals(PlayerScore.Fifteen, state().userScore)

        scoreModel.incrementUserScore()
        assertEquals(PlayerScore.Thirty, state().userScore)

        scoreModel.undo()
        assertEquals(PlayerScore.Fifteen, state().userScore)

        scoreModel.undo()
        assertEquals(PlayerScore.Love, state().userScore)
    }

    @Test
    fun `test undo on empty history does nothing`() {
        val stateBefore = state()
        scoreModel.undo()
        assertEquals(stateBefore.userScore, state().userScore)
        assertEquals(stateBefore.opponentScore, state().opponentScore)
    }

    @Test
    fun `test reset clears to initial state`() {
        // Play some points
        repeat(3) { scoreModel.incrementUserScore() }
        repeat(2) { scoreModel.incrementOpponentScore() }

        scoreModel.reset()

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
        repeat(4) { scoreModel.incrementUserScore() }
        assertEquals(!initialServer, state().isUserServing)

        // Opponent wins second game
        repeat(4) { scoreModel.incrementOpponentScore() }
        assertEquals(initialServer, state().isUserServing)
    }

    @Test
    fun `test scoring blocked after match won`() {
        // User wins match (2 sets of 6-0)
        repeat(6) { repeat(4) { scoreModel.incrementUserScore() } }
        scoreModel.startNextSet()
        repeat(6) { repeat(4) { scoreModel.incrementUserScore() } }

        assertEquals("Player 1", state().matchWinner)
        val stateAfterWin = state()

        // Attempt to score more points
        scoreModel.incrementUserScore()
        scoreModel.incrementOpponentScore()

        assertEquals(stateAfterWin.userScore, state().userScore)
        assertEquals(stateAfterWin.opponentScore, state().opponentScore)
        assertEquals(stateAfterWin.userGames, state().userGames)
    }

    @Test
    fun `test league match tiebreak at 1-1 sets`() {
        scoreModel.updateMatchParameters(matchFormat = MatchFormat.LEAGUE)
        scoreModel.reset()

        // User wins first set 6-0
        repeat(6) { repeat(4) { scoreModel.incrementUserScore() } }
        assertEquals(1, state().userSets)
        assertEquals("Player 1", state().setWinner)

        scoreModel.startNextSet()

        // Opponent wins second set 6-0
        repeat(6) { repeat(4) { scoreModel.incrementOpponentScore() } }
        assertEquals(1, state().opponentSets)
        assertEquals("Player 2", state().setWinner)

        // Start 3rd set -> should be match tiebreak
        scoreModel.startNextSet()
        assertTrue(state().isMatchTiebreak)
        assertTrue(state().userScore is PlayerScore.TiebreakScore)
        assertEquals(0, (state().userScore as PlayerScore.TiebreakScore).points)

        // Play 10-point match tiebreak: user wins 10-0
        repeat(10) { scoreModel.incrementUserScore() }

        assertEquals("Player 1", state().matchWinner)
        assertEquals(2, state().userSets)
        assertEquals(1, state().opponentSets)
    }

    @Test
    fun `test league regular tiebreak still works at 6-6`() {
        scoreModel.updateMatchParameters(matchFormat = MatchFormat.LEAGUE)
        scoreModel.reset()

        // Both reach 6-6 in first set
        repeat(6) {
            repeat(4) { scoreModel.incrementUserScore() }
            repeat(4) { scoreModel.incrementOpponentScore() }
        }

        // Should enter 7-point tiebreak
        assertTrue(state().userScore is PlayerScore.TiebreakScore)
        assertEquals(6, state().userGames)
        assertEquals(6, state().opponentGames)

        // User wins tiebreak 7-0
        repeat(7) { scoreModel.incrementUserScore() }

        assertEquals(7, state().userGames)
        assertEquals(6, state().opponentGames)
        assertEquals(1, state().userSets)
        assertEquals("Player 1", state().setWinner)
    }

    @Test
    fun `test league match tiebreak extended past 10`() {
        scoreModel.updateMatchParameters(matchFormat = MatchFormat.LEAGUE)
        scoreModel.reset()

        // Each wins 1 set
        repeat(6) { repeat(4) { scoreModel.incrementUserScore() } }
        scoreModel.startNextSet()
        repeat(6) { repeat(4) { scoreModel.incrementOpponentScore() } }
        scoreModel.startNextSet()

        assertTrue(state().isMatchTiebreak)

        // Reach 9-9 in match tiebreak
        repeat(9) {
            scoreModel.incrementUserScore()
            scoreModel.incrementOpponentScore()
        }
        assertEquals(9, (state().userScore as PlayerScore.TiebreakScore).points)
        assertEquals(9, (state().opponentScore as PlayerScore.TiebreakScore).points)
        assertNull(state().matchWinner)

        // User wins 11-9
        scoreModel.incrementUserScore()
        assertNull(state().matchWinner) // 10-9, need 2 point lead
        scoreModel.incrementUserScore()
        assertEquals("Player 1", state().matchWinner)
    }

    @Test
    fun `test fast pro set win at 8 games`() {
        scoreModel.updateMatchParameters(matchFormat = MatchFormat.FAST)
        scoreModel.reset()

        // User wins 8 games straight
        repeat(8) {
            repeat(4) { scoreModel.incrementUserScore() }
        }

        assertEquals(1, state().userSets)
        assertEquals("Player 1", state().matchWinner)
    }

    @Test
    fun `test fast no-ad scoring`() {
        scoreModel.updateMatchParameters(matchFormat = MatchFormat.FAST)
        scoreModel.reset()

        // Reach 40-40 (deuce)
        repeat(3) { scoreModel.incrementUserScore() }
        repeat(3) { scoreModel.incrementOpponentScore() }

        assertTrue(state().isDeuce)

        // Next point should win the game directly (no advantage)
        scoreModel.incrementUserScore()
        assertEquals(1, state().userGames)
        assertEquals("Player 1", state().gameWinner)
    }

    @Test
    fun `test fast tiebreak at 8-8`() {
        scoreModel.updateMatchParameters(matchFormat = MatchFormat.FAST)
        scoreModel.reset()

        // Both reach 8-8
        repeat(8) {
            repeat(4) { scoreModel.incrementUserScore() }
            repeat(4) { scoreModel.incrementOpponentScore() }
        }

        // Should enter tiebreak
        assertTrue(state().userScore is PlayerScore.TiebreakScore)
        assertEquals(8, state().userGames)
        assertEquals(8, state().opponentGames)

        // User wins 7-point tiebreak
        repeat(7) { scoreModel.incrementUserScore() }
        assertEquals("Player 1", state().matchWinner)
    }

    @Test
    fun `test fast set win at 8-6`() {
        scoreModel.updateMatchParameters(matchFormat = MatchFormat.FAST)
        scoreModel.reset()

        // Both reach 6-6
        repeat(6) {
            repeat(4) { scoreModel.incrementUserScore() }
            repeat(4) { scoreModel.incrementOpponentScore() }
        }

        // No tiebreak at 6-6 in fast mode (tiebreak is at 8-8)
        assertFalse(state().userScore is PlayerScore.TiebreakScore)

        // User wins 2 more games to reach 8-6
        repeat(2) { repeat(4) { scoreModel.incrementUserScore() } }

        assertEquals(8, state().userGames)
        assertEquals(6, state().opponentGames)
        assertEquals("Player 1", state().matchWinner)
    }

    @Test
    fun `test undo a game win`() {
        // Win a game: Love -> 15 -> 30 -> 40 -> Game
        repeat(4) { scoreModel.incrementUserScore() }
        assertEquals(1, state().userGames)
        assertEquals("Player 1", state().gameWinner)

        scoreModel.undo()
        assertEquals(0, state().userGames)
        assertEquals(PlayerScore.Forty, state().userScore)
        assertEquals(PlayerScore.Love, state().opponentScore)
        assertNull(state().gameWinner)
    }

    @Test
    fun `test undo a set win`() {
        // Win a set: 6 games of 4 points each
        repeat(6) { repeat(4) { scoreModel.incrementUserScore() } }
        assertEquals(1, state().userSets)
        assertEquals("Player 1", state().setWinner)

        scoreModel.undo()
        assertEquals(0, state().userSets)
        assertEquals(PlayerScore.Forty, state().userScore)
        assertEquals(5, state().userGames)
        assertNull(state().setWinner)
    }

    @Test
    fun `test undo into a tiebreak`() {
        // Reach 6-6 tiebreak
        repeat(6) {
            repeat(4) { scoreModel.incrementUserScore() }
            repeat(4) { scoreModel.incrementOpponentScore() }
        }
        assertTrue(state().userScore is PlayerScore.TiebreakScore)

        // Win tiebreak 7-0
        repeat(7) { scoreModel.incrementUserScore() }
        assertEquals(1, state().userSets)
        assertEquals("Player 1", state().setWinner)

        // Undo the winning point
        scoreModel.undo()
        assertNull(state().setWinner)
        assertEquals(0, state().userSets)
        assertTrue(state().userScore is PlayerScore.TiebreakScore)
        assertEquals(6, (state().userScore as PlayerScore.TiebreakScore).points)
        assertEquals(0, (state().opponentScore as PlayerScore.TiebreakScore).points)
    }

    @Test
    fun `test undo a match win`() {
        // Win first set
        repeat(6) { repeat(4) { scoreModel.incrementUserScore() } }
        scoreModel.startNextSet()

        // Win second set (match win)
        repeat(6) { repeat(4) { scoreModel.incrementUserScore() } }
        assertEquals("Player 1", state().matchWinner)

        // Undo
        scoreModel.undo()
        assertNull(state().matchWinner)
        assertEquals(1, state().userSets)
        assertEquals(PlayerScore.Forty, state().userScore)
        assertEquals(5, state().userGames)

        // Should be able to continue scoring
        scoreModel.incrementUserScore()
        assertEquals(2, state().userSets)
    }

    @Test
    fun `test undo after startNextSet does not cross set boundary`() {
        // Win a set
        repeat(6) { repeat(4) { scoreModel.incrementUserScore() } }
        assertEquals("Player 1", state().setWinner)

        scoreModel.startNextSet()
        assertEquals(0, state().userGames)
        assertEquals(0, state().opponentGames)
        assertTrue(state().isNewSet)

        // Score a point in new set
        scoreModel.incrementUserScore()
        assertEquals(PlayerScore.Fifteen, state().userScore)

        // Undo goes back to Love-Love in the new set (the state after startNextSet + before point)
        scoreModel.undo()
        assertEquals(PlayerScore.Love, state().userScore)
        // Should still be in the new set context
        assertEquals(1, state().userSets)
        assertEquals(0, state().userGames)
    }

    @Test
    fun `test multiple undos through a full game`() {
        // Score 4 points to win a game
        repeat(4) { scoreModel.incrementUserScore() }
        assertEquals(1, state().userGames)

        // Undo all 4 points
        scoreModel.undo() // back to 40-0
        assertEquals(PlayerScore.Forty, state().userScore)
        assertEquals(0, state().userGames)

        scoreModel.undo() // back to 30-0
        assertEquals(PlayerScore.Thirty, state().userScore)

        scoreModel.undo() // back to 15-0
        assertEquals(PlayerScore.Fifteen, state().userScore)

        scoreModel.undo() // back to 0-0
        assertEquals(PlayerScore.Love, state().userScore)
        assertEquals(0, state().userGames)
    }

    @Test
    fun `test full realistic three set match`() {
        // Set 1: Player 1 wins 6-4
        // Player 1 wins 6 games, Player 2 wins 4 games
        repeat(4) {
            repeat(4) { scoreModel.incrementUserScore() } // User game
            repeat(4) { scoreModel.incrementOpponentScore() } // Opponent game
        }
        assertEquals(4, state().userGames)
        assertEquals(4, state().opponentGames)
        // User wins 2 more games
        repeat(2) { repeat(4) { scoreModel.incrementUserScore() } }
        assertEquals(1, state().userSets)
        assertEquals("Player 1", state().setWinner)
        assertEquals(listOf(6 to 4), state().setHistory)

        scoreModel.startNextSet()

        // Set 2: Player 2 wins 6-4
        repeat(4) {
            repeat(4) { scoreModel.incrementOpponentScore() }
            repeat(4) { scoreModel.incrementUserScore() }
        }
        repeat(2) { repeat(4) { scoreModel.incrementOpponentScore() } }
        assertEquals(1, state().opponentSets)
        assertEquals("Player 2", state().setWinner)
        assertEquals(listOf(6 to 4, 4 to 6), state().setHistory)

        scoreModel.startNextSet()

        // Set 3: Player 1 wins 7-5
        repeat(5) {
            repeat(4) { scoreModel.incrementUserScore() }
            repeat(4) { scoreModel.incrementOpponentScore() }
        }
        // User wins 2 more games to go 7-5
        repeat(2) { repeat(4) { scoreModel.incrementUserScore() } }
        assertEquals("Player 1", state().matchWinner)
        assertEquals(2, state().userSets)
        assertEquals(1, state().opponentSets)
        assertEquals(listOf(6 to 4, 4 to 6, 7 to 5), state().setHistory)
    }

    @Test
    fun `test announcement set correctly after scoring and cleared on undo`() {
        // Score a point
        scoreModel.incrementUserScore()
        assertEquals("15 Love", state().announcement)

        // Score another point
        scoreModel.incrementOpponentScore()
        assertEquals("15 All", state().announcement)

        // Undo clears announcement
        scoreModel.undo()
        assertNull(state().announcement)
    }

    @Test
    fun `test format only changes when score is zero`() {
        // Start with standard format
        scoreModel.updateMatchParameters(matchFormat = MatchFormat.STANDARD)
        scoreModel.reset()

        // Score a point
        scoreModel.incrementUserScore()
        assertEquals(PlayerScore.Fifteen, state().userScore)
        assertEquals(MatchFormat.STANDARD, state().matchFormat)

        // Try to change format mid-game
        scoreModel.updateMatchParameters(matchFormat = MatchFormat.FAST)
        assertEquals(MatchFormat.STANDARD, state().matchFormat) // Should NOT change

        // Reset and verify format now applies
        scoreModel.reset()
        assertEquals(MatchFormat.FAST, state().matchFormat)
    }

    @Test
    fun `test extended tiebreak past 6-6`() {
        // Both reach 6-6 in games
        repeat(6) {
            repeat(4) { scoreModel.incrementUserScore() }
            repeat(4) { scoreModel.incrementOpponentScore() }
        }

        assertTrue(state().userScore is PlayerScore.TiebreakScore)

        // Reach 6-6 in tiebreak points
        repeat(6) {
            scoreModel.incrementUserScore()
            scoreModel.incrementOpponentScore()
        }
        assertEquals(6, (state().userScore as PlayerScore.TiebreakScore).points)
        assertEquals(6, (state().opponentScore as PlayerScore.TiebreakScore).points)
        assertNull(state().setWinner) // Not won yet, need 2-point lead

        // Reach 7-7
        scoreModel.incrementUserScore()
        scoreModel.incrementOpponentScore()
        assertNull(state().setWinner)

        // User wins 9-7
        scoreModel.incrementUserScore()
        assertNull(state().setWinner)
        scoreModel.incrementUserScore()
        assertEquals("Player 1", state().setWinner)
        assertEquals(7, state().userGames)
        assertEquals(6, state().opponentGames)
    }
}
