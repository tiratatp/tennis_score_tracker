package com.nuttyknot.tennisscoretracker.wear

import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.WearableListenerService

class WearDataListenerService : WearableListenerService() {
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        // DataClient.OnDataChangedListener in WearRemoteViewModel handles active updates.
        // This service ensures data events are delivered even when the app is in the background,
        // which triggers the system to launch the app process if needed.
    }
}
