package com.iray.infiray_lt_m3_sdk.utils;

import android.graphics.Bitmap;
import android.graphics.RectF;

import com.serenegiant.usb.UVCCameraM3;

import java.util.ArrayList;
import java.util.Collections;

public class DataUtils {

    public static int DEFAULT_PREVIEW_WIDTH;
    public static int DEFAULT_PREVIEW_HEIGHT;

    /**
     * 获取机芯面阵大小及类型（成像型/测温型）
     * @param camera
     * @return int[]类型 index=0-机芯面阵宽度，index=1-机芯面阵高度，index=2-机芯类型（数值0-成像型，1-测温型）
     */
    public static int[] getDeviceType(UVCCameraM3 camera){
        String cameraSize = camera.getSupportedSize();
        int[] deviceType = new int[3];
        String width = cameraSize.substring(53, 56);
        switch (width) {
            case "128":
                deviceType[0] = 1280;
                deviceType[1] = 512; //640*512T
                if (cameraSize.substring(58, 61).equals("720")) {
                    deviceType[1] = 720;  //1280*720
                }
                break;
            case "512":
                deviceType[0] = 512;
                deviceType[1] = 192; //256*192T
                break;
            case "256":
                deviceType[0] = 256;
                deviceType[1] = 192; //256*192
                if (cameraSize.substring(58, 61).equals("720")) {
                    deviceType[0] = 2560;
                    deviceType[1] = 720;   //1280*720T
                }
                break;
            default:
                String height = cameraSize.substring(57, 60);
                deviceType[0] = Integer.parseInt(width);
                deviceType[1] = Integer.parseInt(height);
                break;
        }
        if (deviceType[0] == 768 || (deviceType[0] == 1280 && deviceType[1] == 512) || deviceType[0] == 512 || deviceType[0] == 2560) {
            deviceType[0] = deviceType[0] / 2;
//            bTemp = true;
            deviceType[2] = 1;
//            updateItems();
        } else {
            deviceType[2] = 0;
        }
        return deviceType;
    }

    /**
     * 获取整帧最高、最低、平均温度
     * @return
     */
    public static synchronized float[] getTempResult() {
        float sum = 0;
        float[] result = new float[3];
        ArrayList<Float> tempList = new ArrayList<Float>();

        int iCount = 0;
        for (int j = 0; j < CameraUtils.DEFAULT_PREVIEW_HEIGHT; j++) {
            for (int i = 0; i < CameraUtils.DEFAULT_PREVIEW_WIDTH; i++) {
                iCount++;
                int index = (int) (i + j * CameraUtils.DEFAULT_PREVIEW_WIDTH);
                tempList.add(CameraUtils.tempInfo[index]);
                sum += CameraUtils.tempInfo[index];
            }
        }

        result[0] = Collections.max(tempList);
        result[1] = Collections.min(tempList);
        result[2] = sum / iCount;
        return result;
    }


    /**
     * 获取CDS-2视频流
     *
     * @param bytes
     * @return
     */
    public static byte[] GetImageFrame(byte[] bytes) {
        byte[] yuyvByte = new byte[DEFAULT_PREVIEW_WIDTH * DEFAULT_PREVIEW_HEIGHT * 2];
        int desIndex = 0;
        int srcIndex = 0;
        for (int i = 0; i < DEFAULT_PREVIEW_HEIGHT; i++) {
            System.arraycopy(bytes, srcIndex, yuyvByte, desIndex, DEFAULT_PREVIEW_WIDTH * 2);
            desIndex += DEFAULT_PREVIEW_WIDTH * 2;
            srcIndex += DEFAULT_PREVIEW_WIDTH * 4;
        }
        return yuyvByte;
    }

    /**
     * 获取CDS-2温度流
     *
     * @param bytes
     * @return
     */
    public static byte[] GetTemp(byte[] bytes) {
        byte[] tempByte = new byte[DEFAULT_PREVIEW_WIDTH * DEFAULT_PREVIEW_HEIGHT * 2];
        int desIndex = 0;
        int srcIndex = DEFAULT_PREVIEW_WIDTH * 2;
        for (int i = 0; i < DEFAULT_PREVIEW_HEIGHT; i++) {
            System.arraycopy(bytes, srcIndex, tempByte, desIndex, DEFAULT_PREVIEW_WIDTH * 2);
            desIndex += DEFAULT_PREVIEW_WIDTH * 2;
            srcIndex += DEFAULT_PREVIEW_WIDTH * 4;
        }
        return tempByte;
    }


    /*
   UYVY to int Array
    */
    public static int[] Uyvy2RgbInt(byte[] pYuvPackedByte, int lPicHeight, int lPicWidth) {

        int picSize = lPicWidth * lPicHeight;

        int[] pRrgaInt = new int[picSize];

        byte[] pYPlaneByte = new byte[lPicHeight * lPicWidth];
        byte[] pUPlaneByte = new byte[(lPicHeight) * (lPicWidth / 2)];
        byte[] pVPlaneByte = new byte[(lPicHeight) * (lPicWidth / 2)];

        for (int idxSrc = 0, idxDest = 0, idxY = 0; idxDest < lPicHeight * lPicWidth / 2; idxDest++) {
            pUPlaneByte[idxDest] = pYuvPackedByte[idxSrc + 0];
            pYPlaneByte[idxY] = pYuvPackedByte[idxSrc + 1];

            pVPlaneByte[idxDest] = pYuvPackedByte[idxSrc + 2];
            pYPlaneByte[idxY + 1] = pYuvPackedByte[idxSrc + 3];
            idxSrc = idxSrc + 4;
            idxY = idxY + 2;
        }

        pRrgaInt = Yuv2RgbInt(pYPlaneByte, pUPlaneByte, pVPlaneByte, lPicHeight, lPicWidth);

        pYPlaneByte = null;
        pUPlaneByte = null;
        pVPlaneByte = null;

        return pRrgaInt;
    }


    /*
  UYVY to int Array
   */
    public static int[] Yuyv2RgbInt(byte[] pYuvPackedByte, int lPicHeight, int lPicWidth) {

        int picSize = lPicWidth * lPicHeight;

        int[] pRrgaInt = new int[picSize];

        byte[] pYPlaneByte = new byte[lPicHeight * lPicWidth];
        byte[] pUPlaneByte = new byte[(lPicHeight) * (lPicWidth / 2)];
        byte[] pVPlaneByte = new byte[(lPicHeight) * (lPicWidth / 2)];

        for (int idxSrc = 0, idxDest = 0, idxY = 0; idxDest < lPicHeight * lPicWidth / 2; idxDest++) {
            pYPlaneByte[idxY] = pYuvPackedByte[idxSrc + 0];
            pUPlaneByte[idxDest] = pYuvPackedByte[idxSrc + 1];

            pYPlaneByte[idxY + 1] = pYuvPackedByte[idxSrc + 2];
            pVPlaneByte[idxDest] = pYuvPackedByte[idxSrc + 3];
            idxSrc = idxSrc + 4;
            idxY = idxY + 2;
        }

        pRrgaInt = Yuv2RgbInt(pYPlaneByte, pUPlaneByte, pVPlaneByte, lPicHeight, lPicWidth);

        pYPlaneByte = null;
        pUPlaneByte = null;
        pVPlaneByte = null;

        return pRrgaInt;
    }


    /*
   YUV to RGB int Pixel Array
    */
    public static int[] Yuv2RgbInt(byte[] pYPlaneByte, byte[] pUPlaneByte, byte[] pVPlaneByte, int lPicHeight, int lPicWidth) {
        int R, G, B;
        int Y, U, V;
        int picSize = lPicWidth * lPicHeight;
        int[] pRrgaInt = new int[picSize];

        for (int iRow = 0; iRow < lPicHeight; iRow++) {
            for (int jCol = 0; jCol < lPicWidth; jCol++) {

                //byte to int
                Y = pYPlaneByte[iRow * lPicWidth + jCol] & 0xff;
                U = pUPlaneByte[(iRow * lPicWidth + jCol) / 2] & 0xff;
                V = pVPlaneByte[(iRow * lPicWidth + jCol) / 2] & 0xff;
                //int YUV to RGB
                Y = Y < 16 ? 16 : Y;
                R = Math.round(1.164f * (Y - 16) + 1.596f * (V - 128));
                G = Math.round(1.164f * (Y - 16) - 0.813f * (V - 128) - 0.391f * (U - 128));
                B = Math.round(1.164f * (Y - 16) + 2.018f * (U - 128));

                R = Math.max(0, Math.min(255, R));
                G = Math.max(0, Math.min(255, G));
                B = Math.max(0, Math.min(255, B));

                pRrgaInt[iRow * lPicWidth + jCol] = (R << 16) | (G << 8) | B | 0xFF000000;
            }
        }
        return pRrgaInt;
    }


    /*
    RGB to Bitmap
     */
    static public Bitmap Rgb2Bitmap(int[] colors, int width, int height) {
        if (colors == null) {
            return null;
        }
        Bitmap bmp = Bitmap.createBitmap(colors, 0, width, width, height,
                Bitmap.Config.ARGB_8888);
        return bmp;
    }

    //byteArray To HexString
    public static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }
}
