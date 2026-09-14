package com.prasad.mathgrapher.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.prasad.mathgrapher.MainActivity
import com.prasad.mathgrapher.R

private const val CHANNEL_ID = "daily_nudge_channel"
private const val NOTIFICATION_ID = 1001

private val NUDGE_MESSAGES = listOf(
    "A curve is waiting to be graphed \uD83D\uDCC8",
    "Try a new equation today?",
    "Your graph paper misses you.",
    "Quick math break?",
    "Got a tricky equation? Come test it."
)

fun createNudgeChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Daily reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}

fun showNudgeNotification(context: Context) {
    if (Build.VERSION.SDK_INT >= 33 &&
        ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
        return // permission not granted — skip silently, don't crash
    }

    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent,
        PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher_round) // Fallback to launcher icon for now
        .setContentTitle("Math Grapher")
        .setContentText(NUDGE_MESSAGES.random())
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
}
