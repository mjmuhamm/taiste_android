package com.ruh.taiste

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    val TAG = "FirebaseMessagingService"
    private val mHandler: Handler = Handler(Looper.getMainLooper())

    @SuppressLint("LongLogTag")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        Log.d(TAG, "msg received: ${remoteMessage.from}")
        val title = remoteMessage.notification!!.title
        val message = remoteMessage.notification!!.body

        mHandler.post {
            Toast.makeText(this, "${remoteMessage.notification!!.title} | ${remoteMessage.notification!!.body}", Toast.LENGTH_LONG).show()
            if (remoteMessage.notification != null) {
                showNotification(
                    remoteMessage.notification?.title,
                    remoteMessage.notification?.body
                )
            }
        }
    }
    override fun onNewToken(token: String) {
        Log.d("TAG", "Refreshed token: $token")
    }
    @SuppressLint("UnspecifiedImmutableFlag")
    private fun showNotification(title: String?, body: String?) {

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(soundUri)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }
}