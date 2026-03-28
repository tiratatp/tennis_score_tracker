package com.nuttyknot.tennisscoretracker

enum class MatchFormat(
    val displayName: String,
    val sport: Sport,
) {
    // Tennis
    STANDARD("Standard Match (Best of 3 Sets)", Sport.TENNIS),
    LEAGUE("League Match (3rd Set Tiebreak)", Sport.TENNIS),
    FAST("Fast Match (8-Game Pro Set, No-Ad)", Sport.TENNIS),

    // Badminton
    BWF_STANDARD("BWF Standard (Best of 3 to 21)", Sport.BADMINTON),
    BWF_SHORT("Short Format (Best of 5 to 11)", Sport.BADMINTON),

    // Pickleball
    PB_RALLY_11("Rally Scoring (to 11, Best of 3)", Sport.PICKLEBALL),
    PB_RALLY_15("Rally Scoring (to 15, Best of 3)", Sport.PICKLEBALL),
    PB_RALLY_21("Rally Scoring (to 21, Best of 3)", Sport.PICKLEBALL),
    PB_SIDEOUT("Side-Out Singles (to 11, Best of 3)", Sport.PICKLEBALL),
}
