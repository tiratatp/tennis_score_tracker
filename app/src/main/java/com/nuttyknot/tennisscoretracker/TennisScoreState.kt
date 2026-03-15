package com.nuttyknot.tennisscoretracker

import java.util.*

sealed class PlayerScore(val display: String, val tts: String = display) {
    object Love : PlayerScore("0", "Love")
    object Fifteen : PlayerScore("15")
    object Thirty : PlayerScore("30")
    object Forty : PlayerScore("40")
    object Advantage : PlayerScore("Ad", "Advantage")

    override fun toString(): String = display
}

data class TennisMatchState(
    val userScore: PlayerScore = PlayerScore.Love,
    val opponentScore: PlayerScore = PlayerScore.Love,
    val userGames: Int = 0,
    val opponentGames: Int = 0,
    val userSets: Int = 0,
    val opponentSets: Int = 0,
    val isDeuce: Boolean = false,
    val isUserServing: Boolean = true,
    val userName: String = "",
    val opponentName: String = "",
    val gameWinner: String? = null,
    val setWinner: String? = null,
    val matchWinner: String? = null,
    val setHistory: List<Pair<Int, Int>> = emptyList(), // Store (userGames, opponentGames) for completed sets
    val isNewSet: Boolean = false,
    val announcement: String? = null
)

// Pure function to generate announcements from state
fun generateAnnouncement(state: TennisMatchState): String {
    if (state.matchWinner != null) {
        var historyStr = ""
        for (i in state.setHistory.indices) {
            val pair = state.setHistory[i]
            historyStr += "${pair.first} ${pair.second}"
            if (i < state.setHistory.size - 1) historyStr += ", "
        }
        return "Game, Set, and Match, ${state.matchWinner}. $historyStr"
    }

    if (state.setWinner != null) {
        val lastSet = if (state.setHistory.isNotEmpty()) state.setHistory[state.setHistory.size - 1] else Pair(0, 0)
        val scoreString = if (lastSet.first > lastSet.second) "${lastSet.first} ${lastSet.second}" else "${lastSet.second} ${lastSet.first}"
        val setOrdinal = when (state.userSets + state.opponentSets) {
            1 -> "First"
            2 -> "Second"
            3 -> "Third"
            4 -> "Fourth"
            5 -> "Fifth"
            else -> "${state.userSets + state.opponentSets}"
        }
        return "Game and $setOrdinal Set, ${state.setWinner}, $scoreString"
    }

    if (state.isNewSet && state.userScore == PlayerScore.Love && state.opponentScore == PlayerScore.Love) {
        val serverName = if (state.isUserServing) {
            if (state.userName.isEmpty()) "User" else state.userName
        } else {
            if (state.opponentName.isEmpty()) "Opponent" else state.opponentName
        }
        val setOrdinal = when (state.userSets + state.opponentSets + 1) {
            1 -> "First"
            2 -> "Second"
            3 -> "Third"
            4 -> "Fourth"
            5 -> "Fifth"
            else -> "${state.userSets + state.opponentSets + 1}"
        }
        return "$setOrdinal Set, $serverName to serve. Play."
    }

    if (state.gameWinner != null) {
        val summary = if (state.userGames == state.opponentGames) {
            "Games are ${state.userGames} All"
        } else {
            val leaderName = if (state.userGames > state.opponentGames) {
                if (state.userName.isEmpty()) "User" else state.userName
            } else {
                if (state.opponentName.isEmpty()) "Opponent" else state.opponentName
            }
            val leads = if (state.userGames > state.opponentGames) state.userGames else state.opponentGames
            val trailing = if (state.userGames > state.opponentGames) state.opponentGames else state.userGames
            val gameWord = if (leads == 1) "game" else "games"
            "$leaderName leads $leads $gameWord to $trailing"
        }
        return "Game, ${state.gameWinner}. $summary"
    }

    val serverScore = if (state.isUserServing) state.userScore else state.opponentScore
    val receiverScore = if (state.isUserServing) state.opponentScore else state.userScore

    if (state.isDeuce) return "Deuce"

    if (serverScore is PlayerScore.Advantage) {
        val name = if (state.isUserServing) state.userName else state.opponentName
        return if (name.trim().isEmpty()) "Ad-In" else "Advantage, $name"
    }

    if (receiverScore is PlayerScore.Advantage) {
        val name = if (state.isUserServing) state.opponentName else state.userName
        return if (name.trim().isEmpty()) "Ad-Out" else "Advantage, $name"
    }

    if (serverScore == receiverScore) {
        return "${serverScore.tts} All"
    }

    return "${serverScore.tts} ${receiverScore.tts}"
}
