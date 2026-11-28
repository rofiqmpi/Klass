package com.mpi.klass

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

private const val FCM_TAG = "KlassMessagingService"

class KlassMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(FCM_TAG, "Refreshed FCM token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "MPI Klass"
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: remoteMessage.data["message"]
            ?: "You have a new notification."

        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        val channelId = CHANNEL_ANNOUNCEMENTS

        postSystemNotification(
            context = applicationContext,
            notificationId = notificationId,
            channelId = channelId,
            title = title,
            body = body,
        )
    }
}
