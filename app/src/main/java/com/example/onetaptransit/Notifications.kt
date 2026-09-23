package com.example.onetaptransit

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

private const val CHANNEL_ID = "channel_id"

fun createNotificationChannel(ctx: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "my_channel"
        val descriptionText = "my notification channel description text."
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager = ctx.getSystemService(NotificationManager::class.java) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        Log.d("Notification", "Notification channel created.")
    }
}

fun launchNotification(
    unique_id: Int,
    title: String = "",
    content: String = "",
    expandedContent: String = "",
    ctx: Context
) {

    val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
        .setContentTitle(title)
        .setContentText(content)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setPriority(NotificationCompat.PRIORITY_LOW)

    if (expandedContent.isNotEmpty()) {
        builder
            .setStyle(NotificationCompat.BigTextStyle()
            .bigText(expandedContent))
    }

    with(NotificationManagerCompat.from(ctx)) {
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notify(unique_id, builder.build())
            Log.d("Notification", "Notification launched, ID: $unique_id, Title: $title")
        }
    }
}

fun cancelNotification(unique_id: Int, ctx: Context) {
    val notificationManager: NotificationManager = ctx.getSystemService(NotificationManager::class.java) as NotificationManager
    notificationManager.cancel(unique_id)
}
