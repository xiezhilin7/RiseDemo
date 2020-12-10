package com.example.risedemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.example.risedemo.workmanager.WorkManagerActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "TileService-MainActivity";

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: main activity");
        findViewById(R.id.work_manager_button).setOnClickListener(this::onClick);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.work_manager_button:
                startActivity(new Intent(this, WorkManagerActivity.class));
                break;
            default:
                break;
        }
    }
}