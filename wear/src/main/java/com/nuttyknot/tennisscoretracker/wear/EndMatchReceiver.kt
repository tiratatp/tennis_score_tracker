package com.nuttyknot.tennisscoretracker.wear

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import com.nuttyknot.tennisscoretracker.shared.WearConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EndMatchReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val capabilityClient = Wearable.getCapabilityClient(context)
                val messageClient = Wearable.getMessageClient(context)
                val info =
                    capabilityClient
                        .getCapability(
                            BuildConfig.CAPABILITY_PHONE_APP,
                            CapabilityClient.FILTER_REACHABLE,
                        ).await()
                val nodeId =
                    info.nodes.firstOrNull { it.isNearby }?.id
                        ?: info.nodes.firstOrNull()?.id
                        ?: return@launch
                messageClient
                    .sendMessage(
                        nodeId,
                        WearConstants.PATH_COMMAND,
                        WearConstants.CMD_END_MATCH.toByteArray(),
                    ).await()
            } catch (e: ApiException) {
                Log.e(TAG, "Error ending match", e)
            } catch (
                @Suppress("TooGenericExceptionCaught")
                e: Exception,
            ) {
                Log.e(TAG, "Unexpected error ending match", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "EndMatchReceiver"
    }
}
