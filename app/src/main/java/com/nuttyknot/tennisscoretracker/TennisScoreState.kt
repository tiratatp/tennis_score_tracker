package com.nuttyknot.tennisscoretracker

sealed class PlayerScore(val display: String) {
    object Love : PlayerScore("Love")
    object Fifteen : PlayerScore("15")
    object Thirty : PlayerScore("30")
    object Forty : PlayerScore("40")
    object Advantage : PlayerScore("Ad")

    override fun toString(): String = display
}

data class TennisMatchState(
    val userScore: PlayerScore = PlayerScore.Love,
    val opponentScore: PlayerScore = PlayerScore.Love,
    val userGames: Int = 0,
    val opponentGames: Int = 0,
    val userSets: Int = 0,
    val opponentSets: Int = 0,
    val isDeuce: Boolean = false
) {
    fun toTtsString(): String {
        // Special announcements for game/set wins would ideally be handled right after the score action.
        // This method describes the current point score.
        if (isDeuce) return "Deuce"
        if (userScore is PlayerScore.Advantage) return "Advantage User"
        if (opponentScore is PlayerScore.Advantage) return "Advantage Opponent"
        if (userScore == opponentScore && userScore != PlayerScore.Love) return "${userScore.display} all"
        return "${userScore.display} ${opponentScore.display}"
    }
}
