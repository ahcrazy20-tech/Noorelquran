package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.R

object PrayerNotificationHelper {

    private const val CHANNEL_PRAYER = "noor_prayer_alerts"
    private const val CHANNEL_ADHKAR = "noor_adhkar_alerts"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val prayerChannel = NotificationChannel(
                CHANNEL_PRAYER,
                "مواقيت الصلاة والأذان",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات دخول وقت الصلاة والإقامة"
                enableVibration(true)
            }
            val adhkarChannel = NotificationChannel(
                CHANNEL_ADHKAR,
                "منبه الأذكار والورد القرآني",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "تذكير بأذكار الصباح والمساء وورد الختمة اليومي"
            }
            manager.createNotificationChannel(prayerChannel)
            manager.createNotificationChannel(adhkarChannel)
        }
    }

    fun sendNotification(
        context: Context,
        title: String,
        body: String,
        isPrayerAlert: Boolean = true
    ): Boolean {
        ensureChannels(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }
        val channelId = if (isPrayerAlert) CHANNEL_PRAYER else CHANNEL_ADHKAR
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(
                if (isPrayerAlert) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify((System.currentTimeMillis() % 100000).toInt(), notification)
        return true
    }
}
