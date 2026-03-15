package com.nuttyknot.tennisscoretracker

import android.content.Context
import android.view.KeyEvent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Property delegate for DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    companion object {
        val KEYCODE = intPreferencesKey("keycode")
        val DOUBLE_CLICK_LATENCY = longPreferencesKey("double_click_latency")
        val LONG_PRESS_LATENCY = longPreferencesKey("long_press_latency")

        const val DEFAULT_KEYCODE = KeyEvent.KEYCODE_VOLUME_UP
        const val DEFAULT_DOUBLE_CLICK_LATENCY = 300L
        const val DEFAULT_LONG_PRESS_LATENCY = 1000L
    }

    val keycodeFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEYCODE] ?: DEFAULT_KEYCODE
    }

    val doubleClickLatencyFlow: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[DOUBLE_CLICK_LATENCY] ?: DEFAULT_DOUBLE_CLICK_LATENCY
    }

    val longPressLatencyFlow: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LONG_PRESS_LATENCY] ?: DEFAULT_LONG_PRESS_LATENCY
    }

    suspend fun updateKeycode(keycode: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEYCODE] = keycode
        }
    }

    suspend fun updateDoubleClickLatency(latency: Long) {
        context.dataStore.edit { preferences ->
            preferences[DOUBLE_CLICK_LATENCY] = latency
        }
    }

    suspend fun updateLongPressLatency(latency: Long) {
        context.dataStore.edit { preferences ->
            preferences[LONG_PRESS_LATENCY] = latency
        }
    }
}
