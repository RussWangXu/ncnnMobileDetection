package com.example.ncnnmobiledetection.receive;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.unity3d.player.UnityPlayer;

import java.util.List;

import static android.content.Context.WIFI_SERVICE;

public class BatteryBroadcastReceiver extends BroadcastReceiver {

    @SuppressLint("MissingPermission")
    public int getMobileDbm(Context context) {
        int dbm = -1;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        List<CellInfo> cellInfoList;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

            cellInfoList = tm.getAllCellInfo();
            if (null != cellInfoList) {
                for (CellInfo cellInfo : cellInfoList) {
                    if (cellInfo instanceof CellInfoGsm) {
                        CellSignalStrengthGsm cellSignalStrengthGsm = ((CellInfoGsm) cellInfo).getCellSignalStrength();
                        dbm = cellSignalStrengthGsm.getDbm();
                    } else if (cellInfo instanceof CellInfoCdma) {
                        CellSignalStrengthCdma cellSignalStrengthCdma =
                                ((CellInfoCdma) cellInfo).getCellSignalStrength();
                        dbm = cellSignalStrengthCdma.getDbm();
                    } else if (cellInfo instanceof CellInfoWcdma) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            CellSignalStrengthWcdma cellSignalStrengthWcdma =
                                    ((CellInfoWcdma) cellInfo).getCellSignalStrength();
                            dbm = cellSignalStrengthWcdma.getDbm();
                        }
                    } else if (cellInfo instanceof CellInfoLte) {
                        CellSignalStrengthLte cellSignalStrengthLte = ((CellInfoLte) cellInfo).getCellSignalStrength();
                        dbm = cellSignalStrengthLte.getDbm();
                    }
                }
            }
        }
        return dbm;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        int networkType = 0;
        NetworkInfo netInfo;
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            //没八秒实行一次
            Log.i("level", "level  " + intent.getIntExtra("level", 0));

            //电池电量
            int level = intent.getIntExtra("level", 0);
            UnityPlayer.UnitySendMessage("MainPanel","SetBateryLength",""+level);
            //移动信号强度
            int getDBM = getMobileDbm(context);
            UnityPlayer.UnitySendMessage("MainPanel","SetSignalLength",""+getDBM);

            int wifiState =((WifiManager) context.getSystemService(WIFI_SERVICE)).getConnectionInfo().getRssi();
            UnityPlayer.UnitySendMessage("MainPanel","SetSignalLength",""+wifiState);



            //电池状态
            int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                networkType = 9;

            } else if (100 >= level && level > 80) {
                networkType = 4;
            } else if (80 >= level && level > 50) {
                networkType = 5;
            } else if (50 >= level && level > 30) {
                networkType = 6;
            } else if (30 >= level && level > 10) {
                networkType = 7;
            } else if (10 >= level && level >= 0) {
                networkType = 8;
            }
            Log.i("setNetworkTtpe", "setNetworkTtpe  " + networkType);

        }
    }
}
