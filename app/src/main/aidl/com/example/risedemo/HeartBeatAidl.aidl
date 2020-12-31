package com.example.risedemo;

import com.example.risedemo.IReceiverAidlInterface;

interface HeartBeatAidl {
    String getValueInMainProcess();
    void notifySuccess(String data);
    void registerCallback(IReceiverAidlInterface cb);
}
