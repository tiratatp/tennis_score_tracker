package com.nuttyknot.tennisscoretracker.shared

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WearScoreDisplayTest {
    @Test
    fun `default round-trip`() {
        val original = WearScoreDisplay()
        val restored = WearScoreDisplay.fromJson(original.toJson())
        assertEquals(original, restored)
    }

    @Test
    fun `fully populated round-trip`() {
        val original =
            WearScoreDisplay(
                userName = "Alice",
                opponentName = "Bob",
                userScore = "40",
                opponentScore = "30",
                userGames = 5,
                opponentGames = 3,
                userSets = 1,
                opponentSets = 0,
                setHistory = listOf(6 to 4, 3 to 6),
                isUserServing = false,
                isMatchOver = true,
                matchWinner = "Alice",
            )
        val restored = WearScoreDisplay.fromJson(original.toJson())
        assertEquals(original, restored)
    }

    @Test
    fun `null matchWinner preserved`() {
        val original = WearScoreDisplay(matchWinner = null)
        val restored = WearScoreDisplay.fromJson(original.toJson())
        assertNull(restored.matchWinner)
    }

    @Test
    fun `empty setHistory preserved`() {
        val original = WearScoreDisplay(setHistory = emptyList())
        val restored = WearScoreDisplay.fromJson(original.toJson())
        assertTrue(restored.setHistory.isEmpty())
    }

    @Test
    fun `multi-set history order`() {
        val history = listOf(6 to 4, 4 to 6, 7 to 5)
        val original = WearScoreDisplay(setHistory = history)
        val restored = WearScoreDisplay.fromJson(original.toJson())
        assertEquals(history, restored.setHistory)
    }

    @Test
    fun `non-standard score strings`() {
        val original = WearScoreDisplay(userScore = "6", opponentScore = "10")
        val restored = WearScoreDisplay.fromJson(original.toJson())
        assertEquals("6", restored.userScore)
        assertEquals("10", restored.opponentScore)
    }
}
