package com.iray.infiray_lt_m3_sdk.utils.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.RectF;


import com.iray.infiray_lt_m3_sdk.utils.IrgData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetImageUtils {
    //    IrgData irgData = new IrgData();
//    private float[] tempInfo = irgData.getTempInfo();
    private float[] tempInfo = IrgData.tempInfo;

    public static short convertByte2Short(byte[] buf, boolean bBigEnding) {
        if (buf == null) {
            throw new IllegalArgumentException("byte array is null!");
        }
        short r = 0;
        if (bBigEnding) {
            for (int i = 0; i < 2; i++) {
                r <<= 8;
                r |= (buf[i] & 0x00ff);
            }
        } else {
            for (int i = 1; i >= 0; i--) {
                r <<= 8;
                r |= (buf[i] & 0x00ff);
            }
        }

        return r;
    }

    /**
     * ATF温度字节转short &0x3FFF
     * @param buf
     * @return
     */
    public static short covertByte2ShotByATF(byte[] buf){
        if (buf == null) {
            throw new IllegalArgumentException("byte array is null!");
        }
        short r = 0;
        r |= buf[1] & 0x3f;
        r <<= 8;
        r |= buf[0] & 0xff;
        return r;
    }

    public static int convertByte2Int(byte[] buf) {
        int r = 0;
        for (int i = buf.length - 1; i >= 0; i--) {
            r <<= 8;
            r |= (buf[i] & 0x000000ff);
        }
        return r;
    }

    public static byte[] converInt2Byte(int val) {
        byte[] result = new byte[4];
        result[0] = (byte) (val & 0xff);
        result[1] = (byte) ((val >> 8) & 0xff);
        result[2] = (byte) ((val >> 16) & 0xff);
        result[3] = (byte) ((val >> 24) & 0xff);
        return result;
    }

    public static byte[] convertShort2Byte(short val) {
        byte[] result = new byte[2];
        result[0] = (byte) (val & 0xff);
        result[1] = (byte) ((val >> 8) & 0xff);
        return result;
    }



    /**
     * Bitmap转化为ARGB数据，再转化为NV21数据
     *
     * @param src    传入ARGB_8888的Bitmap
     * @param width  NV21图像的宽度
     * @param height NV21图像的高度
     * @return nv21数据
     */
    public static byte[] bitmapToNv21(Bitmap src, int width, int height) {
        if (src != null && src.getWidth() >= width && src.getHeight() >= height) {
            int[] argb = new int[width * height];
            src.getPixels(argb, 0, width, 0, 0, width, height);
            return argbToNv21(argb, width, height);
        } else {
            return null;
        }
    }

    /**
     * ARGB数据转化为NV21数据
     *
     * @param argb   argb数据
     * @param width  宽度
     * @param height 高度
     * @return nv21数据
     */
//    private static byte[] argbToNv21(int[] argb, int width, int height) {
//        int frameSize = width * height;
//        int yIndex = 0;
//        int uvIndex = frameSize;
//        int index = 0;
//        byte[] nv21 = new byte[width * height * 3 / 2];
//        for (int j = 0; j < height; ++j) {
//            for (int i = 0; i < width; ++i) {
//                int R = (argb[index] & 0xFF0000) >> 16;
//                int G = (argb[index] & 0x00FF00) >> 8;
//                int B = argb[index] & 0x0000FF;
//                int Y = (77 * R + 150 * G + 29 * B) >> 8;
//                int U = ((-44 * R - 87 * G + 131 * B) >> 8) + 128;
//                int V = ((131 * R - 110 * G - 21 * B) >> 8) + 128;
//                nv21[yIndex++] = (byte) (Y < 0 ? 0 : (Y > 255 ? 255 : Y));
//                if (j % 2 == 0 && index % 2 == 0 && uvIndex < nv21.length - 2) {
//                    nv21[uvIndex++] = (byte) (V < 0 ? 0 : (V > 255 ? 255 : V));
//                    nv21[uvIndex++] = (byte) (U < 0 ? 0 : (U > 255 ? 255 : U));
//                }
//
//                ++index;
//            }
//        }
//        return nv21;
//    }
    private static byte[] argbToNv21(int[] argb, int width, int height) {
//    public byte[] getRecordFromArgb(int[] argb, int width, int height) {
        byte[] record = new byte[width * height * 3 / 2];
        int Yindex = 0;
        int UVindex = width * height;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int B = argb[i * width + j] & 0xff;
                int G = (argb[i * width + j] & 0xffff) >> 8;
                int R = (argb[i * width + j] & 0xffffff) >> 16;

                int Y = (77 * R + 150 * G + 29 * B) >> 8;
                record[Yindex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));

                if (i % 2 == 0) {
                    if (j % 2 == 0) {
                        int V = ((131 * R - 110 * G - 21 * B) >> 8) + 128;
                        record[UVindex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    } else {
                        int U = ((-44 * R - 87 * G + 131 * B) >> 8) + 128;
                        record[UVindex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                    }
                }
            }
        }
        return record;
    }

    public static byte[] converShort2Byte(short val) {
        byte[] result = new byte[2];
        result[0] = (byte) (val & 0xff);
        result[1] = (byte) ((val >> 8) & 0xff);
        return result;
    }

    public static boolean isIP(String addr) {
        if (addr.length() < 7 || addr.length() > 15 || "".equals(addr)) {
            return false;
        }
        String rexp = "^([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$";
        Pattern pattern = Pattern.compile(rexp);
        Matcher matcher = pattern.matcher(addr);
        return matcher.find();
    }

    public static boolean isPort(String port) {
        if (port.length() > 5 || "".equals(port)) {
            return false;
        }
        String rexp = "^(\\d|[1-9]\\d{1,3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])$";
        Pattern pattern = Pattern.compile(rexp);
        Matcher matcher = pattern.matcher(port);
        return matcher.find();
    }
}
