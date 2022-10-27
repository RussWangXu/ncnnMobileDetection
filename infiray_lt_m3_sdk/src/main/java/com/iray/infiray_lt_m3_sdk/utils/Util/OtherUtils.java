/*
 * Developed by Infrared Products Department.
 * Copyright(C) 2020 IRAY, All Rights Reserved.
 *
 * Filename: OtherUtils.java
 * Packagename: com.example.m300analysis.Utils.OtherUtils
 * Summary:
 *
 * Author: LiXiao
 * Version:
 * Date: 2020年01月02日 09:43:53
 * History:
 *
 * Author: LiXiao
 * Version:
 * Date: 2020年01月02日 09:43:54
 * History:
 */

package com.iray.infiray_lt_m3_sdk.utils.Util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.view.MotionEvent;

import static android.content.Context.WIFI_SERVICE;

public class OtherUtils {
    public static int getMax(int[] array) {
        int max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    public static int getMin(int[] array) {
        int min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    /**
     * 取数组中非0的最小值
     *
     * @param array
     * @return
     */
    public static int getMinExcept0(int[] array)
    {
        int min=0;
        for (int i=0;i<array.length;i++)
        {
            if (array[i]!=0)
            {
                min = array[i];
                break;
            }
        }

        for (int i = 1; i < array.length; i++) {
            if (array[i] < min && array[i]>0) {
                min = array[i];
            }
        }
        return min;
    }
    public static float getMax(float[] array)
    {
        float max=array[0];
        for (int i=1;i<array.length;i++ )
        {
            if (array[i]>max)
            {
                max=array[i];
            }
        }
        return max;
    }

    public static float getMin(float[] array)
    {
        float min=array[0];
        for (int i=1;i<array.length;i++)
        {
            if (array[i]<min)
            {
                min=array[i];
            }
        }
        return min;
    }

    public static byte getMax(byte[] array)
    {
        byte max=array[0];
        for (int i=1;i<array.length;i++ )
        {
            if (array[i]>max)
            {
                max=array[i];
            }
        }
        return max;
    }

    public static byte getMin(byte[] array)
    {
        byte min=(byte)(array[0]&0xFF);
        for (int i=1;i<array.length;i++)
        {
            if ((array[i]&0xFF)<min)
            {
                min=(byte)(array[i]&0xFF);
            }
        }
        return min;
    }

    /**
     * 计算两个点的距离
     *
     * @param event
     * @return
     */
    public static double spacing(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return Math.sqrt(x * x + y * y);
        } else {
            return 0;
        }
    }

    public static String getSSID(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
        if (wm != null) {
            WifiInfo winfo = wm.getConnectionInfo();
            if (winfo != null) {
                String s = winfo.getSSID();
                if (s.length() > 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
                    return s.substring(1, s.length() - 1);
                }
            }
        }
        return "";
    }
}
