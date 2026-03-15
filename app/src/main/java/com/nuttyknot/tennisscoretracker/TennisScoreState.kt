package com.nuttyknot.tennisscoretracker

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
    /** Store (userGames, opponentGames) for completed sets */
    val setHistory: List<Pair<Int, Int>> = emptyList(),
    val isNewSet: Boolean = false,
    val announcement: String? = null,
)

// Pure function to generate announcements from state
fun generateAnnouncement(state: TennisMatchState): String {
    val isNewSetLoveAll =
        state.isNewSet && state.userScore == PlayerScore.Love &&
            state.opponentScore == PlayerScore.Love
    return when {
        state.matchWinner != null -> generateMatchWinningAnnouncement(state)
        state.setWinner != null -> generateSetWinningAnnouncement(state)
        isNewSetLoveAll -> generateNewSetAnnouncement(state)
        state.gameWinner != null -> generateGameWinningAnnouncement(state)
        else -> generatePointScoreAnnouncement(state)
    }
}

private fun generateMatchWinningAnnouncement(state: TennisMatchState): String {
    val historyStr = state.setHistory.joinToString(", ") { "${it.first} ${it.second}" }
    return "Game, Set, and Match, ${state.matchWinner}. $historyStr"
}

private fun generateSetWinningAnnouncement(state: TennisMatchState): String {
    val lastSet = state.setHistory.lastOrNull() ?: Pair(0, 0)
    val scoreString =
        if (lastSet.first > lastSet.second) {
            "${lastSet.first} ${lastSet.second}"
        } else {
            "${lastSet.second} ${lastSet.first}"
        }
    val setOrdinal = getOrdinalString(state.userSets + state.opponentSets)
    return "Game and $setOrdinal Set, ${state.setWinner}, $scoreString"
}

private fun generateNewSetAnnouncement(state: TennisMatchState): String {
    val serverName =
        if (state.isUserServing) {
            state.userName.ifEmpty { "User" }
        } else {
            state.opponentName.ifEmpty { "Opponent" }
        }
    val setOrdinal = getOrdinalString(state.userSets + state.opponentSets + 1)
    return "$setOrdinal Set, $serverName to serve. Play."
}

private fun generateGameWinningAnnouncement(state: TennisMatchState): String {
    val summary =
        if (state.userGames == state.opponentGames) {
            "Games are ${state.userGames} All"
        } else {
            val isUserLeading = state.userGames > state.opponentGames
            val leaderName =
                if (isUserLeading) {
                    state.userName.ifEmpty { "User" }
                } else {
                    state.opponentName.ifEmpty { "Opponent" }
                }
            val leads = if (isUserLeading) state.userGames else state.opponentGames
            val trailing = if (isUserLeading) state.opponentGames else state.userGames
            val gameWord = if (leads == 1) "game" else "games"
            "$leaderName leads $leads $gameWord to $trailing"
        }
    return "Game, ${state.gameWinner}. $summary"
}

private fun generatePointScoreAnnouncement(state: TennisMatchState): String {
    if (state.isDeuce) return "Deuce"

    val serverScore = if (state.isUserServing) state.userScore else state.opponentScore
    val receiverScore = if (state.isUserServing) state.opponentScore else state.userScore

    return when {
        serverScore is PlayerScore.Advantage -> {
            val name = if (state.isUserServing) state.userName else state.opponentName
            if (name.trim().isEmpty()) "Ad-In" else "Advantage, $name"
        }
        receiverScore is PlayerScore.Advantage -> {
            val name = if (state.isUserServing) state.opponentName else state.userName
            if (name.trim().isEmpty()) "Ad-Out" else "Advantage, $name"
        }
        serverScore == receiverScore -> "${serverScore.tts} All"
        else -> "${serverScore.tts} ${receiverScore.tts}"
    }
}

private object OrdinalConstants {
    const val THIRD = 3
    const val FOURTH = 4
    const val FIFTH = 5
}

private fun getOrdinalString(value: Int): String {
    return when (value) {
        1 -> "First"
        2 -> "Second"
        OrdinalConstants.THIRD -> "Third"
        OrdinalConstants.FOURTH -> "Fourth"
        OrdinalConstants.FIFTH -> "Fifth"
        else -> "$value"
    }
}
