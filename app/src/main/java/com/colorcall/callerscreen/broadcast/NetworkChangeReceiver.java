package com.colorcall.callerscreen.broadcast;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.colorcall.callerscreen.utils.AppUtils;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private Listener listener;

    public void registerReceiver(Context context, Listener listener) {
        if (context == null) {
            return;
        }
        this.listener = listener;
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(this, filter);
    }

    public void unregisterReceiver(Context context) {
        if (context == null) {
            return;
        }
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Thread t = new Thread(){
            public void run(){
                Message message = new Message();
                String action = intent.getAction();
                message.obj = context;
                if (NetworkChangeReceiver.this.listener != null && (TextUtils.isEmpty(action) ? "" : action).equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                   message.what=1;
                }
                handler.sendMessage(message);
            }
        };
        t.start();
    }
    @SuppressLint("HandlerLeak")
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Context context = (Context) msg.obj;
            if(msg.what==1&&context!=null){
                NetworkChangeReceiver.this.listener.netWorkStateChanged(AppUtils.isNetworkConnected(context));
            }
            super.handleMessage(msg);
        }
    };
    public interface Listener {
        void netWorkStateChanged(boolean isNetWork);
    }
}
