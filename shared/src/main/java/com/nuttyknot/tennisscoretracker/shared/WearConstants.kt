package com.nuttyknot.tennisscoretracker.shared

object WearConstants {
    const val PATH_SCORE = "/tennis/score"
    const val PATH_COMMAND = "/tennis/command"
    const val KEY_SCORE_JSON = "score_json"
    const val KEY_TIMESTAMP = "timestamp"

    const val CMD_USER_SCORED = "user_scored"
    const val CMD_OPPONENT_SCORED = "opponent_scored"
    const val CMD_UNDO = "undo"
    const val CMD_RESET = "reset"
    const val CMD_LAUNCH_APP = "launch_app"

    const val CAPABILITY_PHONE_APP = "tennis_score_tracker_phone"
}
