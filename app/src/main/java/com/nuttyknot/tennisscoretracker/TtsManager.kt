package com.nuttyknot.tennisscoretracker

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                isInitialized = true
            }
        }
    }

    fun announceScore(state: TennisMatchState) {
        if (isInitialized) {
            val utterance = state.toTtsString()
            // QUEUE_FLUSH interrupts any current TTS right away
            tts?.speak(utterance, TextToSpeech.QUEUE_FLUSH, null, "score_utterance")
        }
    }

    fun announce(text: String) {
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "custom_utterance")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
