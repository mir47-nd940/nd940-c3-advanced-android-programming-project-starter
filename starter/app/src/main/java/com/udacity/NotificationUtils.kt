package com.udacity

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

private val NOTIFICATION_ID = 0
private val REQUEST_CODE = 0

const val EXTRA_DOWNLOAD_FILE_NAME = "download_file_name"
const val EXTRA_DOWNLOAD_STATUS = "download_status"

fun NotificationManager.sendNotification(
    messageBody: String,
    downloadFileName: String,
    downloadStatus: String,
    applicationContext: Context,
) {
    val intent = Intent(applicationContext, DetailActivity::class.java)
    intent.putExtra(EXTRA_DOWNLOAD_FILE_NAME, downloadFileName)
    intent.putExtra(EXTRA_DOWNLOAD_STATUS, downloadStatus)
    val pendingIntent: PendingIntent = PendingIntent.getActivity(
        applicationContext,
        REQUEST_CODE,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.download_notification_channel_id)
    )
        .setSmallIcon(R.drawable.ic_assistant_black_24dp)
        .setContentTitle(
            applicationContext
                .getString(R.string.notification_title)
        )
        .setContentText(messageBody)
        .setAutoCancel(true)
        .addAction(
            R.drawable.ic_assistant_black_24dp,
            applicationContext.getString(R.string.notification_action_title),
            pendingIntent
        )
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    notify(NOTIFICATION_ID, builder.build())
}

fun NotificationManager.cancelNotifications() {
    cancelAll()
}