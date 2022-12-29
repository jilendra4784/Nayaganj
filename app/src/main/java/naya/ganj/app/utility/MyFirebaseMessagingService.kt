package naya.ganj.app.utility


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import naya.ganj.app.R
import naya.ganj.app.SplashActivity


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e(TAG, "onMessageReceived: ", )
        Log.d(
            "firebasemessage_data",
            "" + remoteMessage.data.toString() + " " + remoteMessage.data.size
        )
        try {
            if (remoteMessage.notification != null) {
                createNotification(
                    remoteMessage.notification?.title,
                    remoteMessage.notification?.body
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            if (remoteMessage.data.isNotEmpty()) {

                createNotification(remoteMessage.data.get("title"), remoteMessage.data.get("body"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun createNotification(title: String?, messageBody: String?) {

        count++
        val pattern = longArrayOf(500, 500, 500, 500, 500, 500, 500, 500, 500)
        val intent = Intent(applicationContext, SplashActivity::class.java)
        //you can use your launcher Activity insted of SplashActivity, But if the Activity you used here is not launcher Activty than its not work when App is in background.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        //Add Any key-value to pass extras to intent
        intent.putExtra("pushnotification", title)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val mNotifyManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = resources.getString(R.string.default_notification_channel_id)

        //For Android Version Orio and greater than orio.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(
                channelId,
                resources.getString(R.string.notification_channel_name),
                importance
            )
            mNotifyManager.createNotificationChannel(mChannel)
        }
        //For Android Version lower than orio.
        val mBuilder = NotificationCompat.Builder(this, channelId)
        mBuilder.setContentTitle(title)
            .setContentText(messageBody)
            .setSmallIcon(R.drawable.notification_alert)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.notification_alert))
            .setAutoCancel(false)
            .setVibrate(pattern)
            .setSound(defaultSoundUri)
            .setColor(ContextCompat.getColor(this, R.color.purple_500))
            //.setContentIntent(pendingIntent)
            .setNumber(count)
            .setChannelId(channelId)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(pendingIntent, true)

        mNotifyManager.notify(count, mBuilder.build())

        Log.d("mNotifyManager_count", "" + count)
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
        private var count = 0
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
    }
}