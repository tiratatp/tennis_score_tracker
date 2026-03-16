package com.nuttyknot.tennisscoretracker

import android.content.Context
import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsManager(context: Context) {
    private var tts: TextToSpeech? = null

    @Volatile
    private var isInitialized = false

    init {
        tts =
            TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.UK)
                    if (result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
                    ) {
                        isInitialized = true
                        tts?.setSpeechRate(UMPIRE_SPEECH_RATE)
                        tts?.setPitch(UMPIRE_PITCH)

                        val audioAttributes =
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build()
                        tts?.setAudioAttributes(audioAttributes)
                    }
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

    private companion object {
        const val UMPIRE_SPEECH_RATE = 0.90f
        const val UMPIRE_PITCH = 0.95f
    }
}
