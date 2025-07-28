package com.colorcall.callerscreen.service;

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.colorcall.callerscreen.R
import com.colorcall.callerscreen.main.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e("TAN", "onMessageReceived: $remoteMessage", )
        // Xử lý thông báo khi ứng dụng đang chạy
        remoteMessage.notification?.let {
            val title = it.title
            val body = it.body
            if (title != null && body != null) {
                showNotification(title, body)
            }
        }
    }

    override fun onNewToken(token: String) {
    }

    private fun showNotification(title: String, body: String) {
        // Tạo notification channel (cho Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "color_call_id",
                "Color Call Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Color Call Notification"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Tạo Intent để mở MainActivity
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Tạo PendingIntent
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Tạo và hiển thị thông báo
        val builder = NotificationCompat.Builder(this, "color_call_id")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // Gắn PendingIntent
            .setAutoCancel(true) // Tự động hủy thông báo khi nhấn

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this).notify(1, builder.build())
    }

}