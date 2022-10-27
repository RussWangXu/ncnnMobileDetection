package com.iray.infiray_lt_m3_sdk.utils;

import android.os.Environment;
import android.util.Log;

import com.iray.infiray_lt_m3_sdk.utils.Util.GetImageUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class IrgData {
    private static final String SCREENSHOT_FILE_NAME_TEMPLATE = "%s_%s_%s.irg";
    private static final String SCREENSHOT_FILE_PATH_TEMPLATE = "%s/%s/%s";

    private byte start[] = new byte[]{(byte) 0xca, (byte) 0xac}; //头开始
    public final short length = 128; //头开始到头结束总长度
    //FIXME 如果用private，在GetImageUtils中创建实例IrgData实例后。tempInfo就会重新初始化。？？
    public static final short headerLength =128; //头开始到头结束总长度
    public static float[] tempInfo = new float[640 * 512];
//    private static byte[] oriTempData = new byte[640 * 512 * 2];
//    private static float[] tempKelvin = new float[640 * 512];
    private static byte[] oriTemp;


    public static short irWidth = 0; //红外面阵宽度
    public static short irHeight = 0; //红外面阵高度

    public final byte irUnitSize = 1; //红外每个像素点所占字节数，0：1byte，1：2byte，默认为1

    private static short tempWidth = 0; //测温数据面阵宽度
    private static short tempHeight = 0; //测温数据面阵高度

    public final byte tempUnitSize = 1; //测温每个像素点所占字节数，0：1byte，1：2byte，默认为1

    private static short visibleWiidth = 0; //可见光数据面阵宽度
    private static short visibleHeight = 0; //可见光数据面阵高度

    /**
     * 环境参数6个，这里设定为真实值，*10000为记录到irg文件中的值
     */
    public static float emissity = 1.0f; //发射率
    public static float temperature = 25; //大气温度
    public static float refectTemp = 25; //反射温度
    public static float atmosphericPermeability = 1; //大气透过率
    public static float distance = 2; //距离
    public static float humidity = 0.4f; //湿度

    /**
     * 距离修正系数4个，这里设定为真实值，*1000为记录到irg文件中的值
     */
    private static float K0 = 0;
    private static float B0 = 1;
    private static float K1 = 0;
    private static float B1 = 0;

    private byte paletteNum = 1; //使用的色板编号
    private byte isPointTempON = 0; //中心点/高温点/低温点是否开启，二进制表示，1：打开，0：关闭，0x07：全开，0x04：中心点打开
    private byte viewMode = 1; //记录当前图像模式，0：可将光模式，1：红外模式，2：热融合模式，3：画中画模式，4：iMIX模式，默认值4
    private byte tempUnit = 0;  //记录当前温度单位， 0：摄氏度，1：开尔文温度，2：华氏度
    private byte deviceType = 0; //记录当前设备型号， 0：M3， 1：LT 2:ATF
    private byte bTImage = 0;   //记录温度成像状态， 0：关， 1：开

    private byte end[] = new byte[]{(byte) 0xac, (byte) 0xca};


//    public IrgData() {
//        if (tempWidth==0)
//        {
//            setTempWidth(irWidth);
//            setTempHeight(irHeight);
//        }
//        if (visibleHeight==0)
//        {
////            visibleHeight=irHeight;
////            visibleWiidth=irWidth;
//            setVisibleHeight(irHeight);
//            setVisibleWiidth(irWidth);
//
//        }
//    }


    public void setStart(byte[] start) {
        this.start = start;
    }

    public void setEnd(byte[] end) {
        this.end = end;
    }

    public byte getTempUnit() {
        return tempUnit;
    }

    public void setTempUnit(byte tempUnit) {
        this.tempUnit = tempUnit;
    }

    public byte getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(byte deviceType) {
        this.deviceType = deviceType;
    }

    public short getIrWidth() {
        return irWidth;
    }

    public short getIrHeight() {
        return irHeight;
    }

    public short getTempWidth() {
        return tempWidth;
    }

    public short getTempHeight() {
        return tempHeight;
    }

    public short getVisibleWiidth() {
        return visibleWiidth;
    }

    public short getVisibleHeight() {
        return visibleHeight;
    }

    public float getEmissity() {
        return emissity;
    }

    public float getTemperature() {
        return temperature;
    }

    public float getRefectTemp() {
        return refectTemp;
    }

    public float getAtmosphericPermeability() {
        return atmosphericPermeability;
    }

    public float getDistance() {
        return distance;
    }

    public float getHumidity() {
        return humidity;
    }

    public float getK0() {
        return K0;
    }

    public float getB0() {
        return B0;
    }

    public float getK1() {
        return K1;
    }

    public float getB1() {
        return B1;
    }


//    public byte[] getOriTempData() {
//        return oriTempData;
//    }
//
//    public void setOriTempData(byte[] oriTempData) {
//        this.oriTempData = oriTempData;
//    }
//
//    public void setTempKelvin(float[] tempKelvin) {
//        this.tempKelvin = tempKelvin;
//    }

    public byte[] getOriTemp() {
        return oriTemp;
    }

    public void setOriTemp(byte[] oriTemp) {
        IrgData.oriTemp = oriTemp;
    }

    public void setIrWidth(short irWidth) {
        this.irWidth = irWidth;
    }

    public void setIrHeight(short irHeight) {
        this.irHeight = irHeight;
    }

    public void setTempWidth(short tempWidth) {
        this.tempWidth = tempWidth;
    }

    public void setTempHeight(short tempHeight) {
        this.tempHeight = tempHeight;
    }

    public void setVisibleWiidth(short visibleWiidth) {
        this.visibleWiidth = visibleWiidth;
    }

    public void setVisibleHeight(short visibleHeight) {
        this.visibleHeight = visibleHeight;
    }

    public void setEmissity(float emissity) {
        this.emissity = emissity;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public void setRefectTemp(float refectTemp) {
        this.refectTemp = refectTemp;
    }

    public void setAtmosphericPermeability(float atmosphericPermeability) {
        this.atmosphericPermeability = atmosphericPermeability;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public void setK0(float k0) {
        K0 = k0;
    }

    public void setB0(float b0) {
        B0 = b0;
    }

    public void setK1(float k1) {
        K1 = k1;
    }

    public void setB1(float b1) {
        B1 = b1;
    }

    public byte getPaletteNum() {
        return paletteNum;
    }

    public void setPaletteNum(byte paletteNum) {
        this.paletteNum = paletteNum;
    }

    public void setIsPointTempON(byte isPointTempON) {
        this.isPointTempON = isPointTempON;
    }

    public void setViewMode(byte viewMode) {
        this.viewMode = viewMode;
    }

    public byte getbTImage() {
        return bTImage;
    }

    public void setbTImage(byte bTImage) {
        this.bTImage = bTImage;
    }

    public String saveToIRG(byte[] irData) {

        //判断sd卡是否存在
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //文件名
            long systemTime = System.currentTimeMillis();
//            String imageDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date(systemTime));
            String mFileName = String.format(SCREENSHOT_FILE_NAME_TEMPLATE, DeviceUtils.DEVICE_PN,DeviceUtils.DEVICE_SN, String.valueOf(systemTime));

            File dir = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "iRayUVC");
            //判断文件是否存在，不存在则创建
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(dir, mFileName);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.i("tag", "file path:" + file.getAbsolutePath());

            //将float的开尔文温度*10再*10000，并用byte数组存储
//            byte[] tempKelByte = new byte[640 * 512 * 4];
//            for (int i = 0; i < tempKelvin.length; i++) {
//                int bufKel = (int) (tempKelvin[i] * 10000);
//                byte[] bufKelByte = new byte[4];
//                bufKelByte = GetImageUtils.converInt2Byte(bufKel);
//                tempKelByte[i * 4] = bufKelByte[0];
//                tempKelByte[i * 4 + 1] = bufKelByte[1];
//                tempKelByte[i * 4 + 2] = bufKelByte[2];
//                tempKelByte[i * 4 + 3] = bufKelByte[3];
//            }

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                if (fos != null) {
                    //文件头
                    fos.write(start);
                    //自定义头大小
                    fos.write(GetImageUtils.convertShort2Byte(length));
                    //红外数据面阵大小
                    fos.write(GetImageUtils.converInt2Byte(irWidth * irHeight));
                    //红外数据面阵高度
                    fos.write(GetImageUtils.convertShort2Byte(irHeight));
                    //红外数据面阵宽度
                    fos.write(GetImageUtils.convertShort2Byte(irWidth));
                    //红外每个像素点所占字节数
                    fos.write(irUnitSize);
                    //测温数据面阵大小
                    fos.write(GetImageUtils.converInt2Byte(irWidth * irHeight * 2));
                    //测温数据面阵高度
                    fos.write(GetImageUtils.convertShort2Byte(irHeight));
                    //测温数据面阵宽度
                    fos.write(GetImageUtils.convertShort2Byte(irWidth));
                    //测温每个像素点所占字节数
                    fos.write(tempUnitSize);
                    //可见光面阵大小
                    fos.write(GetImageUtils.converInt2Byte(irHeight * irWidth * 3));
                    //可见光面阵高度
                    fos.write(GetImageUtils.convertShort2Byte(irHeight));
                    //可将光面阵宽度
                    fos.write(GetImageUtils.convertShort2Byte(irWidth));
                    //发射率
                    fos.write(GetImageUtils.converInt2Byte((int) (emissity * 10000)));
                    //反射温度
                    fos.write(GetImageUtils.converInt2Byte((int) (refectTemp * 10000)));
                    //大气温度
                    fos.write(GetImageUtils.converInt2Byte((int) (temperature * 10000)));
                    //距离
                    fos.write(GetImageUtils.converInt2Byte((int) (distance * 10000)));
                    //湿度
                    fos.write(GetImageUtils.converInt2Byte((int) (humidity * 10000)));
                    //大气透过率
                    fos.write(GetImageUtils.converInt2Byte((int) (atmosphericPermeability * 10000)));
                    //距离修正系数K0
                    fos.write(GetImageUtils.converInt2Byte((int) (K0 * 10000)));
                    //距离修正系数B0
                    fos.write(GetImageUtils.converInt2Byte((int) (B0 * 10000)));
                    //距离修正系数K1
                    fos.write(GetImageUtils.converInt2Byte((int) (K1 * 10000)));
                    //距离修正系数B1
                    fos.write(GetImageUtils.converInt2Byte((int) (B1 * 10000)));
                    //使用的色板编号
                    fos.write(paletteNum);
                    //中心点高温点低温点是否开启
                    fos.write(isPointTempON);
                    //记录当前图像模式
                    fos.write(viewMode);
                    //温度单位
                    fos.write(tempUnit);
                    //机芯型号
                    fos.write(deviceType);
                    //温度成像状态
                    fos.write(bTImage);
                    //保留位（50byte）
                    fos.write(new byte[50]);
                    //自定义头结束
                    fos.write(end);
                    //红外数据
                    fos.write(irData);
                    //温度数据
                    fos.write(oriTemp, 0, irWidth * irHeight * 2);
                    //可见光数据
                    fos.write(new byte[irHeight * irWidth * 3]);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return mFileName;
        }
        return null;
    }

}
