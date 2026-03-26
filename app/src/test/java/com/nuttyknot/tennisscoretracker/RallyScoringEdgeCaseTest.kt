package com.nuttyknot.tennisscoretracker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RallyScoringEdgeCaseTest {
    private lateinit var scoreModel: ScoreModel

    @Before
    fun setup() {
        scoreModel = ScoreModel()
        scoreModel.updateMatchParameters(userName = "Player 1", opponentName = "Player 2")
    }

    private fun state() = scoreModel.matchState.value

    @Test
    fun `test badminton win by 2 from 20-20`() {
        scoreModel.updateMatchParameters(matchFormat = MatchFormat.BWF_STANDARD)
        scoreModel.reset()

        // Both reach 20-20
        repeat(20) {
            scoreModel.incrementUserScore()
            scoreModel.incrementOpponentScore()
        }
        assertEquals(20, (state().userScore as PlayerScore.TiebreakScore).points)
        assertEquals(20, (state().opponentScore as PlayerScore.TiebreakScore).points)
        assertNull(state().setWinner)

        // 21-20 does NOT win (need 2-point lead)
        scoreModel.incrementUserScore()
        assertNull(state().setWinner)

        // 22-20 wins
        scoreModel.incrementUserScore()
        assertEquals("Player 1", state().setWinner)
    }

    @Test
    fun `test badminton cap at 30`() {
        scoreModel.updateMatchParameters(matchFormat = MatchFormat.BWF_STANDARD)
        scoreModel.reset()

        // Both reach 29-29
        repeat(29) {
            scoreModel.incrementUserScore()
            scoreModel.incrementOpponentScore()
        }
        assertEquals(29, (state().userScore as PlayerScore.TiebreakScore).points)
        assertEquals(29, (state().opponentScore as PlayerScore.TiebreakScore).points)
        assertNull(state().setWinner)

        // 30-29 wins (cap reached, no need for 2-point lead)
        scoreModel.incrementUserScore()
        assertEquals("Player 1", state().setWinner)
    }

    @Test
    fun `test BWF short cap at 15`() {
        scoreModel.updateMatchParameters(matchFormat = MatchFormat.BWF_SHORT)
        scoreModel.reset()

        // Both reach 14-14
        repeat(14) {
            scoreModel.incrementUserScore()
            scoreModel.incrementOpponentScore()
        }
        assertNull(state().setWinner)

        // 15-14 wins (cap reached)
        scoreModel.incrementUserScore()
        assertEquals("Player 1", state().setWinner)
    }

    @Test
    fun `test pickleball win by 2`() {
        scoreModel.updateMatchParameters(matchFormat = MatchFormat.PB_RALLY_11)
        scoreModel.reset()

        // Both reach 10-10
        repeat(10) {
            scoreModel.incrementUserScore()
            scoreModel.incrementOpponentScore()
        }
        assertNull(state().setWinner)

        // 11-10 does NOT win (need 2-point lead, no cap)
        scoreModel.incrementUserScore()
        assertNull(state().setWinner)

        // 11-11
        scoreModel.incrementOpponentScore()
        assertNull(state().setWinner)

        // 12-11
        scoreModel.incrementUserScore()
        assertNull(state().setWinner)

        // 13-11 wins
        scoreModel.incrementUserScore()
        assertEquals("Player 1", state().setWinner)
    }

    @Test
    fun `test sideout full game flow`() {
        scoreModel.updateMatchParameters(matchFormat = MatchFormat.PB_SIDEOUT)
        scoreModel.reset()

        // User is serving initially
        assertTrue(state().isUserServing)

        // User (server) scores -> 1-0
        scoreModel.incrementUserScore()
        assertEquals(1, (state().userScore as PlayerScore.TiebreakScore).points)
        assertEquals(0, (state().opponentScore as PlayerScore.TiebreakScore).points)
        assertTrue(state().isUserServing)

        // Opponent scores while user serves -> side-out, no points change
        scoreModel.incrementOpponentScore()
        assertEquals(1, (state().userScore as PlayerScore.TiebreakScore).points)
        assertEquals(0, (state().opponentScore as PlayerScore.TiebreakScore).points)
        assertFalse(state().isUserServing)

        // Opponent (now server) scores -> 1-1
        scoreModel.incrementOpponentScore()
        assertEquals(1, (state().userScore as PlayerScore.TiebreakScore).points)
        assertEquals(1, (state().opponentScore as PlayerScore.TiebreakScore).points)
        assertFalse(state().isUserServing)

        // User scores while opponent serves -> side-out
        scoreModel.incrementUserScore()
        assertEquals(1, (state().userScore as PlayerScore.TiebreakScore).points)
        assertEquals(1, (state().opponentScore as PlayerScore.TiebreakScore).points)
        assertTrue(state().isUserServing)

        // User (server) scores -> 2-1
        scoreModel.incrementUserScore()
        assertEquals(2, (state().userScore as PlayerScore.TiebreakScore).points)
        assertEquals(1, (state().opponentScore as PlayerScore.TiebreakScore).points)
    }

    @Test
    fun `test badminton server rotation`() {
        scoreModel.updateMatchParameters(matchFormat = MatchFormat.BWF_STANDARD)
        scoreModel.reset()

        assertTrue(state().isUserServing)

        // User (server) wins rally -> server stays
        scoreModel.incrementUserScore()
        assertTrue(state().isUserServing)

        // Opponent (receiver) wins rally -> server switches
        scoreModel.incrementOpponentScore()
        assertFalse(state().isUserServing)

        // Opponent (now server) wins rally -> server stays
        scoreModel.incrementOpponentScore()
        assertFalse(state().isUserServing)

        // User (now receiver) wins rally -> server switches back
        scoreModel.incrementUserScore()
        assertTrue(state().isUserServing)
    }

    @Test
    fun `test badminton game winner serves in next game`() {
        scoreModel.updateMatchParameters(matchFormat = MatchFormat.BWF_STANDARD)
        scoreModel.reset()

        // User wins first game 21-0
        repeat(21) { scoreModel.incrementUserScore() }
        assertEquals("Player 1", state().setWinner)
        // User won the game, so user should serve in the next game
        assertTrue("Game winner should serve in next game", state().isUserServing)

        // Start next game
        scoreModel.startNextSet()

        // Now play a game where opponent wins but user served the second-to-last rally.
        // Opponent scores 20 points (opponent serves each time since opponent wins the rally)
        repeat(20) { scoreModel.incrementOpponentScore() }
        // User scores 1 to make it 1-20 (user now becomes server in badminton since user won that rally)
        scoreModel.incrementUserScore()
        assertTrue("User should be serving after winning a rally", state().isUserServing)
        // Opponent wins the game at 21-1
        scoreModel.incrementOpponentScore()
        assertEquals("Player 2", state().setWinner)
        // Opponent won the game -> opponent should serve, NOT a toggle of isUserServing
        assertFalse("Game winner (opponent) should serve in next game", state().isUserServing)
    }

    @Test
    fun `test updateMatchParameters converts score type for non-tennis format`() {
        scoreModel.updateMatchParameters(matchFormat = MatchFormat.STANDARD)
        scoreModel.reset()
        assertEquals(PlayerScore.Love, state().userScore)
        assertEquals(PlayerScore.Love, state().opponentScore)

        // Switch to badminton (should convert to TiebreakScore)
        scoreModel.updateMatchParameters(matchFormat = MatchFormat.BWF_STANDARD)
        assertTrue(state().userScore is PlayerScore.TiebreakScore)
        assertTrue(state().opponentScore is PlayerScore.TiebreakScore)
        assertEquals(0, (state().userScore as PlayerScore.TiebreakScore).points)

        // Switch back to tennis (should convert to Love)
        scoreModel.updateMatchParameters(matchFormat = MatchFormat.STANDARD)
        assertEquals(PlayerScore.Love, state().userScore)
        assertEquals(PlayerScore.Love, state().opponentScore)
    }
}
