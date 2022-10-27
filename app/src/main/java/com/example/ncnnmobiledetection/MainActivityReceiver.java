package com.example.ncnnmobiledetection;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.example.ncnnmobiledetection.receive.BatteryBroadcastReceiver;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

public class MainActivityReceiver extends UnityPlayerActivity {

    public BatteryBroadcastReceiver batteryReceiver;
    public static MainActivityReceiver instance = getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initReceiver();
    }

    public static MainActivityReceiver getInstance() {
        if (instance == null) {
            instance = new MainActivityReceiver();
        }
        return instance;
    }

    /**
     * 注册电池电量广播
     ***/
    public void initReceiver() {
        UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                batteryReceiver = new BatteryBroadcastReceiver();
                IntentFilter mFilter = new IntentFilter();
                mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                mFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
                mFilter.addAction(Intent.ACTION_BATTERY_LOW);
                UnityPlayer.currentActivity.registerReceiver(batteryReceiver, mFilter);
            }
        });
    }

    public void unregisterBatteryReceiver() {
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }
    }

}