package com.example.ncnnmobiledetection;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.ncnnmobiledetection.utils.UartUtils;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;

public class UsbPointCommand extends UnityPlayerActivity {
    public static final String TAG = "cn.wch.wchusbdriver";
    public static final String ACTION_USB_PERMISSION = "cn.wch.wchusbdriver.USB_PERMISSION";
    public boolean isOpen;
    public int baudRate;
    public byte stopBit;
    public byte dataBit;
    public byte parity;
    public byte flowControl;
    public byte[] writeBuffer;
    public byte[] readBuffer;
    public Handler handler;
    public int retval;
    public int brightness = 3;
    public boolean is_2D;
    public static UsbPointCommand instance = getInstance();
    public CH34xUARTDriver driver;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        initUart();
        initView();
        initUSB();
        initData();
    }

    public static UsbPointCommand getInstance() {
        if (instance == null) {
            instance = new UsbPointCommand();
        }

        return instance;
    }

    public void initView() {
        baudRate = 115200;
        stopBit = 1;
        dataBit = 8;
        parity = 0;
        flowControl = 0;
    }

    @SuppressLint("WrongConstant")
    public void initUart() {
        Log.d("CH34xUARTDriver-------", "初始化");
        if (driver != null && driver.isConnected()) {
            Log.d("CH34xUARTDriver", "已连接到设备");
        } else {
            Log.d("CH34xUARTDriver", "未连接到设备");
            UnityPlayer var10004 = mUnityPlayer;
            Activity var1 = UnityPlayer.currentActivity;
            driver = new CH34xUARTDriver((UsbManager) getSystemService(Context.USB_SERVICE), UnityPlayer.currentActivity,
                    ACTION_USB_PERMISSION);
        }

        if (!driver.UsbFeatureSupported()) {
            Log.d("CH34xUARTDriver", "您的手机不支持USB HOST，请更换其他手机再试！");
        }

        UnityPlayer.currentActivity.getWindow().addFlags(128);
        writeBuffer = new byte[512];
        readBuffer = new byte[512];
        isOpen = false;
    }

    public void initUSB() {
        UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
            public void run() {
                initUart();
                initView();
                initData();
                if (!isOpen) {
                    retval = driver.ResumeUsbList();
                    if (retval == -1) {
                        Log.d("CH34xUARTDriver", "打开设备失败");
                        driver.CloseDevice();
                    } else if (retval == 0) {
                        if (!driver.UartInit()) {
                            Log.d("CH34xUARTDriver", "设备初始化失败");
                            return;
                        }

                        Log.d("CH34xUARTDriver", "打开设备成功");
                        isOpen = true;
                        new readThread().start();
                        if (driver.SetConfig(baudRate, dataBit, stopBit, parity, flowControl)) {
                            Log.d("CH34xUARTDriver", "串口设置成功");
                        } else {
                            Log.d("CH34xUARTDriver", "串口设置失败");
                        }
                    } else {
                        Builder builder = new Builder(UnityPlayer.currentActivity);
                        builder.setIcon(R.mipmap.ic_launcher);
                        builder.setTitle("未授权限");
                        builder.setMessage("确认退出吗？");
                        builder.setPositiveButton("确定", new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                System.exit(0);
                            }
                        });
                        builder.setNegativeButton("返回", new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.show();
                    }
                }

            }
        });
    }


    public void brightnessUp() {
        Log.e("test==========", "brightnessUp");
        if (isOpen) {
            ++brightness;
            if (brightness > 4) {
                --brightness;
                Log.d("CH34xUARTDriver", "已是最高亮度");
            } else {
                byte[] to_send = toByteArray("AA02010" + String.valueOf(brightness) + "55");
                int retval = driver.WriteData(to_send, to_send.length);
                if (retval < 0) {
                    Log.d("CH34xUARTDriver", "写失败");
                }
            }
        } else {
            Log.d("CH34xUARTDriver", "没有打开设备");
        }

    }

    public void brightnessDown() {
        if (isOpen) {
            --brightness;
            if (brightness < 0) {
                ++brightness;
                Log.d("CH34xUARTDriver", "已是最低亮度");
            } else {
                byte[] to_send = toByteArray("AA02010" + brightness + "55");
                int retval = driver.WriteData(to_send, to_send.length);
                if (retval < 0) {
                    Log.d("CH34xUARTDriver", "写失败");
                }
            }
        } else {
            Log.d("CH34xUARTDriver", "没有打开设备");
        }

    }

    public void twoDimensional() {
        if (isOpen) {
            byte[] to_send;
            int retval;
            if (is_2D) {
                is_2D = false;
                to_send = toByteArray(UartUtils.UART_3D);
                retval = driver.WriteData(to_send, to_send.length);
                if (retval < 0) {
                    Log.d("CH34xUARTDriver", "写入失败");
                    is_2D = true;
                }
            } else {
                is_2D = true;
                to_send = toByteArray(UartUtils.UART_2D);
                retval = driver.WriteData(to_send, to_send.length);
                if (retval < 0) {
                    Log.d("CH34xUARTDriver", "写入失败");
                    is_2D = false;
                }
            }
        } else {
            Log.d("CH34xUARTDriver", "没有打开设备");
        }

    }

    public void slamOpen() {
        if (isOpen) {
            byte[] to_send = toByteArray(UartUtils.SLAM_OPEN);
            int retval = driver.WriteData(to_send, to_send.length);
            if (retval < 0) {
                Log.d("CH34xUARTDriver", "写入失败");
            }
        } else {
            Log.d("CH34xUARTDriver", "没有打开设备");
        }

    }

    public void slamClose() {
        if (isOpen) {
            byte[] to_send = toByteArray(UartUtils.SLAM_OFF);
            int retval = driver.WriteData(to_send, to_send.length);
            if (retval < 0) {
                Log.d("CH34xUARTDriver", "写入失败");
            }
        } else {
            Log.d("CH34xUARTDriver", "没有打开设备");
        }

    }

    public void nightVisionOpen() {
        if (isOpen) {
            byte[] to_send = toByteArray(UartUtils.NIGHT_OPEN);
            int retval = driver.WriteData(to_send, to_send.length);
            if (retval < 0) {
                Log.d("CH34xUARTDriver", "写入失败");
            }
        } else {
            Log.d("CH34xUARTDriver", "没有打开设备");
        }

    }

    public void nightVisionClose() {
        if (isOpen) {
            byte[] to_send = toByteArray(UartUtils.NIGHT_OFF);
            int retval = driver.WriteData(to_send, to_send.length);
            if (retval < 0) {
                Log.d("CH34xUARTDriver", "写入失败");
            }
        } else {
            Log.d("CH34xUARTDriver", "没有打开设备");
        }

    }

    public void shimmerOpen() {
        if (isOpen) {
            byte[] to_send = toByteArray(UartUtils.SHIMMER_OPEN);
            int retval = driver.WriteData(to_send, to_send.length);
            if (retval < 0) {
                Log.d("CH34xUARTDriver", "写入失败");
            }
        } else {
            Log.d("CH34xUARTDriver", "没有打开设备");
        }

    }

    public void shimmerClose() {
        if (isOpen) {
            byte[] to_send = toByteArray(UartUtils.SHIMMER_OFF);
            int retval = driver.WriteData(to_send, to_send.length);
            if (retval < 0) {
                Log.d("CH34xUARTDriver", "写入失败");
            }
        } else {
            Log.d("CH34xUARTDriver", "没有打开设备");
        }

    }

    public void initData() {
        handler = new Handler() {
            public void handleMessage(Message msg) {
                String mess = (String) msg.obj;
                if (!mess.isEmpty() && mess.trim().equals("AA010155")) {
                    Toast.makeText(UnityPlayer.currentActivity, "success", Toast.LENGTH_SHORT).show();
                }

            }
        };
    }

    public void onResume() {
        super.onResume();
        if (driver != null && !driver.isConnected()) {
            int retval = driver.ResumeUsbPermission();
            if (retval != 0 && retval == -2) {
                Log.d("CH34xUARTDriver", "获取权限失败");
            }
        }

    }

    public void onDestroy() {
        isOpen = false;
        driver.CloseDevice();
        super.onDestroy();
    }

    public void DestoryActivity() {
        isOpen = false;
        driver.CloseDevice();
    }

    private String toHexString(byte[] arg, int length) {
        String result = new String();
        if (arg != null) {
            for (int i = 0; i < length; ++i) {
                result = result + (Integer.toHexString(arg[i] < 0 ? arg[i] + 256 : arg[i]).length() == 1 ? "0" + Integer.toHexString(arg[i] < 0 ? arg[i] + 256 : arg[i]) : Integer.toHexString(arg[i] < 0 ? arg[i] + 256 : arg[i])) + " ";
            }

            return result;
        } else {
            return "";
        }
    }

    private byte[] toByteArray(String arg) {
        if (arg != null) {
            char[] NewArray = new char[1000];
            char[] array = arg.toCharArray();
            int length = 0;

            int EvenLength;
            for (EvenLength = 0; EvenLength < array.length; ++EvenLength) {
                if (array[EvenLength] != ' ') {
                    NewArray[length] = array[EvenLength];
                    ++length;
                }
            }

            EvenLength = length % 2 == 0 ? length : length + 1;
            if (EvenLength != 0) {
                int[] data = new int[EvenLength];
                data[EvenLength - 1] = 0;

                for (int i = 0; i < length; ++i) {
                    if (NewArray[i] >= '0' && NewArray[i] <= '9') {
                        data[i] = NewArray[i] - 48;
                    } else if (NewArray[i] >= 'a' && NewArray[i] <= 'f') {
                        data[i] = NewArray[i] - 97 + 10;
                    } else if (NewArray[i] >= 'A' && NewArray[i] <= 'F') {
                        data[i] = NewArray[i] - 65 + 10;
                    }
                }

                byte[] byteArray = new byte[EvenLength / 2];

                for (int i = 0; i < EvenLength / 2; ++i) {
                    byteArray[i] = (byte) (data[i * 2] * 16 + data[i * 2 + 1]);
                }

                return byteArray;
            }
        }

        return new byte[0];
    }

    public class readThread extends Thread {
        public readThread() {
        }

        public void run() {
            byte[] buffer = new byte[4096];

            while (true) {
                Message msg = Message.obtain();
                if (!isOpen) {
                    return;
                }

                int length = driver.ReadData(buffer, 4096);
                if (length > 0) {
                    String recv = toHexString(buffer, length);
                    msg.obj = recv;
                    handler.sendMessage(msg);
                }
            }
        }
    }
}