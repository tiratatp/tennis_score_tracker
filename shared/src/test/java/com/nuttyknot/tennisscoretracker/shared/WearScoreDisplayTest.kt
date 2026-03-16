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

    @Test
    fun `null colors round-trip`() {
        val original = WearScoreDisplay()
        val restored = WearScoreDisplay.fromJson(original.toJson())
        assertNull(restored.primaryColorArgb)
        assertNull(restored.secondaryColorArgb)
    }

    @Test
    fun `populated colors round-trip`() {
        @Suppress("MagicNumber")
        val original =
            WearScoreDisplay(
                primaryColorArgb = 0xFFCCFF00.toInt(),
                secondaryColorArgb = 0xFFFFFFFF.toInt(),
            )
        val restored = WearScoreDisplay.fromJson(original.toJson())
        assertEquals(original.primaryColorArgb, restored.primaryColorArgb)
        assertEquals(original.secondaryColorArgb, restored.secondaryColorArgb)
    }

    @Test
    fun `missing colors in old JSON`() {
        val json = """{"userName":"A","opponentName":"B"}"""
        val restored = WearScoreDisplay.fromJson(json)
        assertNull(restored.primaryColorArgb)
        assertNull(restored.secondaryColorArgb)
    }

    @Test
    fun `old phone full JSON parses with null colors`() {
        // Simulates JSON from a phone app version before color fields were added
        val json =
            """
            {
                "userName":"Alice","opponentName":"Bob",
                "userScore":"40","opponentScore":"30",
                "userGames":5,"opponentGames":3,
                "userSets":1,"opponentSets":0,
                "setHistory":[{"user":6,"opponent":4}],
                "isUserServing":false,
                "isMatchOver":false
            }
            """.trimIndent()
        val restored = WearScoreDisplay.fromJson(json)
        assertEquals("Alice", restored.userName)
        assertEquals("40", restored.userScore)
        assertEquals(5, restored.userGames)
        assertEquals(1, restored.setHistory.size)
        assertNull(restored.primaryColorArgb)
        assertNull(restored.secondaryColorArgb)
    }

    @Test
    fun `unknown fields in JSON are ignored`() {
        // Simulates a newer phone sending fields the current watch doesn't know about
        val json =
            """
            {
                "userName":"Alice","opponentName":"Bob",
                "userScore":"15","opponentScore":"0",
                "userGames":0,"opponentGames":0,
                "userSets":0,"opponentSets":0,
                "setHistory":[],
                "isUserServing":true,
                "isMatchOver":false,
                "primaryColorArgb":-3342592,
                "secondaryColorArgb":-1,
                "futureField":"someValue",
                "anotherNewField":42
            }
            """.trimIndent()
        val restored = WearScoreDisplay.fromJson(json)
        assertEquals("Alice", restored.userName)
        assertEquals("15", restored.userScore)
        assertEquals(-3342592, restored.primaryColorArgb)
        assertEquals(-1, restored.secondaryColorArgb)
    }

    @Test
    fun `new JSON with colors consumed by old parser logic`() {
        // Verifies that a fully populated new-format JSON round-trips correctly
        @Suppress("MagicNumber")
        val original =
            WearScoreDisplay(
                userName = "Alice",
                opponentName = "Bob",
                userScore = "AD",
                opponentScore = "40",
                userGames = 6,
                opponentGames = 5,
                userSets = 1,
                opponentSets = 0,
                setHistory = listOf(6 to 4),
                isUserServing = true,
                isMatchOver = false,
                matchWinner = null,
                primaryColorArgb = 0xFF00FFFF.toInt(),
                secondaryColorArgb = 0xFFFF00FF.toInt(),
            )
        val restored = WearScoreDisplay.fromJson(original.toJson())
        assertEquals(original, restored)
    }
}
