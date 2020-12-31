// IMemoryAidlInterface.aidl
package com.example.risedemo.sharedemo;

interface IMemCallback {

     void onSnapshotCallback(in ParcelFileDescriptor data,in int length);

}
