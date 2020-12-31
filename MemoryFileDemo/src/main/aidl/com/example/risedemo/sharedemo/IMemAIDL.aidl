// IMemoryAidlInterface.aidl
package com.example.risedemo.sharedemo;

import com.example.risedemo.sharedemo.IMsgCallback;
import com.example.risedemo.sharedemo.IMemCallback;

interface IMemAIDL {

     void sendMessage(String json);

     void onMessageCallback(IMsgCallback callback);

     void takeSnapshot(IMemCallback callback);
}
