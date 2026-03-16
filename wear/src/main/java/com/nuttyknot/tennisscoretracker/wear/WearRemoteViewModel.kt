package com.nuttyknot.tennisscoretracker.wear

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import com.nuttyknot.tennisscoretracker.shared.WearConstants
import com.nuttyknot.tennisscoretracker.shared.WearScoreDisplay
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WearRemoteViewModel(application: Application) :
    AndroidViewModel(application),
    DataClient.OnDataChangedListener {
    private val messageClient: MessageClient = Wearable.getMessageClient(application)
    private val dataClient: DataClient = Wearable.getDataClient(application)
    private val nodeClient: NodeClient = Wearable.getNodeClient(application)

    private val _scoreDisplay = MutableStateFlow(WearScoreDisplay())
    val scoreDisplay: StateFlow<WearScoreDisplay> = _scoreDisplay.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var isListening = false

    fun startListening() {
        if (isListening) return
        isListening = true
        dataClient.addListener(this)
        loadCurrentData()
        startConnectionPolling()
    }

    private fun stopListening() {
        if (!isListening) return
        isListening = false
        dataClient.removeListener(this)
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }

    private fun loadCurrentData() {
        viewModelScope.launch {
            try {
                val dataItems = dataClient.getDataItems().await()
                try {
                    for (i in 0 until dataItems.count) {
                        val item = dataItems[i]
                        if (item.uri.path == WearConstants.PATH_SCORE) {
                            val dataMap = DataMapItem.fromDataItem(item).dataMap
                            val json = dataMap.getString(WearConstants.KEY_SCORE_JSON)
                            if (json != null) {
                                _scoreDisplay.value = WearScoreDisplay.fromJson(json)
                            }
                        }
                    }
                } finally {
                    dataItems.release()
                }
            } catch (e: ApiException) {
                Log.e(TAG, "Error loading current data", e)
            }
        }
    }

    private fun startConnectionPolling() {
        viewModelScope.launch {
            while (isListening) {
                try {
                    val nodes = nodeClient.connectedNodes.await()
                    _isConnected.value = nodes.isNotEmpty()
                } catch (e: ApiException) {
                    Log.e(TAG, "Error checking connection", e)
                    _isConnected.value = false
                }
                delay(CONNECTION_CHECK_INTERVAL_MS)
            }
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (i in 0 until dataEvents.count) {
            handleScoreEvent(dataEvents[i].dataItem)
        }
    }

    private fun handleScoreEvent(dataItem: DataItem) {
        if (dataItem.uri.path != WearConstants.PATH_SCORE) return
        val json =
            DataMapItem.fromDataItem(dataItem).dataMap
                .getString(WearConstants.KEY_SCORE_JSON) ?: return
        _scoreDisplay.value = WearScoreDisplay.fromJson(json)
        _isConnected.value = true
    }

    fun sendCommand(command: String) {
        viewModelScope.launch {
            try {
                val nodes = nodeClient.connectedNodes.await()
                _isConnected.value = nodes.isNotEmpty()
                for (node in nodes) {
                    messageClient.sendMessage(
                        node.id,
                        WearConstants.PATH_COMMAND,
                        command.toByteArray(),
                    ).await()
                }
            } catch (e: ApiException) {
                Log.e(TAG, "Error sending command: $command", e)
                _isConnected.value = false
            }
        }
    }

    companion object {
        private const val TAG = "WearRemoteViewModel"
        private const val CONNECTION_CHECK_INTERVAL_MS = 10_000L
    }
}
