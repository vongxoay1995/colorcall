package com.colorcall.callerscreen.dialer.service

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.colorcall.callerscreen.broadcast.CallReceiver
import com.colorcall.callerscreen.dialer.CallManager
import com.colorcall.callerscreen.dialer.CallNotificationManager
import com.colorcall.callerscreen.dialer.NoCall
import com.colorcall.callerscreen.dialer.activity.CallDialerActivity
import com.colorcall.callerscreen.dialer.extensions.config
import com.colorcall.callerscreen.dialer.extensions.isOutgoing
import com.colorcall.callerscreen.dialer.extensions.powerManager
import com.colorcall.callerscreen.utils.FlashUtils

class CallDialerService : InCallService() {
    private val callNotificationManager by lazy { CallNotificationManager(this) }

    // Lazy init to avoid using 'this' before Service context is attached
    private val flashUtils: FlashUtils by lazy {
        FlashUtils.getInstance(true, this)
    }

    private val callListener = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
             if (state == Call.STATE_DISCONNECTED || state == Call.STATE_DISCONNECTING) {

                 val localBroadcastManager = LocalBroadcastManager
                     .getInstance(this@CallDialerService)
                 localBroadcastManager.sendBroadcast(Intent("com.colorcall.endCall"))
                 callNotificationManager.cancelNotification()
                 if (flashUtils.isRunning) {
                     flashUtils.stop()
                 }
             } else {
                 callNotificationManager.setupNotification()
             }
        }
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
          CallManager.onCallAdded(call)
          CallManager.inCallService = this
          call.registerCallback(callListener)
          val isScreenLocked = (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isDeviceLocked
          if (!powerManager.isInteractive || call.isOutgoing() || isScreenLocked || config.alwaysShowFullscreen) {
              try {
                  callNotificationManager.setupNotification(true)
                  startActivity(CallDialerActivity.getStartIntent(this))
              } catch (e: Exception) {
                  // seems like startActivity can throw AndroidRuntimeException and ActivityNotFoundException, not yet sure when and why, lets show a notification
                  callNotificationManager.setupNotification()
              }
          } else {
              callNotificationManager.setupNotification()
          }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
         call.unregisterCallback(callListener)
         val wasPrimaryCall = call == CallManager.getPrimaryCall()
         CallManager.onCallRemoved(call)
         if (CallManager.getPhoneState() == NoCall) {
             CallManager.inCallService = null
             callNotificationManager.cancelNotification()
         } else {
             callNotificationManager.setupNotification()
             if (wasPrimaryCall) {
                 startActivity(CallDialerActivity.getStartIntent(this))
             }
         }
    }

    override fun onCallAudioStateChanged(audioState: CallAudioState?) {
        super.onCallAudioStateChanged(audioState)
         if (audioState != null) {
             CallManager.onAudioStateChanged(audioState)
         }
    }

    override fun onDestroy() {
        super.onDestroy()
        callNotificationManager.cancelNotification()
        // Safety: ensure flash is stopped when service dies
        if (flashUtils.isRunning) {
            flashUtils.stop()
        }
    }
}

