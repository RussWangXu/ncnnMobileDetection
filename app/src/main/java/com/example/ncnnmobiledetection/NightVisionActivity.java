package com.example.ncnnmobiledetection;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;
import android.widget.ImageView;
import android.widget.Toast;

import com.iray.infiray_lt_m3_sdk.utils.CameraUtils;
import com.iray.infiray_lt_m3_sdk.utils.DataUtils;
import com.iray.infiray_lt_m3_sdk.utils.DeviceUtils;
import com.iray.infiray_lt_m3_sdk.utils.IrgData;
import com.iray.infiray_lt_m3_sdk.utils.widget.SimpleUVCCameraTextureView;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.IButtonCallback;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.IStatusCallback;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCameraM3;
import com.serenegiant.utils.HandlerThreadHandler;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;

import static com.iray.infiray_lt_m3_sdk.utils.CameraUtils.mUSBMonitor;

public class NightVisionActivity extends UnityPlayerActivity implements CameraDialog.CameraDialogParent {

    public static int drawWidth;
    public static int drawHeight;
    public static int surfaceWidth;
    public static int widthOffset;
    public boolean thread_Flag = true;

    public float ratio = 3.0f;
    public int ScreenWidth;
    public int ScreenHeight = 480;

    public byte[] recordByte;
    public byte[] shotByteY;
    //float SCALE_MAX = 0; //最大的缩放比例
    public float SCALE_MIN = 0;//最小缩放比例
    public IrgData irgData = new IrgData();
    //public Handler mhandler;
    //public final static int PARA_INIT = 106;
    public int tempUnit = 0;  //0:摄氏度 1，开尔文 2，华氏度

    public final static String TAG = "CameraUtils";
    public Object mSync = new Object();

    // for accessing USB and USB camera
    public SimpleUVCCameraTextureView mUVCCameraView;
    // for open&start / stop&close camera preview

    public Surface mPreviewSurface;
    public ImageView mImageView;


    public int originalWidth;   //机芯面阵宽度
    public int originalHeight;  //机芯面阵高度

    public byte[] imageByte;

    public int[] colors;
    public Bitmap bitmap;
    public byte[] frameByte;
    public float[] tempResult = new float[3];

    public Handler mHandler;
    public CameraUtils cameraUtils;
    public DeviceUtils deviceUtils;


    public int _autoState = 1;     //1=打开，0=关闭
    public int _teclessState = 1;  //1=打开，0=关闭
    public int _mirrorState = 1;   //1=上下，0=还原
    public int _flipState = 1;     //1=左右，0=还原
    public int _pseColorID = 1;    //1,3,4,5,6,7,8,9,10,11
    public int _deviceInfoID = 0;  //0=softVersion,1=sn,2=pn,3=探测器id，4=all
    public int _brightState = 0;   //1=已获取亮度，对比度，0=未获取

    public Toast mToast;
    public static NightVisionActivity instance = getInstance();  //单例类
    public long mWorkerThreadID;
    public Handler mWorkerHandler;
    public Handler mUIHandler = new Handler(Looper.getMainLooper());
    public static final int MAGIC_TEXTURE_ID = 10;
    public String imagePath;
    public String nightVisionString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public static NightVisionActivity getInstance() {
        if (instance == null) {
            instance = new NightVisionActivity();
        }
        return instance;
    }

    //初始化夜视监听方法
    public void initNightVision(String imgPath) {
        //imagePath = imgPath;     getExternalFilesDir("").getAbsolutePath();     Environment.getExternalStorageDirectory().getPath() + "/rgbImage"
        imagePath = imgPath;
        if (mWorkerHandler == null) {
            mWorkerHandler = HandlerThreadHandler.createHandler(NightVisionActivity.class.getSimpleName());
            mWorkerThreadID = mWorkerHandler.getLooper().getThread().getId();
        }

        setHandler();
        cameraUtils = CameraUtils.getInstance(UnityPlayer.currentActivity, mOnDeviceConnectListener, mHandler);
//        mUVCCameraView.setAspectRatio(UVCCameraM3.DEFAULT_PREVIEW_WIDTH / (float) UVCCameraM3.DEFAULT_PREVIEW_HEIGHT);

        if (mUSBMonitor != null) {
            mUSBMonitor.register();
        }

        if (cameraUtils.mUVCCamera != null) {
            cameraUtils.mUVCCamera.startPreview();
        }
    }

    //打开夜视摄像头
    public void openNightCamera() {
//        synchronized (mSync) {
//            if (cameraUtils.mUVCCamera == null) {
//                cameraUtils.showDialog(UnityPlayer.currentActivity);
//            }
//        }

        UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                releaseCamera();
                UsbPointCommand.getInstance().initUart();
                UsbPointCommand.getInstance().initView();
                UsbPointCommand.getInstance().initUSB();
                UsbPointCommand.getInstance().initData();
                try {
                    if (cameraUtils.mUVCCamera == null) {
                        if (mUSBMonitor != null) {
                            Log.d(TAG,"mUSBMonitor   = =  不是空的，找到的设备有"+mUSBMonitor.getDeviceList().size()+"个");
                            for (int i = 0; i < mUSBMonitor.getDeviceList().size(); i++) {
                                if ("NANO".equals(mUSBMonitor.getDeviceList().get(i).getProductName()) /*mUSBMonitor.getDeviceList().get(i).getProductName().contains("NANO")*/) {
                                    cameraUtils.getUSBMonitor().requestPermission(mUSBMonitor.getDeviceList().get(i));
                                    onDialogResult(false);
                                }
                            }
                        } else {
                            Log.d(TAG,"mUSBMonitor   = =  是空的");
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG,"cameraUtils.mUVCCamera   = =  是空的");
                }
            }
        });
    }


    //关闭夜视的方法
    public void closeNightCamera() {
        synchronized (mSync) {
            if (cameraUtils != null) {
                if (cameraUtils.mUVCCamera != null) {
                    cameraUtils.mUVCCamera.stopPreview();
                }
                if (mUSBMonitor != null) {
                    mUSBMonitor.unregister();
                }

                releaseCamera();

                if (mToast != null) {
                    mToast.cancel();
                    mToast = null;
                }
                //先关闭USB通信设备
//                UsbPointCommand.getInstance().DestoryActivity();
                //发送指令关闭夜视
                UsbPointCommand.getInstance().nightVisionClose();

//                if (mUSBMonitor != null) {
//                    Log.w(TAG, "----------onDestroy：mUSBMonitor");
//                    mUSBMonitor.destroy();
//                    mUSBMonitor = null;
//                }

            }
        }
    }


    public void setHandler() {
        if (mHandler==null){
            mHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {

                }
            };
        }
    }

    public final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {

        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            releaseCamera();
            cameraUtils.mUsbControlBlock = ctrlBlock;
            cameraUtils.initSerialPort(device, mHandler);
            //connectedTag = true;
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    final UVCCameraM3 camera = new UVCCameraM3();
                    camera.open(ctrlBlock);
                    camera.setStatusCallback(new IStatusCallback() {
                        @Override
                        public void onStatus(final int statusClass, final int event, final int selector,
                                             final int statusAttribute, final ByteBuffer data) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final Toast toast = Toast.makeText(UnityPlayer.currentActivity,
                                            "onStatus(statusClass=" + statusClass
                                                    + "; " +
                                                    "event=" + event + "; " +
                                                    "selector=" + selector + "; " +
                                                    "statusAttribute=" + statusAttribute + "; " +
                                                    "data=...)", Toast.LENGTH_SHORT);
                                    synchronized (mSync) {
                                        if (mToast != null) {
                                            mToast.cancel();
                                        }
                                        toast.show();
                                        mToast = toast;
                                    }
                                }
                            });
                        }
                    });
                    camera.setButtonCallback(new IButtonCallback() {
                        @Override
                        public void onButton(final int button, final int state) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final Toast toast = Toast.makeText(UnityPlayer.currentActivity,
                                            "onButton(button=" + button + "; " + "state=" + state + ")",
                                            Toast.LENGTH_SHORT);
                                    synchronized (mSync) {
                                        if (mToast != null) {
                                            mToast.cancel();
                                        }
                                        mToast = toast;
                                        toast.show();
                                    }
                                }
                            });
                        }
                    });

                    if (mPreviewSurface != null) {
                        mPreviewSurface.release();
                        mPreviewSurface = null;
                    }

                    String s = camera.getSupportedSize();
                    String width = s.substring(53, 56);
                    String height = s.substring(57, 60);
                    originalWidth = Integer.parseInt(width);
                    originalHeight = Integer.parseInt(height);
                    //cameraUtils.bTemp = deviceType[2] != 0;
                    UVCCameraM3.DEFAULT_PREVIEW_WIDTH = CameraUtils.DEFAULT_PREVIEW_WIDTH = DataUtils.DEFAULT_PREVIEW_WIDTH = originalWidth;
                    UVCCameraM3.DEFAULT_PREVIEW_HEIGHT = CameraUtils.DEFAULT_PREVIEW_HEIGHT = DataUtils.DEFAULT_PREVIEW_HEIGHT = originalHeight;
                    initPreviewParam();     //初始化面阵相关变量

//                    Log.e(TAG, "----------camera.originalWidth(): " + originalWidth);
//                    Log.e(TAG, "----------camera.originalHeight(): " + originalHeight);
//                    Log.e(TAG, "----------ScreenHeight(): " + ScreenHeight);

                    ratio = ScreenHeight / (originalHeight * 1.0f);
                    recordByte = new byte[originalWidth * originalHeight * 3 / 2];
                    shotByteY = new byte[originalWidth * originalHeight];
                    SCALE_MIN = ScreenHeight / (originalHeight * 1.0f);//最小缩放比例
                    UVCCameraM3.DEFAULT_PREVIEW_WIDTH = originalWidth;
                    UVCCameraM3.DEFAULT_PREVIEW_HEIGHT = originalHeight;
                    irgData.setIrWidth((short) originalWidth);
                    irgData.setIrHeight((short) originalHeight);
                    drawHeight = ScreenHeight;
                    drawWidth = ScreenHeight * originalWidth / originalHeight;
                    bitmap = Bitmap.createBitmap(Bitmap.createBitmap(originalWidth, originalHeight, Bitmap.Config.ARGB_8888));
                    widthOffset = (surfaceWidth - drawWidth) / 2;  //设置surfaceview与成像区域的左侧偏移量

                    try {
                        camera.setPreviewSize(originalWidth, UVCCameraM3.DEFAULT_PREVIEW_HEIGHT, UVCCameraM3.FRAME_FORMAT_MJPEG);
                    } catch (final IllegalArgumentException e) {
                        // fallback to YUV mode
                        try {
                            camera.setPreviewSize(originalWidth, UVCCameraM3.DEFAULT_PREVIEW_HEIGHT, UVCCameraM3.DEFAULT_PREVIEW_MODE);
                        } catch (final IllegalArgumentException e1) {
                            camera.destroy();
                            return;
                        }
                    }
                    //palette = getFileFromAssets("Y_Out.dat");
//                    final SurfaceTexture st = mUVCCameraView.getSurfaceTexture();
                    SurfaceTexture surfaceTexture = new SurfaceTexture(MAGIC_TEXTURE_ID);
                    mPreviewSurface = new Surface(surfaceTexture);
                    camera.setPreviewDisplay(mPreviewSurface);
                    camera.setFrameCallback(mIFrameCallback, UVCCameraM3.PIXEL_FORMAT_YUV/*UVCCamera.PIXEL_FORMAT_NV21*/);//
                    camera.startPreview();

                    synchronized (mSync) {
                        cameraUtils.mUVCCamera = camera;
                    }

                    cameraUtils.initModel(cameraUtils.mUVCCamera, mHandler);
                }
            }, 0);
        }

        private void queueEvent(Runnable task, long i) {
            if (task != null && mWorkerHandler != null) {
                try {
                    mWorkerHandler.removeCallbacks(task);
                    if (i > 0L) {
                        mWorkerHandler.postDelayed(task, i);
                    } else if (mWorkerThreadID == Thread.currentThread().getId()) {
                        task.run();
                    } else {
                        mWorkerHandler.post(task);
                    }
                } catch (Exception var5) {
                }

            }
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            releaseCamera();

        }

        @Override
        public void onDettach(final UsbDevice device) {
        }

        @Override
        public void onCancel(final UsbDevice device) {
        }
    };


    public synchronized void releaseCamera() {
        synchronized (mSync) {
            if (cameraUtils.receiveTask_Nano != null) {
                cameraUtils.receiveTask_Nano.Stop();
            }

            if (cameraUtils.usbRequest != null) {
                cameraUtils.usbRequest.close();
                cameraUtils.usbRequest = null;
            }
            if (cameraUtils.mUVCCamera != null) {
                try {
                    cameraUtils.mUVCCamera.setStatusCallback(null);
                    cameraUtils.mUVCCamera.setButtonCallback(null);
                    cameraUtils.mUVCCamera.close();
                    cameraUtils.mUVCCamera.destroy();
                } catch (final Exception e) {
                    //
                }
                cameraUtils.mUVCCamera = null;
            }
            if (mPreviewSurface != null) {
                mPreviewSurface.release();
                mPreviewSurface = null;
            }
        }
    }

    public void initPreviewParam() {
        imageByte = new byte[originalWidth * originalHeight * 2];
        bitmap = Bitmap.createBitmap(originalWidth, originalHeight, Bitmap.Config.ARGB_8888);

        frameByte = new byte[originalWidth * originalHeight * 2];
    }

    //设置每一帧回调，第二个参数未格式
    public IFrameCallback mIFrameCallback = new IFrameCallback() {
        @Override
        public void onFrame(final ByteBuffer frame) {
            frame.clear();


            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (bitmap) {
                        frame.get(frameByte, 0, originalWidth * originalHeight * 2);
                        imageByte = frameByte;
                        nightVisionString = base64Byte2String(imageByte);
//                        colors = DataUtils.Yuyv2RgbInt(imageByte, originalHeight, originalWidth);
//                        bitmap = DataUtils.Rgb2Bitmap(colors, originalWidth, originalHeight);
                        frame.clear();
                    }

                }
            }).start();

//            mHandler.post(mUpdateImageTask);
        }
    };

    public String sendNightVisionString(){
        return nightVisionString;
    }

    public Runnable mUpdateImageTask = new Runnable() {
        @Override
        public void run() {
            synchronized (bitmap) {
//                saveTmpBitmap(bitmap);
//                saveNightTxt(bitmaptoString(bitmap, 70));
                UnityPlayer.UnitySendMessage("MainPanel", "ShowNightVisionImage", nightVisionString);
            }
        }
    };

    //bty[]转base64字符串
    public String base64Byte2String(byte[] bytes) {
        return Base64.encodeToString(bytes, 0);
    }

    //压缩并保存到本地路径
    public void saveTmpBitmap(Bitmap bmp) {
//        if (imagePath == null)
//            return;
//        File appDir = new File(imagePath);
//
//        if (!appDir.exists()) {
//            appDir.mkdir();
//        }

//        File file = new File(imagePath, "nightVision.txt");
//        save(file, nightVisionString);


//        File file = new File(appDir, "nightImage.jpg");
//        try {
//            FileOutputStream fos = new FileOutputStream(file);
//            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//            fos.flush();
//            fos.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    //保存字符串的方法
    public void save(File file, String content) {
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(file);
            outStream.write(content.getBytes());
            outStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outStream != null)
                    outStream.close();
            } catch (IOException e) {
            }
        }
    }

    //保存字符串
    public void saveNightTxt(String nightString) {
        try {
            FileOutputStream fos = new FileOutputStream(imagePath + "/nightVision.txt");
            fos.write(nightString.getBytes());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 将Bitmap转换成字符串
    public String bitmaptoString(Bitmap bitmap, int bitmapQuality) {
        String string = null;
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, bitmapQuality, bStream);
        byte[] bytes = bStream.toByteArray();
        string = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
        return string;
    }


    /**
     * 获取assets文件
     *
     * @param filename
     * @return
     */
    public byte[] getFileFromAssets(String filename) {
        try {
            InputStream in = NightVisionActivity.this.getAssets().open(filename);
            int length = in.available();
            byte[] buffer = new byte[length];
            in.read(buffer);
            in.close();
            return buffer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //保留小数点后两位
    public static String format(double value) {

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.toString();
    }

    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean b) {
        if (b) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // FIXME
                }
            }, 0);
        }
    }

    public void runOnUiThread(Runnable task, long duration) {
        if (task != null) {
            mUIHandler.removeCallbacks(task);
            if (duration <= 0L) {
                try {
                    task.run();
                } catch (Exception var5) {
                    Log.w(TAG, var5);
                }
            } else {
                this.mUIHandler.postDelayed(task, duration);
            }

        }
    }
}