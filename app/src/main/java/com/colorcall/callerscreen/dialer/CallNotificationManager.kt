package com.colorcall.callerscreen.dialer

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.telecom.Call
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.colorcall.callerscreen.R
import com.colorcall.callerscreen.call.CallActivity
import com.colorcall.callerscreen.constan.Constant
import com.colorcall.callerscreen.dialer.activity.CallDialerActivity
import com.colorcall.callerscreen.dialer.extensions.powerManager
import com.google.gson.Gson
import com.simplemobiletools.commons.extensions.notificationManager
import com.simplemobiletools.commons.extensions.setText
import com.simplemobiletools.commons.extensions.setVisibleIf
import com.simplemobiletools.commons.helpers.isOreoPlus

class CallNotificationManager(private val context: Context) {
    private val CALL_NOTIFICATION_ID = 42
    private val ACCEPT_CALL_CODE = 0
    private val DECLINE_CALL_CODE = 1
    private val notificationManager = context.notificationManager
    private val callContactAvatarHelper = CallContactAvatarHelper(context)

    @SuppressLint("NewApi")
    fun setupNotification(forceLowPriority: Boolean = false) {
        getCallContact(context.applicationContext, CallManager.getPrimaryCall()) { callContact ->
            val callContactAvatar = callContactAvatarHelper.getCallContactAvatar(callContact)
            val callState = CallManager.getState()
            val isHighPriority = context.powerManager.isInteractive && callState == Call.STATE_RINGING && !forceLowPriority
            val channelId = if (isHighPriority) "simple_dialer_call_high_priority" else "simple_dialer_call"
            if (isOreoPlus()) {
                val importance = if (isHighPriority) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT
                val name = if (isHighPriority) "call_notification_channel_high_priority" else "call_notification_channel"

                NotificationChannel(channelId, name, importance).apply {
                    setSound(null, null)
                    notificationManager.createNotificationChannel(this)
                }
            }

            val openAppIntent = CallDialerActivity.getStartIntent(context)
            val openAppPendingIntent = PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_MUTABLE)

            val acceptCallIntent = Intent(context, CallActionReceiver::class.java)
            acceptCallIntent.action = ACCEPT_CALL
            val acceptPendingIntent =
                PendingIntent.getBroadcast(context, ACCEPT_CALL_CODE, acceptCallIntent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE)

            val declineCallIntent = Intent(context, CallActionReceiver::class.java)
            declineCallIntent.action = DECLINE_CALL
            val declinePendingIntent =
                PendingIntent.getBroadcast(context, DECLINE_CALL_CODE, declineCallIntent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE)

            var callerName = if (callContact.name.isNotEmpty()) callContact.name else context.getString(R.string.unknowContact)
            if (callContact.numberLabel.isNotEmpty()) {
                callerName += " - ${callContact.numberLabel}"
            }

            val contentTextId = when (callState) {
                Call.STATE_RINGING -> R.string.is_calling
                Call.STATE_DIALING -> R.string.dialing
                Call.STATE_DISCONNECTED -> R.string.call_ended
                Call.STATE_DISCONNECTING -> R.string.call_ending
                else -> R.string.ongoing_call
            }

            val collapsedView = RemoteViews(context.packageName, R.layout.call_notification).apply {
                setText(R.id.notification_caller_name, callerName)
                setText(R.id.notification_call_status, context.getString(contentTextId))
                setVisibleIf(R.id.notification_accept_call, callState == Call.STATE_RINGING)

                setOnClickPendingIntent(R.id.notification_decline_call, declinePendingIntent)
                setOnClickPendingIntent(R.id.notification_accept_call, acceptPendingIntent)

                if (callContactAvatar != null) {
                    setImageViewBitmap(R.id.notification_thumbnail, callContactAvatarHelper.getCircularBitmap(callContactAvatar))
                }
            }

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_phone_vector)
                .setContentIntent(openAppPendingIntent)
                .setPriority(if (isHighPriority) NotificationManager.IMPORTANCE_HIGH else NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(Notification.CATEGORY_CALL)
                .setCustomContentView(collapsedView)
                .setOngoing(true)
                .setSound(null)
                .setUsesChronometer(callState == Call.STATE_ACTIVE)
                .setChannelId(channelId)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())

            if (isHighPriority) {
                builder.setFullScreenIntent(openAppPendingIntent, true)
            }

            val notification = builder.build()
            // it's rare but possible for the call state to change by now
            Log.e("TAN", "setupNotification getState: "+CallManager.getState()+"##"+callState )




            if (CallManager.getState() == callState) {
                val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager

              /*  val taskList = activityManager.getRunningTasks(10)
                Log.e("TAN", "topActivity: "+ taskList.size )
                if (taskList.isNotEmpty() &&
                    taskList[0].numActivities == 1 &&
                    taskList[0].topActivity?.className != "CallDialerActivity") {
                    val activityIntent = Intent(context, CallActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Phải có flag này
                    }
                    Log.e("TAN", "setupNotification: "+callContact.number+"##"+callContact.numberLabel )
                    activityIntent.putExtra(Constant.PHONE_NUMBER, callContact.number)
                    activityIntent.putExtra(Constant.CALL_CONTACT, Gson().toJson(callContact))
                    context.startActivity(activityIntent)
                }else{
                    val activityIntent = Intent(context, CallActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Phải có flag này
                    }
                    Log.e("TAN", "setupNotification: "+callContact.number+"##"+callContact.numberLabel )
                    activityIntent.putExtra(Constant.PHONE_NUMBER, callContact.number)
                    activityIntent.putExtra(Constant.CALL_CONTACT, Gson().toJson(callContact))
                    context.startActivity(activityIntent)
                }*/
                val taskList = activityManager.getRunningTasks(10)

                Log.e("TAN", "topActivity: ${taskList.size}")

                val isNotCallDialerActivity = taskList.isNotEmpty() &&
                        taskList[0].numActivities == 1 &&
                        taskList[0].topActivity?.className != "CallDialerActivity"

                val activityIntent = Intent(context, CallActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(Constant.PHONE_NUMBER, callContact.number)
                    putExtra(Constant.CALL_CONTACT, Gson().toJson(callContact))
                }

                Log.e("TAN", "setupNotification: ${callContact.number}##${callContact.numberLabel}")

                if (isNotCallDialerActivity || taskList.isEmpty()) {
                    context.startActivity(activityIntent)
                }

            }

        }
    }

    fun cancelNotification() {
        notificationManager.cancel(CALL_NOTIFICATION_ID)
    }
}
