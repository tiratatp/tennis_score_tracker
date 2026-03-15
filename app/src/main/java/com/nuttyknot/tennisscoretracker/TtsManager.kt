package com.nuttyknot.tennisscoretracker

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts =
            TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.US
                    isInitialized = true
                }
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
