package com.sameh.chatme.data.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sameh.chatme.R
import com.sameh.chatme.ui.activities.MainActivity
import kotlin.random.Random

class FirebaseService : FirebaseMessagingService() {

    val CHANNEL_ID = "my_notification_channel"
    companion object{
        var sharedPref:SharedPreferences? = null

        var token:String?
        get(){
            return sharedPref?.getString("token","")
        }
        set(value){
            sharedPref?.edit()?.putString("token",value)?.apply()
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        token = p0
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        val intent = Intent(this,MainActivity::class.java)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = Random.nextInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this,0,intent,FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this,CHANNEL_ID)
            .setContentTitle(p0.data["title"])
            .setContentText(p0.data["body"])
            .setSmallIcon(R.drawable.ic_notification_tutor)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId,notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        println("Receive Notification")

        val channelName = "ChannelFirebaseChat"
        val channel = NotificationChannel(CHANNEL_ID,channelName,IMPORTANCE_HIGH).apply {
            description=""
            enableLights(true)
            lightColor = Color.WHITE
        }
        notificationManager.createNotificationChannel(channel)

    }
}