package com.nuttyknot.tennisscoretracker

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsManager(context: Context) {
    private var tts: TextToSpeech? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private lateinit var audioAttributes: AudioAttributes
    private var focusRequest: AudioFocusRequest? = null

    @Volatile
    private var isInitialized = false
    private var defaultVoice: android.speech.tts.Voice? = null

    private val _availableVoices = MutableStateFlow<List<String>>(emptyList())
    val availableVoices: StateFlow<List<String>> = _availableVoices.asStateFlow()

    private val focusListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                -> tts?.stop()
            }
        }

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

                        audioAttributes =
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build()
                        tts?.setAudioAttributes(audioAttributes)

                        tts?.setOnUtteranceProgressListener(
                            object : UtteranceProgressListener() {
                                override fun onStart(utteranceId: String?) = Unit

                                override fun onDone(utteranceId: String?) {
                                    abandonFocus()
                                }

                                override fun onError(utteranceId: String?) {
                                    abandonFocus()
                                }
                            },
                        )

                        defaultVoice = tts?.voice
                        populateAvailableVoices()
                    }
                }
            }
    }

    private fun populateAvailableVoices() {
        val allVoices = tts?.voices ?: return
        val ukLocales = setOf(Locale.UK, Locale("en", "GB"))
        val enLocales = ukLocales + setOf(Locale.US, Locale.ENGLISH, Locale("en", "AU"))
        val speakerVoices = allVoices.filter { !it.name.endsWith("-language") }

        val localUk = speakerVoices.filter { !it.isNetworkConnectionRequired && it.locale in ukLocales }
        val localEn = speakerVoices.filter { !it.isNetworkConnectionRequired && it.locale in enLocales }
        val networkUk = speakerVoices.filter { it.isNetworkConnectionRequired && it.locale in ukLocales }
        val networkEn = speakerVoices.filter { it.isNetworkConnectionRequired && it.locale in enLocales }

        val chosen = localUk.ifEmpty { localEn }.ifEmpty { networkUk }.ifEmpty { networkEn }
        _availableVoices.value = chosen.map { it.name }.sorted()
    }

    fun setVoice(voiceName: String) {
        if (!isInitialized) return
        if (voiceName.isEmpty()) {
            tts?.voice = defaultVoice
            return
        }
        val match = tts?.voices?.find { it.name == voiceName }
        if (match != null) {
            tts?.voice = match
        }
    }

    fun announce(text: String) {
        if (!isInitialized) return

        val request =
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener(focusListener)
                .build()
        focusRequest = request

        val result = audioManager.requestAudioFocus(request)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
        }
    }

    private fun abandonFocus() {
        focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
    }

    fun shutdown() {
        tts?.stop()
        abandonFocus()
        tts?.shutdown()
    }

    private companion object {
        const val UMPIRE_SPEECH_RATE = 0.90f
        const val UMPIRE_PITCH = 0.95f
        const val UTTERANCE_ID = "score_announcement"
    }
}
