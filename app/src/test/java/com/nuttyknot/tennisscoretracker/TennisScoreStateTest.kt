package com.nuttyknot.tennisscoretracker

import com.nuttyknot.tennisscoretracker.announcements.generateAnnouncement
import com.nuttyknot.tennisscoretracker.announcements.generateSideOutAnnouncement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TennisScoreStateTest {
    @Test
    fun testTtsString() {
        var state = MatchState(userScore = PlayerScore.Love, opponentScore = PlayerScore.Love)
        assertEquals("Love All", generateAnnouncement(state))

        state = MatchState(userScore = PlayerScore.Fifteen, opponentScore = PlayerScore.Love)
        assertEquals("15 Love", generateAnnouncement(state))

        state = MatchState(userScore = PlayerScore.Thirty, opponentScore = PlayerScore.Thirty)
        assertEquals("30 All", generateAnnouncement(state))

        state =
            MatchState(
                userScore = PlayerScore.Forty,
                opponentScore = PlayerScore.Forty,
            )
        assertTrue(state.isDeuce)
        assertEquals("Deuce", generateAnnouncement(state))

        state =
            MatchState(
                userScore = PlayerScore.Advantage,
                opponentScore = PlayerScore.Forty,
                userName = "Alcaraz",
            )
        assertEquals("Advantage, Alcaraz", generateAnnouncement(state))

        state =
            MatchState(
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
            MatchState(
                userScore = PlayerScore.Advantage,
                opponentScore = PlayerScore.Forty,
                isUserServing = true,
                userName = "",
            )
        // Note: The user explicitly asked NOT to change announcement text,
        // so it should remain "Ad-In" even though display is "AD"
        assertEquals("Ad-In", generateAnnouncement(state))

        state =
            MatchState(
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
            MatchState(
                userScore = PlayerScore.TiebreakScore(3),
                opponentScore = PlayerScore.TiebreakScore(2),
                isUserServing = true,
            )
        assertEquals("Three Two", generateAnnouncement(state))

        // Opponent serving: opponent score first
        val state2 =
            MatchState(
                userScore = PlayerScore.TiebreakScore(3),
                opponentScore = PlayerScore.TiebreakScore(2),
                isUserServing = false,
            )
        assertEquals("Two Three", generateAnnouncement(state2))

        // Tied tiebreak scores
        val state3 =
            MatchState(
                userScore = PlayerScore.TiebreakScore(4),
                opponentScore = PlayerScore.TiebreakScore(4),
                isUserServing = true,
            )
        assertEquals("Four All", generateAnnouncement(state3))
    }

    @Test
    fun testGameWinningAnnouncement() {
        val state =
            MatchState(
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
            MatchState(
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
            MatchState(
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
            MatchState(
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
            MatchState(
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
            MatchState(
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
            MatchState(
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
        val initial = MatchState()
        assertTrue(initial.isScoreZero)

        val afterPoint =
            MatchState(userScore = PlayerScore.Fifteen)
        assertFalse(afterPoint.isScoreZero)

        val afterGame =
            MatchState(userGames = 1)
        assertFalse(afterGame.isScoreZero)

        val afterSet =
            MatchState(userSets = 1, setHistory = listOf(6 to 4))
        assertFalse(afterSet.isScoreZero)
    }

    @Test
    fun testBadmintonPointAnnouncement() {
        // Server's score first
        val state =
            MatchState(
                userScore = PlayerScore.TiebreakScore(5),
                opponentScore = PlayerScore.TiebreakScore(3),
                isUserServing = true,
                matchFormat = MatchFormat.BWF_STANDARD,
            )
        assertEquals("Five Three", generateAnnouncement(state))

        // Opponent serving: opponent score first
        val state2 =
            MatchState(
                userScore = PlayerScore.TiebreakScore(5),
                opponentScore = PlayerScore.TiebreakScore(3),
                isUserServing = false,
                matchFormat = MatchFormat.BWF_STANDARD,
            )
        assertEquals("Three Five", generateAnnouncement(state2))

        // Tied scores
        val state3 =
            MatchState(
                userScore = PlayerScore.TiebreakScore(10),
                opponentScore = PlayerScore.TiebreakScore(10),
                isUserServing = true,
                matchFormat = MatchFormat.BWF_STANDARD,
            )
        assertEquals("Ten All", generateAnnouncement(state3))
    }

    @Test
    fun testBadmintonGameWinAnnouncement() {
        val state =
            MatchState(
                userSets = 1,
                setWinner = "Player 1",
                setHistory = listOf(21 to 18),
                matchFormat = MatchFormat.BWF_STANDARD,
            )
        assertEquals("First Game, Player 1, Twenty-one Eighteen", generateAnnouncement(state))
    }

    @Test
    fun testBadmintonMatchWinAnnouncement() {
        val state =
            MatchState(
                userSets = 2,
                matchWinner = "Player 1",
                setHistory = listOf(21 to 18, 21 to 15),
                matchFormat = MatchFormat.BWF_STANDARD,
            )
        assertEquals(
            "Match, Player 1. Twenty-one Eighteen, Twenty-one Fifteen",
            generateAnnouncement(state),
        )
    }

    @Test
    fun testBadmintonNewGameAnnouncement() {
        val state =
            MatchState(
                userScore = PlayerScore.TiebreakScore(0),
                opponentScore = PlayerScore.TiebreakScore(0),
                isNewSet = true,
                userSets = 1,
                opponentSets = 0,
                isUserServing = true,
                userName = "Player 1",
                matchFormat = MatchFormat.BWF_STANDARD,
            )
        assertEquals("Second Game, Player 1 to serve.", generateAnnouncement(state))
    }

    @Test
    fun testPickleballRallyPointAnnouncement() {
        val state =
            MatchState(
                userScore = PlayerScore.TiebreakScore(7),
                opponentScore = PlayerScore.TiebreakScore(4),
                isUserServing = true,
                matchFormat = MatchFormat.PB_RALLY_11,
            )
        assertEquals("Seven Four", generateAnnouncement(state))
    }

    @Test
    fun testPickleballGameWinAnnouncement() {
        val state =
            MatchState(
                userSets = 1,
                setWinner = "Player 1",
                setHistory = listOf(11 to 5),
                matchFormat = MatchFormat.PB_RALLY_11,
            )
        assertEquals("First Game, Player 1, Eleven Five", generateAnnouncement(state))
    }

    @Test
    fun testPickleballMatchWinAnnouncement() {
        val state =
            MatchState(
                userSets = 2,
                matchWinner = "Player 1",
                setHistory = listOf(11 to 5, 11 to 8),
                matchFormat = MatchFormat.PB_RALLY_11,
            )
        assertEquals(
            "Match, Player 1. Eleven Five, Eleven Eight",
            generateAnnouncement(state),
        )
    }

    @Test
    fun testPickleballSideOutAnnouncement() {
        val state =
            MatchState(
                userScore = PlayerScore.TiebreakScore(5),
                opponentScore = PlayerScore.TiebreakScore(3),
                isUserServing = true,
                matchFormat = MatchFormat.PB_SIDEOUT,
            )
        assertEquals("Side out. Five Three", generateSideOutAnnouncement(state))

        // Tied scores
        val state2 =
            MatchState(
                userScore = PlayerScore.TiebreakScore(4),
                opponentScore = PlayerScore.TiebreakScore(4),
                isUserServing = true,
                matchFormat = MatchFormat.PB_SIDEOUT,
            )
        assertEquals("Side out. Four All", generateSideOutAnnouncement(state2))
    }

    @Test
    fun testTiebreakScoreTtsUsesWords() {
        assertEquals("Zero", PlayerScore.TiebreakScore(0).tts)
        assertEquals("One", PlayerScore.TiebreakScore(1).tts)
        assertEquals("Ten", PlayerScore.TiebreakScore(10).tts)
        assertEquals("Twenty-one", PlayerScore.TiebreakScore(21).tts)
        assertEquals("Thirty", PlayerScore.TiebreakScore(30).tts)
    }

    @Test
    fun testNumberToWordsAbove30() {
        assertEquals("Thirty-one", PlayerScore.TiebreakScore(31).tts)
        assertEquals("Thirty-five", PlayerScore.TiebreakScore(35).tts)
        assertEquals("Thirty-nine", PlayerScore.TiebreakScore(39).tts)
        assertEquals("Forty", PlayerScore.TiebreakScore(40).tts)
        assertEquals("Forty-one", PlayerScore.TiebreakScore(41).tts)
        assertEquals("Forty-nine", PlayerScore.TiebreakScore(49).tts)
        assertEquals("Fifty", PlayerScore.TiebreakScore(50).tts)
        assertEquals("Sixty", PlayerScore.TiebreakScore(60).tts)
        assertEquals("Seventy", PlayerScore.TiebreakScore(70).tts)
        assertEquals("Eighty", PlayerScore.TiebreakScore(80).tts)
        assertEquals("Ninety", PlayerScore.TiebreakScore(90).tts)
        assertEquals("Ninety-nine", PlayerScore.TiebreakScore(99).tts)
    }

    @Test
    fun testRallyAnnouncementUsesWords() {
        val state =
            MatchState(
                userScore = PlayerScore.TiebreakScore(5),
                opponentScore = PlayerScore.TiebreakScore(3),
                isUserServing = true,
                matchFormat = MatchFormat.BWF_STANDARD,
            )
        assertEquals("Five Three", generateAnnouncement(state))
    }

    @Test
    fun testRallyGameWinAnnouncementUsesWords() {
        val state =
            MatchState(
                userSets = 1,
                setWinner = "Player 1",
                setHistory = listOf(21 to 18),
                matchFormat = MatchFormat.BWF_STANDARD,
            )
        assertEquals("First Game, Player 1, Twenty-one Eighteen", generateAnnouncement(state))
    }

    @Test
    fun testRallyMatchWinAnnouncementUsesWords() {
        val state =
            MatchState(
                userSets = 2,
                matchWinner = "Player 1",
                setHistory = listOf(21 to 18, 21 to 15),
                matchFormat = MatchFormat.BWF_STANDARD,
            )
        assertEquals(
            "Match, Player 1. Twenty-one Eighteen, Twenty-one Fifteen",
            generateAnnouncement(state),
        )
    }

    @Test
    fun testBundleRoundTripDefaultState() {
        val original = MatchState()
        val restored = matchStateFromBundle(original.toBundle())
        assertEquals(original.userScore, restored.userScore)
        assertEquals(original.opponentScore, restored.opponentScore)
        assertEquals(original.userGames, restored.userGames)
        assertEquals(original.opponentGames, restored.opponentGames)
        assertEquals(original.userSets, restored.userSets)
        assertEquals(original.opponentSets, restored.opponentSets)
        assertEquals(original.isUserServing, restored.isUserServing)
        assertEquals(original.matchFormat, restored.matchFormat)
        assertEquals(original.isMatchTiebreak, restored.isMatchTiebreak)
        assertEquals(original.setHistory, restored.setHistory)
    }

    @Test
    fun testBundleRoundTripMidMatch() {
        val original =
            MatchState(
                userScore = PlayerScore.Thirty,
                opponentScore = PlayerScore.Fifteen,
                userGames = 3,
                opponentGames = 2,
                userSets = 1,
                opponentSets = 0,
                isUserServing = false,
                userName = "Alice",
                opponentName = "Bob",
                matchFormat = MatchFormat.LEAGUE,
                isMatchTiebreak = false,
                setHistory = listOf(6 to 4),
            )
        val restored = matchStateFromBundle(original.toBundle())
        assertEquals(original.userScore, restored.userScore)
        assertEquals(original.opponentScore, restored.opponentScore)
        assertEquals(original.userGames, restored.userGames)
        assertEquals(original.opponentGames, restored.opponentGames)
        assertEquals(original.userSets, restored.userSets)
        assertEquals(original.opponentSets, restored.opponentSets)
        assertEquals(original.isUserServing, restored.isUserServing)
        assertEquals(original.userName, restored.userName)
        assertEquals(original.opponentName, restored.opponentName)
        assertEquals(original.matchFormat, restored.matchFormat)
        assertEquals(original.isMatchTiebreak, restored.isMatchTiebreak)
        assertEquals(original.setHistory, restored.setHistory)
    }

    @Test
    fun testBundleRoundTripTiebreakScore() {
        val original =
            MatchState(
                userScore = PlayerScore.TiebreakScore(5),
                opponentScore = PlayerScore.TiebreakScore(3),
                userGames = 6,
                opponentGames = 6,
                isMatchTiebreak = false,
            )
        val restored = matchStateFromBundle(original.toBundle())
        assertTrue(restored.userScore is PlayerScore.TiebreakScore)
        assertEquals(5, (restored.userScore as PlayerScore.TiebreakScore).points)
        assertTrue(restored.opponentScore is PlayerScore.TiebreakScore)
        assertEquals(3, (restored.opponentScore as PlayerScore.TiebreakScore).points)
    }

    @Test
    fun testBundleRoundTripAdvantage() {
        val original =
            MatchState(
                userScore = PlayerScore.Advantage,
                opponentScore = PlayerScore.Forty,
            )
        val restored = matchStateFromBundle(original.toBundle())
        assertEquals(PlayerScore.Advantage, restored.userScore)
        assertEquals(PlayerScore.Forty, restored.opponentScore)
    }

    @Test
    fun testJsonRoundTripDefaultState() {
        val original = MatchState()
        val restored = matchStateFromJsonString(original.toJsonString())
        assertEquals(original.userScore, restored.userScore)
        assertEquals(original.opponentScore, restored.opponentScore)
        assertEquals(original.userGames, restored.userGames)
        assertEquals(original.opponentGames, restored.opponentGames)
        assertEquals(original.userSets, restored.userSets)
        assertEquals(original.opponentSets, restored.opponentSets)
        assertEquals(original.isUserServing, restored.isUserServing)
        assertEquals(original.matchFormat, restored.matchFormat)
        assertEquals(original.isMatchTiebreak, restored.isMatchTiebreak)
        assertEquals(original.setHistory, restored.setHistory)
    }

    @Test
    fun testJsonRoundTripMidMatch() {
        val original =
            MatchState(
                userScore = PlayerScore.Thirty,
                opponentScore = PlayerScore.Fifteen,
                userGames = 3,
                opponentGames = 2,
                userSets = 1,
                opponentSets = 0,
                isUserServing = false,
                userName = "Alice",
                opponentName = "Bob",
                matchFormat = MatchFormat.LEAGUE,
                isMatchTiebreak = false,
                setHistory = listOf(6 to 4),
            )
        val restored = matchStateFromJsonString(original.toJsonString())
        assertEquals(original.userScore, restored.userScore)
        assertEquals(original.opponentScore, restored.opponentScore)
        assertEquals(original.userGames, restored.userGames)
        assertEquals(original.opponentGames, restored.opponentGames)
        assertEquals(original.userSets, restored.userSets)
        assertEquals(original.opponentSets, restored.opponentSets)
        assertEquals(original.isUserServing, restored.isUserServing)
        assertEquals(original.userName, restored.userName)
        assertEquals(original.opponentName, restored.opponentName)
        assertEquals(original.matchFormat, restored.matchFormat)
        assertEquals(original.isMatchTiebreak, restored.isMatchTiebreak)
        assertEquals(original.setHistory, restored.setHistory)
    }

    @Test
    fun testJsonRoundTripTiebreakScore() {
        val original =
            MatchState(
                userScore = PlayerScore.TiebreakScore(5),
                opponentScore = PlayerScore.TiebreakScore(3),
                userGames = 6,
                opponentGames = 6,
                isMatchTiebreak = false,
            )
        val restored = matchStateFromJsonString(original.toJsonString())
        assertTrue(restored.userScore is PlayerScore.TiebreakScore)
        assertEquals(5, (restored.userScore as PlayerScore.TiebreakScore).points)
        assertTrue(restored.opponentScore is PlayerScore.TiebreakScore)
        assertEquals(3, (restored.opponentScore as PlayerScore.TiebreakScore).points)
    }

    @Test
    fun testJsonRoundTripAdvantage() {
        val original =
            MatchState(
                userScore = PlayerScore.Advantage,
                opponentScore = PlayerScore.Forty,
            )
        val restored = matchStateFromJsonString(original.toJsonString())
        assertEquals(PlayerScore.Advantage, restored.userScore)
        assertEquals(PlayerScore.Forty, restored.opponentScore)
    }

    @Test
    fun testServerFirstRule() {
        // Opponent serving, Receiver (User) wins first point
        var state =
            MatchState(
                userScore = PlayerScore.Fifteen,
                opponentScore = PlayerScore.Love,
                isUserServing = false,
            )
        assertEquals("Love 15", generateAnnouncement(state))

        // Opponent serving, Server (Opponent) wins first point
        state =
            MatchState(
                userScore = PlayerScore.Love,
                opponentScore = PlayerScore.Fifteen,
                isUserServing = false,
            )
        assertEquals("15 Love", generateAnnouncement(state))

        // Tied scores should still be "All"
        state =
            MatchState(
                userScore = PlayerScore.Fifteen,
                opponentScore = PlayerScore.Fifteen,
                isUserServing = false,
            )
        assertEquals("15 All", generateAnnouncement(state))
    }
}
