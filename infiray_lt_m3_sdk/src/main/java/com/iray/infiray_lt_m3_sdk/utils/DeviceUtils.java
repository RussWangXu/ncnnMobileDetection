package com.iray.infiray_lt_m3_sdk.utils;

public class DeviceUtils {
    public static String DEVICE_PN = "0";
    public static String BASE64_DEVICE_PN = "0";
    public static String DEVICE_SN = "0";
    public static String BASE64_DEVICE_SN = "0";
    public static String DETECTOR_ID = "0";
    public static String BASE64_DETECTOR_ID = "0";
    public static String SOFT_VERSION = "0";
    public static int WIDTH = 0;
    public static int HEIGHT = 0;

    public static float fpaValue = 0;
    public static float brightValue = 0;
    public static float contrastValue = 0;


    //执行命令类型，0=无，
    //20001=fpa温度， 20002=软件版本， 20003=读取sn，
    //20004=读取pn，  20005=探测器id， 20006=亮度对比度(获取),  20007=亮度对比度（设置）

    //10001=打快门，  10002=背景校正， 10003=自动快门，
    //10004=图像放大，10005=teclessB，10006=保存设置

    //1001=上下镜像， 1002=左右翻转，  101=伪彩设置



    private DeviceUtils() { }
}
