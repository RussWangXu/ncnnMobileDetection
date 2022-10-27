package com.iray.infiray_lt_m3_sdk.utils;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.iray.infiray_lt_m3_sdk.utils.Util.GetImageUtils;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCameraM3;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.serenegiant.utils.UIThreadHelper.runOnUiThread;
import static java.lang.Thread.sleep;
import static java.nio.charset.StandardCharsets.US_ASCII;

public class CameraUtils implements CameraDialog.CameraDialogParent {

    //region ---------- 变量
    private static String TAG = "CameraUtils";
    private static CameraUtils instance = null;
    Context context;
    public static USBMonitor mUSBMonitor;
    public UVCCameraM3 mUVCCamera;
    private final static int HEX_HEAD_55 = 0x55;

    //public ReceiveTaskThread ReceiveTask;
    //Qiaoxp add
    private boolean atfDeviceFlag = true;
    IrgData irgData = new IrgData();
    byte[] kelByteBuffer = new byte[2];
    public static int bTempUnit = 0;//温度单位标志，0-摄氏度 1-开尔文温度 2-华氏度


    //public static int tagModel;   //机芯类型 0-M3 1-LT 2-ATF
    //public boolean tagHDevice = false;     //人体测温机芯标志
    //public int bTImage;    //温度成像开关 0-关 1-开
    //public boolean tagReadingTempRange;    //读取测温范围标志
    //public int bTempRange;//测温范围切换标志， 0-高增益 1-低增益
    public int tagPseColor; //色板编号 0-白热
    //public boolean tagReadingPseColor; //读取伪彩开关
    //public boolean bTemp;   //测温型机芯标志
    //public int tempType; //0:人体测温  1：工业测温



    public static final int INIT_DEVICE_MODE = 99;
    public static final int IS_LT_DEVICE = 100;
    public static final int IS_M3_DEVICE = 101;
    //Qiaoxp add
    public static final int IS_ATF_DEVICE = 102;
    //温度单位摄氏度
    public static final int DISPLAY_TEMP_UNIT_C = 103;
    //温度单位开尔文
    public static final int DISPLAY_TEMP_UNIT_K = 104;
    //温度单位华氏度
    private static boolean spTempUnit_Flag = true;
    public static final int DISPLAY_TEMP_UNIT_F = 105;
    public static final int SET_PARAM_FAIL = 111;
    public static final int SET_TEMP_RANGE_SUCCESS = 120;
    public static final int SET_TEMP_RANGE_FAIL = 121;

    //参数
    //public float emissity, atmTemp, refTemp, atmPrem, distance, fpt;

    //面阵
    public static int DEFAULT_PREVIEW_WIDTH;
    public static int DEFAULT_PREVIEW_HEIGHT;

    public static float[] tempInfo;

    ////////////////////////////////////////////////////////////////

    //串口
    public boolean _isInit = false;     //正在初始化
    public boolean _isUsb;              //串口机型标志
    public UsbInterface usbInterface = null;
    public UsbEndpoint usbEndpointIn = null;
    public UsbEndpoint usbEndpointOut = null;
    public USBMonitor.UsbControlBlock mUsbControlBlock;
    //END

    //接收指令
    public int cmdGetFlag;  //0-停止接收返回 1-接收返回指令
    public int inMax;       //数据最大长度
    public ByteBuffer byteBuffer;
    public UsbRequest usbRequest;
    //END

    //endregion

    //region ---------- 初始化

    public CameraUtils(Context context, USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener, Handler mHandler) {
        this.context = context;
        mUSBMonitor = new USBMonitor(context, mOnDeviceConnectListener);
        receiveTask_Nano = new ReceiveTaskThread_Nano(mHandler);
        _isUsb = true;
        cmdGetFlag = 0;
        _isInit = false;
        _cmdType = 0;
        Log.w(TAG, "----------CameraUtils");
    }

    public static synchronized CameraUtils getInstance(Context context, USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener, Handler mHandler) {
        Log.w(TAG, "----------getInstance1");
        if (null == instance) {
            Log.w(TAG, "----------getInstance2");
            instance = new CameraUtils(context, mOnDeviceConnectListener, mHandler);
        }
        return instance;
    }

    /**
     * 初始化机芯
     *
     * @param mUVCCamera2
     * @param mHandler
     */
    public void initModel(UVCCameraM3 mUVCCamera2, Handler mHandler) {
        mUVCCamera = mUVCCamera2;
        Log.w(TAG, "----------initModel");
        _isInit = true;
        cmdGetFlag = 0;
        _cmdType = 0;
        sdk_getSoftVersion(mHandler);
    }

    /**
     * 初始化串口
     *
     * @param device
     */
    public void initSerialPort(UsbDevice device, Handler mHandler) {
        Log.e("usb1", "----------device.getInterfaceCount() "+device.getInterfaceCount());
        for (int i = 0; i < device.getInterfaceCount(); i++) {
            Log.e(TAG, "initSerialPort: "+device.getInterface(i)+" endpointCount="+device.getInterface(i).getEndpointCount());
            if (device.getInterface(i).getEndpointCount() == 2) {
                usbInterface = device.getInterface(i);
                _isUsb = true;
            }
        }
        if (_isUsb) {
            Log.e("usb1", "----------endpointCount: " + usbInterface.getEndpointCount());
            for (int index = 0; index < usbInterface.getEndpointCount(); index++) {
                UsbEndpoint point = usbInterface.getEndpoint(index);
                if (point.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    if (point.getDirection() == UsbConstants.USB_DIR_IN) {
                        usbEndpointIn = point;
                        Log.e("usb1", "----------usbEndpointIn: " + usbEndpointIn);
                    } else if (point.getDirection() == UsbConstants.USB_DIR_OUT) {
                        usbEndpointOut = point;
                        Log.e("usb1", "----------usbEndpointOut: " + usbEndpointOut);
                    }
                }
            }
            for (int index = 0; index < usbInterface.getEndpointCount(); index++) {
                UsbEndpoint point = usbInterface.getEndpoint(index);
                if (point.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    if (point.getDirection() == UsbConstants.USB_DIR_IN) {
                    } else if (point.getDirection() == UsbConstants.USB_DIR_OUT) {
                    }
                }
            }
            if (usbEndpointIn != null) {
                Log.e("usb1", "----------initSerialPort: usbEndpointIn=" + usbEndpointIn);
                inMax = usbEndpointIn.getMaxPacketSize();
                byteBuffer = ByteBuffer.allocate(inMax);
                usbRequest = new UsbRequest();
                boolean res=usbRequest.initialize(mUsbControlBlock.mConnection, usbEndpointIn);
                Log.e(TAG, "initSerialPort: usbRequest.initialize"+res+" byteBuffer="+byteBuffer+" "+inMax);
            }
        }
    }

    /**
     * to access from CameraDialog
     *
     * @return
     */
    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // FIXME
                }
            }, 0);
        }
    }

    public void showDialog(Activity activity) {
        CameraDialog.showDialog(activity);
    }

    //endregion


    public ReceiveTaskThread_Nano receiveTask_Nano;
    private boolean _isRequestData = false;//是否请求返回数据
    private int _requestDataState = 0;//请求数据状态，0=未获取，1=开始请求，2=数据准备好
    private int _cmdType = 0;//执行命令类型，0=无，

    public class ReceiveTaskThread_Nano extends Thread {
        private Handler mHandler;

        public ReceiveTaskThread_Nano(Handler mHandler) {
            this.mHandler = mHandler;
            this.setName("ReceiveTaskThread_Nano");
        }

        @Override
        public synchronized void start() {
            Log.i(TAG, "----------ReceiveTaskThread_Nano.start");
            super.start();
        }

        public void Stop() {
            cmdGetFlag = 0;
        }

        @Override
        public void run() {
            while (cmdGetFlag == 1) {
                Log.e(TAG, "----------------run1: ");
                usbRequest.queue(byteBuffer, inMax);
                Log.e(TAG, "----------------run2: ");
                //Log.e(TAG, "----------------run2: " + mUsbControlBlock.mConnection.requestWait());

                if (mUsbControlBlock.mConnection.requestWait() == usbRequest) {
                    Log.e(TAG, "----------------run3: ");
                    if(_cmdType == 0)
                        continue;
                    Log.e(TAG, "----------------run4: ");
                    final byte[] retData = byteBuffer.array();
                    Log.e(TAG, "----------run: retData1:" + Arrays.toString(retData));


                    if(_isRequestData == false){//无需返回数据，如快门校正
                        Log.e(TAG, "----------run: retData2:" + DataUtils.byteArrayToHexString(retData));
                        if(retData[0]==0 && retData[1]==0) {
                            Log.e(TAG, "----------命令执行成功，命令类型：" + _cmdType);
                            switch (_cmdType) {
                                case 10002://背景校正
                                case 10003://自动快门
                                case 10005://teclessB
                                case 10006://保存设置成功
                                case 20007://设置亮度对比度
                                    mHandler.sendEmptyMessage(_cmdType);
                                    break;
                            }
                        } else{
                            Log.e(TAG, "----------命令执行失败，命令类型：" + _cmdType);
                        }

                        _cmdType = 0;
                        cmdGetFlag = 0;
                        Log.e(TAG, "----------结束1");
                        this.Stop();
                    } else{//需要返回数据，如读取sn
                        if(_requestDataState == 0){//请求数据状态，没有开始获取数据
                            if(retData[0]!=0 && retData[1]!=0) {
                                Log.e(TAG, "----------命令执行失败，命令类型：" + _cmdType);
                                _cmdType = 0;
                                cmdGetFlag = 0;
                                this.Stop();
                            }else{
                                _requestDataState = 1;//查询数据准备状态
                                commandSendRequestState(mHandler);
                            }
                        }else if(_requestDataState == 1){
                            if(retData[0]==0 && retData[1]==1) {//数据未准备好，再次请求
                                commandSendRequestState(mHandler);
                            }else if(retData[0]==0 && retData[1]==0){//数据准备好，可以获取数据了
                                _requestDataState = 2;
                                commandSendRequestData(mHandler);
                            }
                        } else if(_requestDataState == 2) {
                            if (retData[0] != 0x00) {
                                Log.e(TAG, "----------获取数据失败，命令类型：" + _cmdType);
                            } else {
                                //20001=fpa温度，20002=软件版本，20003=读取sn，20004=读取pn，20005=探测器id，20006=亮度对比度
                                Log.e(TAG, "----------返回数据_cmdType：" + _cmdType);
                                Log.e(TAG, "----------run: retData2:" + DataUtils.byteArrayToHexString(retData));
                                switch (_cmdType) {
                                    case 20001:
                                        _getFpa(retData);
                                        break;
                                    case 20002:
                                        _getSoftVersion(retData);
                                        break;
                                    case 20003:
                                        _getDeviceSN(retData);
                                        break;
                                    case 20004:
                                        _getDevicePN(retData);
                                        break;
                                    case 20005:
                                        _getDetectorID(retData);
                                        break;
                                    case 20006:
                                        _getContrast(retData);
                                        break;
                                }

                                if(_isInit) {
                                    _isInit = false;
                                } else {
                                    mHandler.sendEmptyMessage(_cmdType);
                                }
                            }

                            _cmdType = 0;
                            cmdGetFlag = 0;
                            _requestDataState = 0;
                            this.Stop();
                            Log.e(TAG, "----------结束2");
                        }
                    }
                }//end if
            }//end while
        }//end run
    }

    //region ---------- 指令

    //发送长指令
    private void commandSendNano_long(UVCCameraM3 mUVCCamera, Handler mHandler, int sendType, boolean isRequestData,
                                      int cmdType, int cmd, int subCmd,
                                      int para1, int para2, int para3, int para4, int para5, int para6) {
        Log.i(TAG, "-----------commandSendNano_long");
        if(_cmdType!=0){
            Log.i(TAG, "-----------其他命令正在执行，失败！_cmdType="+ _cmdType);
            return;
        }

        Log.i(TAG, "-----------commandSendNano: cmdGetFlag:" + cmdGetFlag + "       tagUsbNew: " + _isUsb);
        if (cmdGetFlag != 1 && _isUsb) {
            cmdGetFlag = 1;
            receiveTask_Nano = new ReceiveTaskThread_Nano(mHandler);
            receiveTask_Nano.start();
            Log.i(TAG, "-----------ReceiveTask.start()");
        }

        if (mUsbControlBlock != null && mUVCCamera.checkSupportFlag(UVCCameraM3.CTRL_ZOOM_ABS)) {
            byte[] send = new byte[14];
            send[0] = (byte) (0xAA & 0xff);
            send[1] = (byte) cmdType;
            send[2] = (byte) 0x08;
            send[3] = (byte) 0x1D;
            send[4] = (byte) 0x00;

            send[5] = (byte) cmd;
            send[6] = (byte) (subCmd & 0xff);
            send[7] = (byte) para1;
            send[8] = (byte) para2;
            send[9] = (byte) para3;
            send[10] = (byte) para4;
            send[11] = (byte) para5;
            send[12] = (byte) para6;

            int sum = send[1] + send[2] + send[3] + send[4]
                    + send[5] + send[6] + send[7] + send[8] + send[9] + send[10] + send[11] + send[12];

            send[13] = (byte) sum;

            Log.e(TAG, "--------------sendType: " + sendType);
            //Log.e(TAG, "--------------cmdType: " + cmdType);
            String msg = DataUtils.byteArrayToHexString(send);
            Log.e(TAG, "--------------commandSendNano: " + msg);

            _cmdType = sendType;//指令类型
            _isRequestData = isRequestData;//是否需要返回数据
            int r=mUsbControlBlock.mConnection.bulkTransfer(usbEndpointOut, send, send.length, 200);
            Log.e(TAG, "---------commandSendNano_long: res="+r+"   usbEndpointOut="+usbEndpointOut.getDirection());
        }
    }
    //发送短指令
    private void commandSendNano_small(UVCCameraM3 mUVCCamera, Handler mHandler, int sendType, boolean isRequestData,
                                       int cmdType, int cmd, int subCmd,
                                       int para1, int para2, int para3) {
        Log.i(TAG, "-----------commandSendNano_small");

        if(_cmdType!=0){
            Log.i(TAG, "-----------其他命令正在执行，失败！_cmdType="+ _cmdType);
            return;
        }

        Log.i(TAG, "-----------commandSendNano: cmdGetFlag:" + cmdGetFlag + "       tagUsbNew: " + _isUsb);
        if (cmdGetFlag != 1 && _isUsb) {
            cmdGetFlag = 1;
            receiveTask_Nano = new ReceiveTaskThread_Nano(mHandler);
            receiveTask_Nano.start();
            Log.i(TAG, "-----------ReceiveTask.start()");
        }

        if (mUsbControlBlock != null && mUVCCamera.checkSupportFlag(UVCCameraM3.CTRL_ZOOM_ABS)) {
            byte[] send = new byte[11];
            send[0] = (byte) (0xAA & 0xff);
            send[1] = (byte) cmdType;
            send[2] = (byte) 0x05;
            send[3] = (byte) 0x1D;
            send[4] = (byte) 0x00;

            send[5] = (byte) cmd;
            send[6] = (byte) (subCmd & 0xff);
            send[7] = (byte) para1;
            send[8] = (byte) para2;
            send[9] = (byte) para3;

            int sum = send[1] + send[2] + send[3] + send[4]
                    + send[5] + send[6] + send[7] + send[8] + send[9];

            send[10] = (byte) sum;

            Log.e(TAG, "--------------sendType: " + sendType);
            Log.e(TAG, "--------------cmdType: " + _cmdType);
            String msg = DataUtils.byteArrayToHexString(send);
            Log.e(TAG, "--------------commandSendNano: " + msg);

            _cmdType = sendType;//指令类型
            _isRequestData = isRequestData;//是否需要返回数据
            mUsbControlBlock.mConnection.bulkTransfer(usbEndpointOut, send, send.length, 200);
        }
    }

    //读取数据状态
    private void commandSendRequestState(Handler mHandler) {
        //Log.i(TAG, "-----------commandSendRequestState: cmdGetFlag:" + cmdGetFlag + "       tagUsbNew: " + _isUsb);
        if (cmdGetFlag != 1 && _isUsb) {
            cmdGetFlag = 1;
            receiveTask_Nano = new ReceiveTaskThread_Nano(mHandler);
            receiveTask_Nano.start();
            //Log.i(TAG, "-----------ReceiveTask.start()");
        }

        if (mUsbControlBlock != null && mUVCCamera.checkSupportFlag(UVCCameraM3.CTRL_ZOOM_ABS)) {
            byte[] send = new byte[6];
            send[0] = (byte) (0xAA & 0xff);
            send[1] = (byte) 0x79;
            send[2] = (byte) 0x01;
            send[3] = (byte) 0x02;

            send[4] = (byte) 0x00;

            int sum = send[1] + send[2] + send[3] + send[4];
            send[5] = (byte) sum;

            String msg = DataUtils.byteArrayToHexString(send);
            Log.e(TAG, "--------------commandSendRequestState send: " + msg);

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mUsbControlBlock.mConnection.bulkTransfer(usbEndpointOut, send, send.length, 200);
        }
    }

    //读取数据
    private void commandSendRequestData(Handler mHandler) {
        //Log.i(TAG, "-----------commandSendRequestData: cmdGetFlag:" + cmdGetFlag + "       tagUsbNew: " + _isUsb);
        if (cmdGetFlag != 1 && _isUsb) {
            cmdGetFlag = 1;
            receiveTask_Nano = new ReceiveTaskThread_Nano(mHandler);
            receiveTask_Nano.start();
            //Log.i(TAG, "-----------ReceiveTask.start()");
        }

        if (mUsbControlBlock != null && mUVCCamera.checkSupportFlag(UVCCameraM3.CTRL_ZOOM_ABS)) {
            int dataLen = 0;//返回的数据长度，去掉arr[0]=0和arr[n]校验位
            //20001=fpa温度，20002=软件版本，20003=读取sn， 20004=读取pn，  20005=探测器id，20006=亮度对比度
            switch (_cmdType){

                case 20001:
                    dataLen = 14;
                    break;
                case 20002:
                    dataLen = 16;
                    break;
                case 20003:
                    dataLen = 18;
                    break;
                case 20004:
                    dataLen = 31;
                    break;
                case 20005:
                    dataLen = 18;
                    break;
                case 20006:
                    dataLen = 46;
                    break;
            }

            byte[] send = new byte[6];
            send[0] = (byte) (0xAA & 0xff);
            send[1] = (byte) 0x79;
            send[2] = (byte)  (dataLen & 0xff);
            send[3] = (byte) 0x1D;

            send[4] = (byte) 0x00;

            int sum = send[1] + send[2] + send[3] + send[4];
            send[5] = (byte) sum;

            String msg = DataUtils.byteArrayToHexString(send);
            Log.e(TAG, "--------------commandSendRequestData send: " + msg);
            mUsbControlBlock.mConnection.bulkTransfer(usbEndpointOut, send, send.length, 200);
        }
    }

    /**
     * 设置伪彩
     */
    public void sdk_setPseColor(Handler mHandler, int index) {

        Log.i(TAG, "-----------sdk_setPseColor");
        if(_cmdType!=0){
            Log.i(TAG, "-----------其他命令正在执行，失败！_cmdType=" + _cmdType);
            return;
        }

        Log.i(TAG, "-----------setPseColor: cmdGetFlag:" + cmdGetFlag + "       tagUsbNew: " + _isUsb);
        if (cmdGetFlag != 1 && _isUsb) {
            cmdGetFlag = 1;
            receiveTask_Nano = new ReceiveTaskThread_Nano(mHandler);
            receiveTask_Nano.start();
            Log.i(TAG, "-----------ReceiveTask.start()");
        }

        if (mUsbControlBlock != null && mUVCCamera.checkSupportFlag(UVCCameraM3.CTRL_ZOOM_ABS)) {
            byte[] send = new byte[7];
            send[0] = (byte) (0xAA & 0xff);
            send[1] = (byte) 0x78;
            send[2] = (byte) 0x01;
            send[3] = (byte) 0x0C;
            send[4] = (byte) 0x05;

            send[5] = (byte) index;

            int sum = send[1] + send[2] + send[3] + send[4]
                    + send[5] ;
            send[6] = (byte) sum;

            String msg = DataUtils.byteArrayToHexString(send);
            Log.e(TAG, "--------------doShutter send: " + msg);

            _cmdType = 101;//打快门
            _isRequestData = false;//不需要返回数据
            mUsbControlBlock.mConnection.bulkTransfer(usbEndpointOut, send, send.length, 200);
        }
    }

    /**
     * 上下镜像
     *     //1=上下，0=还原
     */
    public void sdk_setMirror(Handler mHandler, int index) {
        int state = (index==1)?0x01:0x00;
        commandSendNano_small(mUVCCamera, mHandler, 1001, false,
                0x78, 0x81, 0x01,
                0xA0, 0xD8, state);
    }

    /**
     *左右翻转
     *     1=左右，0=还原
     */
    public void sdk_setFlip(Handler mHandler, int index) {
        int state = (index==1)?0x01:0x00;
        commandSendNano_small(mUVCCamera, mHandler, 1002, false,
                0x78, 0x81, 0x01,
                0x87, 0x20, state);
    }

    /**
     *打快门
     */
    public void sdk_setShutter(Handler mHandler) {
        commandSendNano_long(mUVCCamera, mHandler, 10001, false,
                0x78, 0x0D, 0xC1,
                0x02, 0x00, 0x00, 0x00, 0x00, 0x00);

//        Log.i(TAG, "-----------sdk_setShutter");
//        if(_cmdType!=0){
//            Log.i(TAG, "-----------其他命令正在执行，失败！_cmdType="+ _cmdType);
//            return;
//        }
//
//        Log.i(TAG, "-----------setShutter: cmdGetFlag:" + cmdGetFlag + "       tagUsbNew: " + _isUsb);
//        if (cmdGetFlag != 1 && _isUsb) {
//            cmdGetFlag = 1;
//            receiveTask_Nano = new ReceiveTaskThread_Nano(mHandler);
//            receiveTask_Nano.start();
//            Log.i(TAG, "-----------ReceiveTask.start()");
//        }
//
//        if (mUsbControlBlock != null && mUVCCamera.checkSupportFlag(UVCCameraM3.CTRL_ZOOM_ABS)) {
//            byte[] send = new byte[14];
//            send[0] = (byte) (0xAA & 0xff);
//            send[1] = (byte) 0x78;
//            send[2] = (byte) 0x08;
//            send[3] = (byte) 0x1D;
//            send[4] = (byte) 0x00;
//
//            send[5] = (byte) 0x0D;
//            send[6] = (byte) (0xC1 & 0xff);
//            send[7] = (byte) 0x02;
//            send[8] = (byte) 0x00;
//            send[9] = (byte) 0x00;
//            send[10] = (byte) 0x00;
//            send[11] = (byte) 0x00;
//            send[12] = (byte) 0x00;
//
//            int sum = send[1] + send[2] + send[3] + send[4]
//                    + send[5] + send[6] + send[7] + send[8] + send[9] + send[10] + send[11] + send[12];
//            send[13] = (byte) sum;
//
//            String msg = DataUtils.byteArrayToHexString(send);
//            Log.e(TAG, "--------------setShutter send: " + msg);
//
//            _cmdType = 10001;//打快门
//            _isRequestData = false;//不需要返回数据
//            mUsbControlBlock.mConnection.bulkTransfer(usbEndpointOut, send, send.length, 200);
//        }
    }

    /**
     *背景校正
     */
    public void sdk_setCorrection(Handler mHandler){
        commandSendNano_long(mUVCCamera, mHandler, 10002, false,
                0x78, 0x0D, 0xC1,
                0x03, 0x00, 0x00, 0x00, 0x00, 0x00);
    }

    /**
     *快门自动状态
     *     1=开启，0=关闭
     */
    public void sdk_setShutterAuto(Handler mHandler, int index){
        int state = (index==1)?0x09:0x08;
        commandSendNano_long(mUVCCamera, mHandler, 10003, false,
                0x78, 0x0A, state,
                0x11, 0x00, 0x00, 0x00, 0x00, 0x00);
    }

    /**
     *缩放
     *    1=放大，0=缩小
     */
    public void sdk_setZoom(Handler mHandler, int index){
        int state = (index==1)?0x0F:0x10;
        commandSendNano_long(mUVCCamera, mHandler, 10004, false,
                0x78, 0x0D, 0xC1,
                state, 0x00, 0x00, 0x00, 0x00, 0x00);
    }

    /**
     *teclessB
     *   1=开启，0=关闭
     */
    public void sdk_setTeclessB(Handler mHandler, int index){
        int state = (index==1)?0x07:0x06;
        commandSendNano_long(mUVCCamera, mHandler, 10005, false,
                0x78, 0x0A, state,
                0x11, 0x00, 0x00, 0x00, 0x00, 0x00);
    }

    /**
     *保存当前图像设置
     */
    public void sdk_setSaveCurrentConfig(Handler mHandler){
        commandSendNano_long(mUVCCamera, mHandler, 10006, false,
                0x78, 0x01, 0xF3,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00);
    }

    /**
     *获取fpa温度
     */
    public void sdk_getFpa(Handler mHandler){
        commandSendNano_long(mUVCCamera, mHandler, 20001, true,
                0x78, 0x01, 0x81,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x06);
    }

    /**
     *获取软件版本
     */
    public void sdk_getSoftVersion(Handler mHandler){
        commandSendNano_long(mUVCCamera, mHandler, 20002, true,
                0x78, 0x01, 0x82,
                0x01, 0x75, 0x70, 0x00, 0x00, 0x08);
    }

    /**
     *获取sn
     */
    public void sdk_getSN(Handler mHandler) {
        commandSendNano_long(mUVCCamera, mHandler, 20003, true,
                0x78, 0x01, 0x82,
                0x01, 0x74, 0x70, 0x00, 0x00, 0x0A);

//        Log.i(TAG, "-----------sdk_getSN");
//
//        if(_cmdType!=0){
//            Log.i(TAG, "-----------其他命令正在执行，失败！_cmdType="+ _cmdType);
//            return;
//        }
//
//        Log.i(TAG, "-----------readSN: cmdGetFlag:" + cmdGetFlag + "       tagUsbNew: " + _isUsb);
//        if (cmdGetFlag != 1 && _isUsb) {
//            cmdGetFlag = 1;
//            receiveTask_Nano = new ReceiveTaskThread_Nano(mHandler);
//            receiveTask_Nano.start();
//            Log.i(TAG, "-----------ReceiveTask.start()");
//        }
//
//        if (mUsbControlBlock != null && mUVCCamera.checkSupportFlag(UVCCameraM3.CTRL_ZOOM_ABS)) {
//            byte[] send = new byte[14];
//            send[0] = (byte) (0xAA & 0xff);
//            send[1] = (byte) 0x78;
//            send[2] = (byte) 0x08;
//            send[3] = (byte) 0x1D;
//            send[4] = (byte) 0x00;
//
//            send[5] = (byte) 0x01;
//            send[6] = (byte) (0x82 & 0xff);
//            send[7] = (byte) 0x01;
//            send[8] = (byte) 0x74;
//            send[9] = (byte) 0x70;
//            send[10] = (byte) 0x00;
//            send[11] = (byte) 0x00;
//            send[12] = (byte) 0x0A;
//
//            int sum = send[1] + send[2] + send[3] + send[4]
//                    + send[5] + send[6] + send[7] + send[8] + send[9] + send[10] + send[11] + send[12];
//            send[13] = (byte) sum;
//
//            String msg = DataUtils.byteArrayToHexString(send);
//            Log.e(TAG, "--------------readSN send: " + msg);
//
//            _cmdType = 20003;//读取sn
//            _isRequestData = true;//需要返回数据
//            mUsbControlBlock.mConnection.bulkTransfer(usbEndpointOut, send, send.length, 200);
//        }
    }

    /**
     *获取pn
     */
    public void sdk_getPn(Handler mHandler){
        commandSendNano_long(mUVCCamera, mHandler, 20004, true,
                0x78, 0x01, 0x82,
                0x01, 0x74, 0x70, 0x10, 0x00, 0x17);
    }

    /**
     *获取探测器id
     */
    public void sdk_getDetectorID(Handler mHandler){
        commandSendNano_long(mUVCCamera, mHandler, 20005, true,
                0x78, 0x01, 0x82,
                0x01, 0x74, 0x70, 0x28, 0x00, 0x0A);
    }


    /**
     *获取亮度对比度
     */
    public void sdk_getContrast(Handler mHandler){
        commandSendNano_long(mUVCCamera, mHandler, 20006, true,
                0x78, 0x09, 0x81,
                0x0C, 0x00, 0x00, 0x00, 0x00, 0x26);
    }

    /**
     *设置亮度对比度
     */
    public void sdk_setContrast(Handler mHandler, float bright, float contrast){
        Log.i(TAG, "-----------sdk_setContrast");

        if(_cmdType!=0){
            Log.i(TAG, "-----------其他命令正在执行，失败！_cmdType="+ _cmdType);
            return;
        }

        Log.i(TAG, "-----------readSN: cmdGetFlag:" + cmdGetFlag + "       tagUsbNew: " + _isUsb);
        if (cmdGetFlag != 1 && _isUsb) {
            cmdGetFlag = 1;
            receiveTask_Nano = new ReceiveTaskThread_Nano(mHandler);
            receiveTask_Nano.start();
            Log.i(TAG, "-----------ReceiveTask.start()");
        }

        if (mUsbControlBlock != null && mUVCCamera.checkSupportFlag(UVCCameraM3.CTRL_ZOOM_ABS)) {
            byte[] send = new byte[52];
            send[0] = (byte) (0xAA & 0xff);
            send[1] =(byte) (0x78 & 0xff);
            send[2] =(byte) (0x2E & 0xff);
            send[3] =(byte) (0x1D & 0xff);
            send[4] =(byte) (0x00 & 0xff);
            send[5] =(byte) (0x09 & 0xff);
            send[6] =(byte) (0xC1 & 0xff);
            send[7] =(byte) (0x0C & 0xff);
            send[8] =(byte) (0x00 & 0xff);
            send[9] =(byte) (0x00 & 0xff);
            send[10] =(byte) (0x00 & 0xff);
            send[11] =(byte) (0x00 & 0xff);
            send[12] =(byte) (0x26 & 0xff);

            _contrastByte[27] = (byte) (0xff & (int)contrast*128/100);
            _contrastByte[28] = (byte) (0xff & (int)bright*128/100);

            for(int i=0;i<38;i++){
                send[i+13] = _contrastByte[i];
            }

            int sum = 0;
            for(int i=1;i<=50;i++){
                sum +=send[i];
            }
            send[51] = (byte) sum;

            String msg = DataUtils.byteArrayToHexString(send);
            Log.e(TAG, "--------------readSN send: " + msg);

            _cmdType = 20007;//亮度对比度（设置）
            _isRequestData = false;//需要返回数据
            mUsbControlBlock.mConnection.bulkTransfer(usbEndpointOut, send, send.length, 200);
        }
    }


    //endregion

    //region ---------- 数据组织

    private void _getSoftVersion(byte[] retData){
        int len = 8;
        byte[] _buf = new byte[len];
        for (int i = 0; i < len; i++) {
            _buf[i] = retData[i + 9];
        }
        Log.e(TAG, "----------_getSoftVersion=" + DataUtils.byteArrayToHexString(_buf));

        if (_buf[0] == 0x00) {
            DeviceUtils.WIDTH = 384;
            DeviceUtils.HEIGHT = 288;
        }
        else if (_buf[0] == 0x01) {
            DeviceUtils.WIDTH = 640;
            DeviceUtils.HEIGHT = 512;
        }

        String version = String.format("NANO%d.%d",  (_buf[4] << 8) + _buf[5], (_buf[6] << 8) + _buf[7]);
        DeviceUtils.SOFT_VERSION = version;
        Log.i(TAG, "------------SOFT_VERSION: " + DeviceUtils.SOFT_VERSION );
    }

    private void _getDeviceSN(byte[] retData) {
        int len = 10;
        byte[] _buf = new byte[len];
        for (int i = 0; i < len ; i++) {
            _buf[i] = retData[i + 9];
        }
        Log.e(TAG, "----------_getDeviceSN=" + DataUtils.byteArrayToHexString(_buf));

        DeviceUtils.DEVICE_SN = new String(_buf, US_ASCII).replaceAll("[^A-Z0-9]", "").trim();  //只保留大写字母及数字
        DeviceUtils.BASE64_DEVICE_SN = Base64.encodeToString(DeviceUtils.DEVICE_SN.getBytes(), Base64.DEFAULT).trim();
        Log.i(TAG, "------------DEVICE_SN: " + DeviceUtils.DEVICE_SN + "       64: " + DeviceUtils.BASE64_DEVICE_SN);
    }

    private void _getDevicePN(byte[] retData) {
        int len = 23;
        byte[] _buf = new byte[len];
        for (int i = 0; i < len; i++) {
            _buf[i] = retData[i + 9];
        }

        DeviceUtils.DEVICE_PN = new String(_buf, US_ASCII).replaceAll("[^A-Z0-9]", "").trim();  //只保留大写字母及数字
        DeviceUtils.BASE64_DEVICE_PN = Base64.encodeToString(DeviceUtils.DEVICE_PN.getBytes(), Base64.DEFAULT).trim();
        Log.i(TAG, "------------DEVICE_PN: " + DeviceUtils.DEVICE_PN + "       64: " + DeviceUtils.BASE64_DEVICE_PN);
    }

    private void _getDetectorID(byte[] retData){
        int len = 10;
        byte[] _buf = new byte[len];
        for (int i = 0; i < len; i++) {
            _buf[i] = retData[i + 9];
        }
        Log.e(TAG, "----------_getDetectorID=" + DataUtils.byteArrayToHexString(_buf));

        DeviceUtils.DETECTOR_ID = new String(_buf, US_ASCII).replaceAll("[^A-Z0-9]", "").trim();  //只保留大写字母及数字
        DeviceUtils.BASE64_DETECTOR_ID = Base64.encodeToString(DeviceUtils.DETECTOR_ID.getBytes(), Base64.DEFAULT).trim();
        Log.i(TAG, "------------DETECTOR_ID: " + DeviceUtils.DETECTOR_ID + "       64: " + DeviceUtils.BASE64_DETECTOR_ID);
    }

    private void _getFpa(byte[] retData) {
        int len = 6;
        byte[] _buf = new byte[len];
        for (int i = 0; i < len; i++) {
            _buf[i] = retData[i + 9];
        }

        Log.e(TAG, "----------_getFpa=" + DataUtils.byteArrayToHexString(_buf));
        int value = ((int)_buf[0]<<8) + _buf[1];
        DeviceUtils.fpaValue = (float) ((float) (value-7543)/(-33.8));
        Log.e(TAG, "----------fpaValue=" + DeviceUtils.fpaValue);
    }

    private byte[] _contrastByte = new byte[38];
    private void _getContrast(byte[] retData) {
        for (int i = 0; i < 38; i++) {
            _contrastByte[i] = retData[i + 9];
        }
        Log.e(TAG, "----------_getContrast=" + DataUtils.byteArrayToHexString(retData));
        DeviceUtils.contrastValue = (float) (_contrastByte[27]*100/128.0);
        DeviceUtils.brightValue = (float) (_contrastByte[28]*100/128.0);
        Log.e(TAG, "----------lightValue=" + DeviceUtils.brightValue);
        Log.e(TAG, "----------contrastValue=" + DeviceUtils.contrastValue);

        Log.e(TAG, "----------_contrastByte[29]=" + (float) (_contrastByte[29]*100/128.0));
        Log.e(TAG, "----------_contrastByte[30]=" + (float) (_contrastByte[30]*100/128.0));
        Log.e(TAG, "----------_contrastByte[31]=" + (float) (_contrastByte[31]*100/128.0));
        Log.e(TAG, "----------_contrastByte[32]=" + (float) (_contrastByte[32]*100/128.0));

    }

    //endregion

}



