package com.nuttyknot.tennisscoretracker

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.nuttyknot.tennisscoretracker.shared.WearConstants

class PhoneWearListenerService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        if (messageEvent.path == WearConstants.PATH_COMMAND) {
            val command = String(messageEvent.data)
            if (command == WearConstants.CMD_LAUNCH_APP) {
                val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }
}
