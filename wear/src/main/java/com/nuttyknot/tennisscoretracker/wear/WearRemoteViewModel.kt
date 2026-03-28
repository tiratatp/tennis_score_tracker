package com.nuttyknot.tennisscoretracker.wear

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import com.nuttyknot.tennisscoretracker.shared.WearConstants
import com.nuttyknot.tennisscoretracker.shared.WearScoreDisplay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONException

@Suppress("TooManyFunctions")
class WearRemoteViewModel(application: Application) :
    AndroidViewModel(application),
    DataClient.OnDataChangedListener,
    CapabilityClient.OnCapabilityChangedListener {
    private val messageClient: MessageClient = Wearable.getMessageClient(application)
    private val dataClient: DataClient = Wearable.getDataClient(application)
    private val capabilityClient: CapabilityClient = Wearable.getCapabilityClient(application)

    private val _scoreDisplay = MutableStateFlow(WearScoreDisplay())
    val scoreDisplay: StateFlow<WearScoreDisplay> = _scoreDisplay.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var isListening = false
    private var isCapabilityListening = false
    private var cachedNodeId: String? = null

    fun startListening() {
        if (isListening) return
        isListening = true
        dataClient.addListener(this)
        startCapabilityListener()
        loadCurrentData()
    }

    private fun stopListening() {
        if (!isListening) return
        isListening = false
        dataClient.removeListener(this)
        stopCapabilityListener()
    }

    private fun startCapabilityListener() {
        if (isCapabilityListening) {
            return
        }
        isCapabilityListening = true
        capabilityClient.addListener(
            this,
            BuildConfig.CAPABILITY_PHONE_APP,
        )
        viewModelScope.launch {
            try {
                val info =
                    capabilityClient.getCapability(
                        BuildConfig.CAPABILITY_PHONE_APP,
                        CapabilityClient.FILTER_REACHABLE,
                    ).await()
                updateConnectionFromCapability(info)
            } catch (e: ApiException) {
                Log.e(TAG, "Error querying capability", e)
                _isConnected.value = false
            } catch (
                @Suppress("TooGenericExceptionCaught")
                e: Exception,
            ) {
                Log.e(TAG, "Unexpected error querying capability", e)
                _isConnected.value = false
            }
        }
    }

    private fun stopCapabilityListener() {
        if (!isCapabilityListening) {
            return
        }
        isCapabilityListening = false
        capabilityClient.removeListener(this)
    }

    fun onAmbientStateChanged(isAmbient: Boolean) {
        if (isAmbient) {
            stopCapabilityListener()
        } else {
            startCapabilityListener()
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        updateConnectionFromCapability(capabilityInfo)
    }

    private fun updateConnectionFromCapability(capabilityInfo: CapabilityInfo) {
        val node =
            capabilityInfo.nodes.firstOrNull { it.isNearby }
                ?: capabilityInfo.nodes.firstOrNull()
        cachedNodeId = node?.id
        _isConnected.value = node != null
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
            } catch (e: JSONException) {
                Log.e(TAG, "Error parsing score JSON", e)
            } catch (
                @Suppress("TooGenericExceptionCaught")
                e: Exception,
            ) {
                Log.e(TAG, "Unexpected error loading data", e)
            }
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (i in 0 until dataEvents.count) {
            val event = dataEvents[i]
            if (event.type == DataEvent.TYPE_CHANGED) {
                handleScoreEvent(event)
            }
        }
    }

    private fun handleScoreEvent(event: DataEvent) {
        val dataItem = event.dataItem
        if (dataItem.uri.path != WearConstants.PATH_SCORE) return
        val json =
            DataMapItem.fromDataItem(dataItem).dataMap
                .getString(WearConstants.KEY_SCORE_JSON) ?: return
        try {
            _scoreDisplay.value = WearScoreDisplay.fromJson(json)
            _isConnected.value = true
        } catch (e: JSONException) {
            Log.e(TAG, "Error parsing score JSON", e)
        }
    }

    fun sendCommand(command: String) {
        viewModelScope.launch {
            try {
                val nodeId = cachedNodeId ?: return@launch
                messageClient.sendMessage(
                    nodeId,
                    WearConstants.PATH_COMMAND,
                    command.toByteArray(),
                ).await()
            } catch (e: ApiException) {
                Log.e(TAG, "Error sending command: $command", e)
                _isConnected.value = false
            } catch (
                @Suppress("TooGenericExceptionCaught")
                e: Exception,
            ) {
                Log.e(TAG, "Unexpected error sending command: $command", e)
                _isConnected.value = false
            }
        }
    }

    companion object {
        private const val TAG = "WearRemoteViewModel"
    }
}
