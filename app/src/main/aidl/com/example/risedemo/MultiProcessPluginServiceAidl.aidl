package com.example.risedemo;

interface MultiProcessPluginServiceAidl {
    String getValueInMultiProcess();
    void transmitValueInMainProcess(String data);
    void notifyMultiProcessCanGetMainProcessValue();
}
