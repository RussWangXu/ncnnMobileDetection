//package com.example.ncnnmobiledetection;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.ImageFormat;
//import android.graphics.Paint;
//import android.graphics.Rect;
//import android.graphics.SurfaceTexture;
//import android.graphics.YuvImage;
//import android.hardware.usb.UsbDevice;
//import android.os.Bundle;
//import android.os.Environment;
//import android.os.Looper;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.Surface;
//import android.view.TextureView;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.Toast;
//
//import com.example.ncnnmobiledetection.bean.DeviceInfo;
//import com.jiangdg.usbcamera.UVCCameraHelper;
//import com.jiangdg.usbcamera.utils.FileUtils;
//import com.serenegiant.usb.CameraDialog;
//import com.serenegiant.usb.USBMonitor;
//import com.serenegiant.usb.common.AbstractUVCCameraHandler;
//import com.serenegiant.usb.widget.CameraViewInterface;
//import com.serenegiant.usb.widget.UVCCameraTextureView;
//import com.unity3d.player.UnityPlayer;
//import com.unity3d.player.UnityPlayerActivity;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class UsbCameraActivity extends UnityPlayerActivity implements CameraDialog.CameraDialogParent, CameraViewInterface.Callback {
//
//    public UVCCameraHelper mCameraHelper;
//    public CameraViewInterface mUVCCameraView;
//    public UVCCameraTextureView mTextureView;
//
////    private View mTextureView;
//
//    public boolean isRequest;
//    public boolean isPreview;
//    public ImageView iv_image;
//    public String imgContent;
//    public NcnnMobileDetection ncnnDet = new NcnnMobileDetection();
//    public static UsbCameraActivity instance = getInstance();  //单例类
//    private CameraHolder cameraHolder;
//
//    public static UsbCameraActivity getInstance() {
//        if (instance == null) {
//            instance = new UsbCameraActivity();
//        }
//        return instance;
//    }
//
//
//    public List<DeviceInfo> getUSBDevInfo() {
//        if (mCameraHelper == null)
//            return null;
//        List<DeviceInfo> devInfos = new ArrayList<>();
//        List<UsbDevice> list = mCameraHelper.getUsbDeviceList();
//        for (UsbDevice dev : list) {
//            DeviceInfo info = new DeviceInfo();
//            info.setPID(dev.getVendorId());
//            info.setVID(dev.getProductId());
//            devInfos.add(info);
//        }
//        return devInfos;
//    }
//
//    public void popCheckDevDialog() {
//        List<DeviceInfo> infoList = getUSBDevInfo();
//        if (infoList == null || infoList.isEmpty()) {
//            Log.d("UsbCameraActivity", "Find devices failed");
//            return;
//        }
//        final List<String> dataList = new ArrayList<>();
//        for (DeviceInfo deviceInfo : infoList) {
//            dataList.add("Device：PID_" + deviceInfo.getPID() + " & " + "VID_" + deviceInfo.getVID());
//        }
//
//        for (int i = 0; i < dataList.size(); i++) {
//            if (infoList.get(i).getPID() == 7532) {
//                mCameraHelper.requestPermission(i);
//            }
//        }
////        if (infoList.get(0).getPID() == 7532) {
////            mCameraHelper.requestPermission(0);
////        } else {
////            mCameraHelper.requestPermission(1);
////        }
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_usbcamera);
//
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        initRegisterUSB();
//    }
//
//
//    //注册usb
//    public void initRegisterUSB() {
//        UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (mCameraHelper != null) {
//                    mCameraHelper.registerUSB();
//                }
//            }
//        });
//
//    }
//
//    //不使用的时候注销usb
//    public void unRegisterUSB() {
//        if (mCameraHelper != null) {
//            mCameraHelper.unregisterUSB();
//        }
//    }
//
//    //销毁时调用
//    public void onDestroyCamera() {
//        FileUtils.releaseFile();
//        if (mCameraHelper != null) {
//            mCameraHelper.release();
//        }
//    }
//
//    public void initCamera() {
//
//        if (cameraHolder == null) {
//            cameraHolder = new CameraHolder();
//            cameraHolder.openCamera();
//        }
//
//
////        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
////        layoutParams.width = 300;
////        layoutParams.height = 300;
////        Button button = new Button(UnityPlayer.currentActivity);
////        button.setLayoutParams(layoutParams);
////        button.setText("新加的按钮");
////        button.setBackgroundColor(Color.GREEN);
////        ViewGroup totalViewAd = (ViewGroup) UnityPlayer.currentActivity.getWindow().getDecorView();
////        totalViewAd.addView(button);
////        button.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                Toast.makeText(UnityPlayer.currentActivity, "来了来了", Toast.LENGTH_LONG).show();
////            }
////        });
//
//    }
//
//    public void OpenTextureView() {
//        UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Log.d("Unity", "initGLTexture");
//
//
//                LayoutInflater inflater = LayoutInflater.from(UnityPlayer.currentActivity);
//                View view = inflater.inflate(R.layout.activity_usbcamera, null);
//                TextureView textureView = view.findViewById(R.id.camera_view);
//
//                GLTextureOES mTextureOES = new GLTextureOES(UnityPlayer.currentActivity, 300, 300);
//                SurfaceTexture mSurfaceTexture = new SurfaceTexture(mTextureOES.getTextureID());
//
////                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
////                layoutParams.height = 300;
////                layoutParams.width = 300;
////                textureView.setLayoutParams(layoutParams);
//
//                UnityPlayer.UnitySendMessage("Canvas", "fromAndroidMsg", "1");
////                TextureView textureView = new TextureView(UnityPlayer.currentActivity);
//
//                ViewGroup totalViewAd = (ViewGroup) UnityPlayer.currentActivity.getWindow().getDecorView();
//                totalViewAd.addView(view);
//
//                textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
//                    @Override
//                    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//                        CameraHelper.get().openCamera(UnityPlayer.currentActivity);
//                        CameraHelper.get().startPreview(surface);
//                        UnityPlayer.UnitySendMessage("Canvas", "fromAndroidMsg", "2");
//                    }
//
//                    @Override
//                    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//                        UnityPlayer.UnitySendMessage("Canvas", "fromAndroidMsg", "3");
//                    }
//
//                    @Override
//                    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//                        CameraHelper.get().stopCamera();
//                        return false;
//                    }
//
//                    @Override
//                    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//                        UnityPlayer.UnitySendMessage("Canvas", "fromAndroidMsg", "4");
//                        Toast.makeText(UnityPlayer.currentActivity, "surface", Toast.LENGTH_LONG).show();
//
//                    }
//                });
//
//                mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
//                    @Override
//                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//                        UnityPlayer.UnitySendMessage("Canvas", "fromAndroidMsg", surfaceTexture.toString());
//                    }
//                });
//            }
//        });
//    }
//
//    public void initOnCreate() {
//        Log.d("UsbCameraActivity", "initOnCreate");
//        UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
//
//            @Override
//            public void run() {
////                LayoutInflater inflater = LayoutInflater.from(UnityPlayer.currentActivity);
////                View view = inflater.inflate(R.layout.activity_usbcamera, null);
////                UVCCameraTextureView camera_view = view.findViewById(R.id.camera_view);
////                TextureView camera_view = new TextureView(UnityPlayer.currentActivity);
//                UVCCameraTextureView uvcCameraTextureView = new UVCCameraTextureView(UnityPlayer.currentActivity);
//
//
//                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                layoutParams.width = 640;
//                layoutParams.height = 480;
//                uvcCameraTextureView.setLayoutParams(layoutParams);
//
//                mUVCCameraView = (CameraViewInterface) uvcCameraTextureView;
//                mUVCCameraView.setCallback(UsbCameraActivity.this);
//                mCameraHelper = UVCCameraHelper.getInstance(640, 480);
//                if (mCameraHelper.getUSBMonitor() == null) {
//                    mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_MJPEG);
//                }
////                ViewGroup totalViewAd = (ViewGroup) UnityPlayer.currentActivity.getWindow().getDecorView();
////                totalViewAd.addView(view, layoutParams);
//                mCameraHelper.initUSBMonitor(UnityPlayer.currentActivity, mUVCCameraView, new UVCCameraHelper.OnMyDevConnectListener() {
//                    @Override
//                    public void onAttachDev(UsbDevice device) {
//                        if (!isRequest) {
//                            isRequest = true;
//                            showShortMsg("device = " + device.getDeviceName() + "---" + device.getProductName());
//                            popCheckDevDialog();
//                        } else {
//                            showShortMsg("isRequest 是True");
//                        }
//                    }
//
//                    @Override
//                    public void onDettachDev(UsbDevice device) {
//                        if (isRequest) {
//                            isRequest = false;
//                            mCameraHelper.closeCamera();
//                            showShortMsg(device.getDeviceName() + " is out");
//                        }
//                    }
//
//                    @Override
//                    public void onConnectDev(UsbDevice device, boolean isConnected) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (!isConnected) {
//                                    showShortMsg("fail to connect,please check resolution params");
//                                    isPreview = false;
//                                } else {
//                                    isPreview = true;
//                                    showShortMsg("connecting");
//                                    // initialize seekbar
//                                    // need to wait UVCCamera initialize over
//                                    new Thread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            try {
//                                                Thread.sleep(500);
//                                            } catch (InterruptedException e) {
//                                                e.printStackTrace();
//                                            }
//                                            Looper.prepare();
//                                            Looper.loop();
//                                        }
//                                    }).start();
//                                }
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onDisConnectDev(UsbDevice device) {
//                        showShortMsg("disconnecting");
//                    }
//                });
//
//
//                mUVCCameraView.setCallback(new CameraViewInterface.Callback() {
//                    @Override
//                    public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
//                        if (!isPreview && mCameraHelper.isCameraOpened()) {
//                            mCameraHelper.startPreview(mUVCCameraView);
//                            isPreview = true;
//                        }
//                    }
//
//                    @Override
//                    public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {
//                        Log.d("onSurfaceChanged", width + "---" + height);
//                    }
//
//                    @Override
//                    public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
//                        if (isPreview && mCameraHelper.isCameraOpened()) {
//                            mCameraHelper.stopPreview();
//                            isPreview = false;
//                        }
//                    }
//                });
//                mCameraHelper.setOnPreviewFrameListener(new AbstractUVCCameraHandler.OnPreViewResultListener() {
//                    @Override
//                    public void onPreviewResult(byte[] bytes) {
////                        imgContent = Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
////                        Log.d("onPreviewResult", bytes.toString());
////                        UnityPlayer.UnitySendMessage("Canvas", "fromAndroidMsg", imgContent);
//                        mCameraHelper.release();
//                        YuvImage image = new YuvImage(bytes, ImageFormat.NV21, mCameraHelper.getPreviewWidth(), mCameraHelper.getPreviewHeight(), null);
//                        if (image != null) {
//                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                            image.compressToJpeg(new Rect(0, 0, mCameraHelper.getPreviewWidth(), mCameraHelper.getPreviewHeight()), 70, stream);
//                            Bitmap bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
//                            try {
////                                long start = System.currentTimeMillis();
//                                NcnnMobileDetection.Obj[] objects = ncnnDet.Detect(bitmap, true);
////                                long end = System.currentTimeMillis();
//                                UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        try {
//                                            showObjects(objects, compressImage(bitmap));
//                                            recycle(bitmap);
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                });
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            //纠正图像的旋转角度问题
//
//                        }
//                    }
//                });
//
//
//                if (mCameraHelper != null) {
//                    if (mCameraHelper.getUSBMonitor() != null) {
//                        showShortMsg("数量多少：" + mCameraHelper.getUSBMonitor().getDeviceList().size());
//                    } else {
//                        showShortMsg("mCameraHelper或者getUSBMonitor为空");
//                    }
//                } else {
//                    showShortMsg("mCameraHelper为空");
//                }
//
//            }
//
//        });
//        //初始化参数之后注册usb
//        initRegisterUSB();
//
//    }
//
//
//    public void sendMsg() {
//        UnityPlayer.UnitySendMessage("Canvas", "fromAndroidMsg", "依然特雷西");
//    }
//
//    @Override
//    public USBMonitor getUSBMonitor() {
//        return mCameraHelper.getUSBMonitor();
//    }
//
//    @Override
//    public void onDialogResult(boolean canceled) {
//        if (canceled) {
//            showShortMsg("取消操作");
//        }
//    }
//
//    //CameraViewInterface.Callback
//    @Override
//    public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
//        if (!isPreview && mCameraHelper.isCameraOpened()) {
//            mCameraHelper.startPreview(mUVCCameraView);
//            isPreview = true;
//        }
//    }
//
//    @Override
//    public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {
//        Log.d("onSurfaceChanged", width + "---" + height);
//    }
//
//    @Override
//    public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
//        if (isPreview && mCameraHelper.isCameraOpened()) {
//            mCameraHelper.stopPreview();
//            isPreview = false;
//        }
//    }
//
//    private void showShortMsg(String msg) {
//        Log.d("UsbCameraActivity", msg);
//    }
//
//    // 销毁无用的Bitmap
//    private void recycle(Bitmap bitmap) {
//        if (!bitmap.isRecycled()) {
//            bitmap.recycle();
//        }
//    }
//
//    private void showObjects(NcnnMobileDetection.Obj[] objects, Bitmap bitmap) {
//        if (objects == null) {
////            iv_image.setImageBitmap(bitmap);
//            saveTmpBitmap(bitmap);
//            return;
//        }
//
//        Bitmap rgba = bitmap.copy(Bitmap.Config.RGB_565, true);
////        Bitmap rgba = getTransparentBitmap(bitmap,100);
//
////        Bitmap rgba = Bitmap.createBitmap(mTextureView.getWidth(), mTextureView.getHeight(), Bitmap.Config.ARGB_8888);
////        Bitmap rgba = Bitmap.createBitmap(mTextureView.getWidth(), mTextureView.getHeight(), Bitmap.Config.RGB_565);
//
//
//        final int[] colors = new int[]{
//                Color.rgb(54, 67, 244),
//                Color.rgb(99, 30, 233),
//                Color.rgb(176, 39, 156),
//                Color.rgb(183, 58, 103),
//                Color.rgb(181, 81, 63),
//                Color.rgb(243, 150, 33),
//                Color.rgb(244, 169, 3),
//                Color.rgb(212, 188, 0),
//                Color.rgb(136, 150, 0),
//                Color.rgb(80, 175, 76),
//                Color.rgb(74, 195, 139),
//                Color.rgb(57, 220, 205),
//                Color.rgb(59, 235, 255),
//                Color.rgb(7, 193, 255),
//                Color.rgb(0, 152, 255),
//                Color.rgb(34, 87, 255),
//                Color.rgb(72, 85, 121),
//                Color.rgb(158, 158, 158),
//                Color.rgb(139, 125, 96)
//        };
//
//        Canvas canvas = new Canvas(rgba);
//
//        Paint paint = new Paint();
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(4);
//
//        Paint textbgpaint = new Paint();
//        textbgpaint.setColor(Color.WHITE);
//        textbgpaint.setStyle(Paint.Style.FILL);
//
//        Paint textpaint = new Paint();
//        textpaint.setColor(Color.BLACK);
//        textpaint.setTextSize(26);
//        textpaint.setTextAlign(Paint.Align.LEFT);
//
//
//        List<Object> list = new ArrayList<>();
//        List<String> newList = new ArrayList<>();
//        list.clear();
//        newList.clear();
//        for (int i = 0; i < objects.length; i++) {
//            paint.setColor(colors[i % 19]);
////            paint.setColor( Color.rgb(54, 67, 244));
//
//            if (objects[i].prob > 0.50f) {
//                list.add(objects[i].x);
//                list.add(objects[i].y);
//                list.add(objects[i].w);
//                list.add(objects[i].h);
//                list.add(objects[i].label);
//                list.add(objects[i].prob);
////                Log.d("objects-------------", "x = " + objects[i].x + "  y = " + objects[i].y + "w = " + objects[i].w + "h = " + objects[i].h + "--label名称=" + objects[i].label + "---相似度=" + objects[i].prob);
//                newList.add("[x=" + objects[i].x + ", y=" + objects[i].y + ", w=" + objects[i].w + ", h=" + objects[i].h + ", lable=" + objects[i].label + ", prob=" + objects[i].prob + "]");
//                canvas.drawRect(objects[i].x, objects[i].y, objects[i].x + objects[i].w, objects[i].y + objects[i].h, paint);//左边，上边，右边，下边
//
//
//                String text = objects[i].label + " = " + String.format("%.1f", objects[i].prob * 100) + "%";
//                float text_width = textpaint.measureText(text);
//                float text_height = -textpaint.ascent() + textpaint.descent();
//
//                float x = objects[i].x;
//                float y = objects[i].y - text_height;
//                if (y < 0)
//                    y = 0;
//                if (x + text_width > rgba.getWidth())
//                    x = rgba.getWidth() - text_width;
//
//                canvas.drawRect(x, y, x + text_width, y + text_height, textbgpaint);
//
//                canvas.drawText(text, x, y - textpaint.ascent(), textpaint);
//
//            }
//        }
////        Log.d("objects=====", list.toString());
////        Log.d("objects222=====", newList.toString());
//
////        iv_image.setImageBitmap(rgba);
////        imgContent = bitmaptoString(rgba, 40);
////        Log.d("UsbCameraActivity-------------------", imgContent);
//        saveTmpBitmap(rgba);
//    }
//
//    //提供调用图片字符串
//    public String OnSendImageArray() {
//        if (cameraHolder.imgString != null) {
//            return cameraHolder.imgString;
//        }
//        return cameraHolder.imgString;
//    }
//
//    // 将Bitmap转换成字符串
//    public String bitmaptoString(Bitmap bitmap, int bitmapQuality) {
//        String string = null;
//        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, bitmapQuality, bStream);
//        byte[] bytes = bStream.toByteArray();
//        string = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
//        return string;
//    }
//
//
//    /**
//     * 质量压缩方法
//     *
//     * @param image
//     * @return
//     */
//    public static Bitmap compressImage(Bitmap image) {
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
//        int options = 90;
//
//        while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
//            baos.reset(); // 重置baos即清空baos
//            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
//            options -= 10;// 每次都减少10
//        }
//        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
//        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
//        return bitmap;
//    }
//
//    public static void saveTmpBitmap(Bitmap bmp) {
//        File appDir = new File(Environment.getExternalStorageDirectory().getPath()+"/rgbImage");
//
//        if (!appDir.exists()) {
//            appDir.mkdir();
//        }
//        String fileName = "slamRgb.jpg";
//        File file = new File(appDir, fileName);
//        try {
//            FileOutputStream fos = new FileOutputStream(file);
//            bmp.compress(Bitmap.CompressFormat.JPEG, 70, fos);
//            fos.flush();
//            fos.close();
////            Log.d("jpegName=====","存储成功");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
////            Log.d("jpegName=====","存储失败");
//        } catch (IOException e) {
//            e.printStackTrace();
////            Log.d("jpegName=====","存储失败");
//        }
//    }
//
//}