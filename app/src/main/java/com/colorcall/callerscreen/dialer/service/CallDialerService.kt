package com.colorcall.callerscreen.dialer.service

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.colorcall.callerscreen.dialer.CallManager
import com.colorcall.callerscreen.dialer.CallNotificationManager
import com.colorcall.callerscreen.dialer.NoCall
import com.colorcall.callerscreen.dialer.activity.CallDialerActivity
import com.colorcall.callerscreen.dialer.extensions.config
import com.colorcall.callerscreen.dialer.extensions.isOutgoing
import com.colorcall.callerscreen.dialer.extensions.powerManager

class CallDialerService : InCallService() {
    private val callNotificationManager by lazy { CallNotificationManager(this) }

    private val callListener = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            Log.e("TAN", "onStateChanged: "+CallManager.getState() )
             if (state == Call.STATE_DISCONNECTED || state == Call.STATE_DISCONNECTING) {

                 val localBroadcastManager = LocalBroadcastManager
                     .getInstance(this@CallDialerService)
                 localBroadcastManager.sendBroadcast(Intent("com.colorcall.endCall"))

                // callNotificationManager.cancelNotification()
             } else {
                 Log.e("TAN", "onStateChanged: TAN 1" )
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
                  Log.e("TAN", "onStateChanged: TAN 2")
                  callNotificationManager.setupNotification(true)
                  startActivity(CallDialerActivity.getStartIntent(this))
              } catch (e: Exception) {
                  Log.e("TAN", "onStateChanged: TAN 3")

                  // seems like startActivity can throw AndroidRuntimeException and ActivityNotFoundException, not yet sure when and why, lets show a notification
                  callNotificationManager.setupNotification()
              }
          } else {
             // val fullScreenIntent = Intent(this, CallActivity::class.java)

              Log.e("TAN", "onStateChanged: TAN 4")

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
             Log.e("TAN", "onStateChanged: TAN 5")

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
    }
}
