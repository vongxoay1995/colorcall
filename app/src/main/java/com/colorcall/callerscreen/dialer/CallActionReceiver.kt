package com.colorcall.callerscreen.dialer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.colorcall.callerscreen.dialer.activity.CallDialerActivity

class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACCEPT_CALL -> {
                context.startActivity(CallDialerActivity.getStartIntent(context))
                CallManager.accept()
            }
            DECLINE_CALL -> CallManager.reject()
        }
    }
}