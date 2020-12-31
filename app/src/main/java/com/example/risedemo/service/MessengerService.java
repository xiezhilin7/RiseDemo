package com.example.risedemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import static com.example.risedemo.MainActivity.FROM_SERVER;

/**
 * 通过Messenger也可以完成进程间通信
 */
public class MessengerService extends Service {
    private static final String TAG = "Rise-MessengerService";
    private final Messenger messenger = new Messenger(MessengerHandler);
    public static final int MSG_FROM_CLIENT = 112;

    private static Handler MessengerHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FROM_CLIENT:
                    Log.e(TAG, msg.getData().getString("msg"));
                    Messenger clientMessenger = msg.replyTo;
                    Message replyMessage = Message.obtain(this, FROM_SERVER);
                    Bundle data = new Bundle();
                    data.putString("reply", "I am from the server");
                    replyMessage.setData(data);
                    try {
                        clientMessenger.send(replyMessage);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }
}
