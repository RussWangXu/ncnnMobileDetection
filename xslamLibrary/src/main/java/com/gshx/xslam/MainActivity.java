package com.gshx.xslam;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.example.ncnnmobiledetection.NcnnMobileDetection;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import org.xvisio.xvsdk.DeviceListener;
import org.xvisio.xvsdk.RgbListener;
import org.xvisio.xvsdk.StreamData;
import org.xvisio.xvsdk.XCamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends UnityPlayerActivity {
//    public static final String TAG = "XVSDK Demo";

    public static final int PERMISSIONS_REQUEST_CAMERA = 0;
    public boolean mPermissionsGranted = false;

    public XCamera mCamera = null;

    public boolean isMixedMode = true;

    public int rgbSolution = 1;

    ImageView mIvStream, img_bitmapRgb;
    private StreamHandler mMainHandler;
    private boolean runClassifier = false;

    public Handler handler = new Handler();

    public NcnnMobileDetection ncnnDet = new NcnnMobileDetection();
    public static MainActivity instance = getInstance();  //单例类

    public static MainActivity getInstance() {
        if (instance == null) {
            instance = new MainActivity();
        }
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


//        setContentView(R.layout.activity_xslam);


//        Button button_open = findViewById(R.id.button_open);
//        button_open.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mCamera.setSlamMode(isMixedMode ? 0 : 2);
//                //打开slam摄像头以及rgb
//                mCamera.startStream(XCamera.Stream.SLAM);
//                mCamera.startStream(XCamera.Stream.RGB);
//                synchronized (lock) {
//                    runClassifier = true;
//                }
//                handler.post(periodicClassify);
//            }
//        });
//
//        mIvStream = findViewById(R.id.rgbView);
//        img_bitmapRgb = findViewById(R.id.img_bitmapRgb);

        //uvc permission https://github.com/saki4510t/UVCPermissionTest
//        XXPermissions.with(this)
//                // 不适配 Android 11 可以这样写
//                .permission(Permission.Group.STORAGE)
//                // 适配 Android 11 需要这样写，这里无需再写 Permission.Group.STORAGE
//                // .permission(Permission.MANAGE_EXTERNAL_STORAGE)
//                .permission(Permission.CAMERA)
//                .request(new OnPermissionCallback() {
//
//                    @Override
//                    public void onGranted(List<String> permissions, boolean all) {
//                        if (all) {
//                            mPermissionsGranted = true;
//                            init();
//                            Log.i("test", "startRecord 0=");
//
////							Toast.makeText(mActivityContext,"获取存储权限成功", (int)1000).show();
//                        }
//                    }
//
//                    @Override
//                    public void onDenied(List<String> permissions, boolean never) {
//                        if (never) {
////                            Toast.makeText(mAppContext, "被永久拒绝授权，请手动授予存储权限", (int) 1000).show();
//                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
//                            XXPermissions.startPermissionActivity(mAppContext, permissions);
//                        } else {
////                            Toast.makeText(mAppContext, "获取存储权限失败", (int) 1000).show();
//                        }
//                    }
//                });
//
//        mPermissionsGranted = true;
    }

    private final Object lock = new Object();
    // 预测图片线程
    public Runnable periodicClassify =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier) {
                            // 开始预测前要判断相机是否已经准备好
                            if (mCamera != null) {
                                try {
                                    Log.d("periodicClassify", "可以走到这里");
                                    if (bitmapRgb != null) {
                                        NcnnMobileDetection.Obj[] objects = ncnnDet.Detect(bitmapRgb, true);

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    showObjects(objects, bitmapRgb);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    if (handler != null) {
                        handler.postDelayed(periodicClassify, 0);
                    }
                }
            };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
            return;
        }
        mPermissionsGranted = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (mPermissionsGranted) {
//            init();
//            mCamera.setSlamMode(isMixedMode ? 0 : 2);
//            //打开slam摄像头以及rgb
//            mCamera.startStream(XCamera.Stream.SLAM);
//            mCamera.startStream(XCamera.Stream.RGB);
//        } else {
//            Log.e(TAG, "missing permissions");
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.stopStreams();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCamera != null) {
            mCamera.stopStreams();
        }
        if (handler != null) {
            handler.removeCallbacks(periodicClassify);
        }
    }

    public void init() {
        boolean ret_init = ncnnDet.Init(getAssets());
        if (!ret_init) {
            Log.e("MainActivity", "yolov5ncnn Init failed");
        }
        mMainHandler = new StreamHandler(Looper.getMainLooper());
        if (mCamera == null) {
            mCamera = new XCamera();
            mCamera.init(UnityPlayer.currentActivity);
            mCamera.setDevicesChangedCallback(mListener);
            mCamera.setRgbCallback(mRgbListener);

//            mCamera.setSlamMode(isMixedMode ? 0 : 2);
//            //打开slam摄像头以及rgb
//            mCamera.startStream(XCamera.Stream.SLAM);
//            mCamera.startStream(XCamera.Stream.RGB);
//            synchronized (lock) {
//                runClassifier = true;
//            }
//            handler.post(periodicClassify);
        }
        mCamera.setRgbSolution(rgbSolution);
    }

    public void openSlamRgb() {
        mCamera.setSlamMode(isMixedMode ? 0 : 2);
        //打开slam摄像头以及rgb
        mCamera.startStream(XCamera.Stream.SLAM);
        mCamera.startStream(XCamera.Stream.RGB);
        synchronized (lock) {
            runClassifier = true;
        }
        handler.post(periodicClassify);
    }


    public class StreamHandler extends Handler {

        public static final int RGB = 1;
        public static final int TOF = 2;
        public static final int STEREO = 3;
        public static final int SGBM = 4;

        public StreamHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
//            Log.e(TAG, "handleMessage " + msg.what);
            switch (msg.what) {
                case RGB:
                    onRgbCallback((StreamData) msg.obj);
                    Log.d("bitmapRgb.setPixels====", "RGB");
                    break;

                default:
                    break;
            }
        }
    }


    private final DeviceListener mListener = new DeviceListener() {
        @Override
        public void onDeviceAttach() {
        }

        @Override
        public void onDeviceDetach() {
        }
    };


    private void sendStreamMessage(int what, StreamData data) {
        mMainHandler.obtainMessage(what, data).sendToTarget();
    }


    private Bitmap bitmapRgb = null;


    private void onRgbCallback(StreamData data) {
        bitmapRgb = Bitmap.createBitmap(data.getPixels(), data.getWidth(), data.getHeight(), Bitmap.Config.ARGB_8888);
//        bitmapRgb.setPixels(data.getPixels(), 0, data.getWidth(), 0, 0, data.getWidth(), data.getHeight());

//        if (data.getHeight() != 480) {
//        mIvStream.setImageBitmap(bitmapRgb);
//        }

//        float scale = data.getHeight() == 720 ? 0.75f : 0.5f;
//        scaled = Bitmap.createScaledBitmap(bitmapRgb, (int) (data.getWidth() * scale), (int) (data.getHeight() * scale), false);
//        Log.d("bitmapRgb.setPixels====scaled======", scaled.toString());
//

//        NcnnMobileDetection.Obj[] objects = ncnnDet.Detect(bitmapRgb, true);
//        showObjects(objects,bitmapRgb);

//        bitmapRgb.recycle();
//        mIvStream.setImageBitmap(scaled);
//        Log.d("bitmapRgb.setPixels====scaled2======", scaled.toString());


    }

    private final RgbListener mRgbListener = new RgbListener() {
        @Override
        public void onFps(final int fps) {

        }

        @Override
        public void onRgb(final int width, final int height, final int[] pixels) {
            sendStreamMessage(StreamHandler.RGB, new StreamData(width, height, pixels));
        }
    };


    private Bitmap rgbaBitmap = null;
    private Canvas canvas;
    private Paint paint;

    private void showObjects(NcnnMobileDetection.Obj[] objects, Bitmap bitmap) {
        if (objects == null) {
//            img_bitmapRgb.setImageBitmap(bitmap);
            saveTmpBitmap(bitmap);
//            Log.d("periodicClassify", "可以走到这里---objects == null");
            return;
        }
        // draw objects on bitmap
        rgbaBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
//        rgbaBitmap = bitmap.copy(Bitmap.Config.RGB_565, true);

        final int[] colors = new int[]{
                Color.rgb(54, 67, 244),
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
        };

        canvas = new Canvas(rgbaBitmap);

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);

        Paint textbgpaint = new Paint();
        textbgpaint.setColor(Color.WHITE);
        textbgpaint.setStyle(Paint.Style.FILL);

        Paint textpaint = new Paint();
        textpaint.setColor(Color.BLACK);
        textpaint.setTextSize(26);
        textpaint.setTextAlign(Paint.Align.LEFT);

        for (int i = 0; i < objects.length; i++) {
            paint.setColor(colors[0]);
//            float left, x轴距离左边的距离
//            float top, y轴距离上边的距离
//            float right, left加上物体的长度  就是矩形的终点
//            float bottom, top加上物体的宽度   就是矩形宽度的终点
//            android.graphics.Paint paint  实例化的画笔及样式
            canvas.drawRect(objects[i].x, objects[i].y, objects[i].x + objects[i].w, objects[i].y + objects[i].h, paint);

            // draw filled text inside image
            {
                String text = objects[i].label + " = " + String.format("%.1f", objects[i].prob * 100) + "%";

                float text_width = textpaint.measureText(text);
                float text_height = -textpaint.ascent() + textpaint.descent();

                float x = objects[i].x;
                float y = objects[i].y - text_height;
                if (y < 0)
                    y = 0;
                if (x + text_width > rgbaBitmap.getWidth())
                    x = rgbaBitmap.getWidth() - text_width;

                canvas.drawRect(x, y, x + text_width, y + text_height, textbgpaint);

                canvas.drawText(text, x, y - textpaint.ascent(), textpaint);
            }
        }

//        img_bitmapRgb.setImageBitmap(rgbaBitmap);
        saveTmpBitmap(rgbaBitmap);
    }

//    public void saveTmpBitmap(Bitmap b) {
//
//        String jpegName = "/storage/emulated/0/Android/data/com.baijiayun/files/slam_rgb.jpg";
//        try {
//            FileOutputStream fout = new FileOutputStream(jpegName);
//            BufferedOutputStream bos = new BufferedOutputStream(fout);
//            b.compress(Bitmap.CompressFormat.JPEG, 70, bos);
//            bos.flush();
//            bos.close();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }

    public static void saveTmpBitmap(Bitmap bmp) {
        File appDir = new File(Environment.getExternalStorageDirectory().getPath()+"/rgbImage");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = "slamRgb.jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 70, fos);
            fos.flush();
            fos.close();
//            Log.d("jpegName=====","存储成功");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
//            Log.d("jpegName=====","存储失败");
        } catch (IOException e) {
            e.printStackTrace();
//            Log.d("jpegName=====","存储失败");
        }
    }
}