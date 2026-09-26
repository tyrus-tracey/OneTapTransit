package com.example.onetaptransit.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.onetaptransit.MainActivity
import com.example.onetaptransit.R

private const val CHANNEL_ID = "channel_id"

fun createNotificationChannel(ctx: Context) {
    val name = "Data Sync Notifications"
    val descriptionText = "Notifications upon synchronization of GTFS Static and Realtime data from Translink servers."
    val importance = NotificationManager.IMPORTANCE_LOW
    val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
        description = descriptionText
        setSound(null, null)
    }
    val notificationManager: NotificationManager = ctx.getSystemService(NotificationManager::class.java) as NotificationManager
    notificationManager.createNotificationChannel(channel)
    Log.d("Notification", "Notification channel created.")
}

fun launchNotification(
    unique_id: Int,
    title: String = "",
    content: String = "",
    expandedContent: String = "",
    ctx: Context
) {
    //TODO: will need to refactor Intent when later using Compose navigation
    val launchMainActivityIntent = Intent(ctx, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    val pendingIntent = PendingIntent.getActivity(ctx, 0, launchMainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
        .setContentTitle(title)
        .setContentText(content)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setContentIntent(pendingIntent)

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

//fun launchGTFSStaticImportNotification(
//    unique_id: Int,
//    progRoutes: Int,
//    progTrips: Int,
//    progCalendar: Int,
//    progCalendarDates: Int,
//    progStops: Int,
//    progStopTimes: Int,
//    ctx: Context
//) {
//    val title = "From Kotlin: Importing GTFS Static data..."
//    val content = "content text"
//    val progMax = 100
//    val xml_layout = RemoteViews(ctx.packageName, R.layout.notification_gtfs_static_import).apply {
//        setProgressBar(R.id.routes_import_progress, progMax, progRoutes, false)
//        setProgressBar(R.id.trips_import_progress, progMax, progTrips, false)
//        setProgressBar(R.id.calendar_import_progress, progMax, progCalendar, false)
//        setProgressBar(R.id.calendar_dates_import_progress, progMax, progCalendarDates, false)
//        setProgressBar(R.id.stops_import_progress, progMax, progStops, false)
//        setProgressBar(R.id.stop_times_import_progress, progMax, progStopTimes, false)
//    }
//
//    val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
//        .setContentTitle(title)
//        .setContentText(content)
//        .setSmallIcon(R.drawable.ic_launcher_foreground)
//        .setStyle(NotificationCompat.DecoratedCustomViewStyle())
//        .setCustomBigContentView(xml_layout)
//        .setOngoing(true)
//        .setOnlyAlertOnce(true)
//
//    with(NotificationManagerCompat.from(ctx)) {
//        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
//            notify(unique_id, builder.build())
//            Log.d("Notification", "Notification launched, ID: $unique_id, Title: $title")
//        }
//    }
//}
//
fun cancelNotification(unique_id: Int, ctx: Context) {
    val notificationManager: NotificationManager = ctx.getSystemService(NotificationManager::class.java) as NotificationManager
    notificationManager.cancel(unique_id)
}
