package com.nuttyknot.tennisscoretracker

import android.view.KeyEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class KeyEventManager(
    private val scope: CoroutineScope,
    private val onSingleClick: () -> Unit,
    private val onDoubleClick: () -> Unit,
    private val onLongPress: () -> Unit,
    private val timeProvider: () -> Long = { System.currentTimeMillis() },
) {
    var targetKeyCode: Int = SettingsManager.DEFAULT_KEYCODE
    var doubleClickLatency: Long = SettingsManager.DEFAULT_DOUBLE_CLICK_LATENCY
    var longPressLatency: Long = SettingsManager.DEFAULT_LONG_PRESS_LATENCY

    private var clickCount = 0
    private var doubleClickJob: Job? = null
    private var longPressJob: Job? = null
    private var isLongPressFired = false

    // Some bluetooth shutters send simultaneous ENTER and VOLUME_UP events.
    // We can debounce them slightly or just treat them as identical triggers.
    private var lastEventTime = 0L

    fun onKeyDown(
        keyCode: Int,
        event: KeyEvent?,
    ): Boolean {
        if (isTargetKey(keyCode)) {
            val isRepeat = event?.let { it.repeatCount > 0 } ?: false
            val currentTime = timeProvider()
            val isDebounced = (currentTime - lastEventTime) < DEBOUNCE_DELAY_MS

            if (!isRepeat && !isDebounced) {
                lastEventTime = currentTime
                if (longPressJob == null && !isLongPressFired) {
                    longPressJob =
                        scope.launch {
                            delay(longPressLatency)
                            isLongPressFired = true
                            onLongPress()
                        }
                }
            }
            return true
        }
        return false
    }

    fun onKeyUp(keyCode: Int): Boolean {
        if (!isTargetKey(keyCode)) return false

        longPressJob?.cancel()
        longPressJob = null

        if (isLongPressFired) {
            isLongPressFired = false
        } else {
            handleKeyUpClick()
        }
        return true
    }

    private fun handleKeyUpClick() {
        clickCount++
        if (clickCount == 1) {
            doubleClickJob =
                scope.launch {
                    delay(doubleClickLatency)
                    if (clickCount == 1) {
                        onSingleClick()
                    }
                    clickCount = 0
                }
        } else if (clickCount == 2) {
            doubleClickJob?.cancel()
            onDoubleClick()
            clickCount = 0
        }
    }

    private fun isTargetKey(keyCode: Int): Boolean {
        val isDefaultVolumeUp = targetKeyCode == KeyEvent.KEYCODE_VOLUME_UP
        if (isDefaultVolumeUp && keyCode == KeyEvent.KEYCODE_ENTER) return true
        return keyCode == targetKeyCode
    }

    companion object {
        internal const val DEBOUNCE_DELAY_MS = 50L
    }
}
