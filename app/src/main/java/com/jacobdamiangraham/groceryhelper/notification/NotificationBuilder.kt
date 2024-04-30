package com.jacobdamiangraham.groceryhelper.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.jacobdamiangraham.groceryhelper.MainActivity
import com.jacobdamiangraham.groceryhelper.R

class NotificationBuilder(private val context: Context) {

    private var notificationManager: NotificationManager
    private var notificationChannelId: String
    private var notificationChannelName: String
    private var notificationChannelDescription: String

    private var notificationChannelImportance: Int = 0

    init {
        notificationChannelImportance = NotificationManager.IMPORTANCE_DEFAULT
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationChannelId = "GroceryHelper"
        notificationChannelName = "GroceryHelper channel name"
        notificationChannelDescription = "GroceryHelper channel description"
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(notificationChannelId, notificationChannelName, notificationChannelImportance).apply{
            description = notificationChannelDescription
        }
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(notificationName: String, notificationDescription: String): NotificationCompat.Builder {
        createNotificationChannel()
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(R.drawable.grocery_helper_app_icon_mdpi)
            .setContentTitle(notificationName)
            .setContentText(notificationDescription)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        return notificationBuilder
    }

    fun displayNotification(notificationName: String, notificationDescription: String) {
        val notification: NotificationCompat.Builder = createNotification(notificationName, notificationDescription)

        val intent: Intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(0, notification.build())
    }
}