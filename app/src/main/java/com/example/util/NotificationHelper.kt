package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    private const val CHANNEL_ID = "pinterest_dl_channel"
    private const val CHANNEL_NAME = "PinterestDL Downloads"

    fun init(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (manager != null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Download progress and completion notifications"
                    enableVibration(true)
                    setShowBadge(true)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun showDownloadProgress(context: Context, id: Int, fileName: String, progress: Int, max: Int) {
        try {
            init(context)
            val manager = NotificationManagerCompat.from(context)

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("Downloading...")
                .setContentText(fileName)
                .setProgress(max, progress, max == 0)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)

            manager.notify(id, builder.build())
        } catch (_: SecurityException) {
            // Handled when notification permission is denied
        }
    }

    fun showDownloadComplete(context: Context, id: Int, fileName: String, fileUri: String) {
        try {
            init(context)
            val manager = NotificationManagerCompat.from(context)

            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(fileUri), "image/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                id,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("Download Complete")
                .setContentText("Saved as $fileName in Pictures/PinterestDL")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            manager.cancel(id)
            manager.notify(id + 1000, builder.build())
        } catch (_: SecurityException) {
            // Handled when notification permission is denied
        }
    }

    fun showDownloadFailed(context: Context, id: Int, fileName: String, errorMsg: String) {
        try {
            init(context)
            val manager = NotificationManagerCompat.from(context)

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle("Download Failed")
                .setContentText("$fileName: $errorMsg")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            manager.cancel(id)
            manager.notify(id + 2000, builder.build())
        } catch (_: SecurityException) {
            // Handled
        }
    }
}
