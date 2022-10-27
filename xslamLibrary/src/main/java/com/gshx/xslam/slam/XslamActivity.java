//package com.gshx.xslam.slam;
//
//import android.Manifest;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.os.Message;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.RadioGroup;
//import android.widget.TextView;
//import android.widget.Toast;
//
//
//
//import org.xvisio.xvsdk.DeviceListener;
//import org.xvisio.xvsdk.ImuListener;
//import org.xvisio.xvsdk.PoseListener;
//import org.xvisio.xvsdk.RgbListener;
//import org.xvisio.xvsdk.SgbmListener;
//import org.xvisio.xvsdk.StereoListener;
//import org.xvisio.xvsdk.StreamData;
//import org.xvisio.xvsdk.TofIrListener;
//import org.xvisio.xvsdk.TofListener;
//import org.xvisio.xvsdk.XCamera;
//
//import java.util.List;
//
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//
//public class XslamActivity extends AppCompatActivity {
//    private static final String TAG = "XVSDK Demo";
//
//    private static final int PERMISSIONS_REQUEST_CAMERA = 0;
//    private boolean mPermissionsGranted = false;
//
//    private Context mAppContext = null;
//    private XCamera mCamera = null;
//
//    private boolean isMixedMode = true;
//
//    int rgbSolution = 1;
//    int cameraSelect = 0;
//    int modeSelect = 0;
//
//    Button mBtRgbSolution;
//    ImageView mIvStream, img_bitmapRgb;
//    TextView mTvSolution, mTvFps;
//    private StreamHandler mMainHandler;
//    private boolean runClassifier = false;
//
//    private Handler handler = new Handler();
//
//    private NcnnMobileDetection ncnnDet = new NcnnMobileDetection();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        boolean ret_init = ncnnDet.Init(getAssets());
//        if (!ret_init) {
//            Log.e("MainActivity", "yolov5ncnn Init failed");
//        }
//
//        mAppContext = getApplicationContext();
//        mMainHandler = new StreamHandler(Looper.getMainLooper());
//        setContentView(R.layout.activity_xslam);
//
//        mBtRgbSolution = findViewById(R.id.button_rgb);
//        mBtRgbSolution.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showRgbSolutionDialog();
//            }
//        });
//
////        RadioGroup radioSlam = findViewById(R.id.radio_slam_mode);
////        modeSelect = radioSlam.getCheckedRadioButtonId();
////        radioSlam.setOnCheckedChangeListener(mSlamModeListener);
//
//
////        CheckBox checkBoxSlam = findViewById(R.id.checkbox_slam);
////        checkBoxSlam.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
////            @Override
////            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
////                if (b) {
////                    mCamera.startStream(XCamera.Stream.SLAM);
////                } else {
////                    mCamera.stopStreams();
////
////                }
////            }
////        });
//
////        CheckBox checkBoxImu = findViewById(R.id.checkbox_imu);
////        checkBoxImu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
////            @Override
////            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
////                if (b) {
////                    mCamera.startStream(XCamera.Stream.IMU);
////                } else {
////                    mCamera.stopStream(XCamera.Stream.IMU);
////                }
////            }
////        });
//
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
//        mTvSolution = findViewById(R.id.tv_rgb_solution);
//        mTvFps = findViewById(R.id.tv_rgb_fps);
//        RadioGroup cameraRadio = findViewById(R.id.radio_camera);
//        cameraSelect = cameraRadio.getCheckedRadioButtonId();
//        cameraRadio.setOnCheckedChangeListener(mCemeraSelectListener);
//
//        //uvc permission https://github.com/saki4510t/UVCPermissionTest
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
//                            Toast.makeText(mAppContext, "被永久拒绝授权，请手动授予存储权限", (int) 1000).show();
//                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
//                            XXPermissions.startPermissionActivity(mAppContext, permissions);
//                        } else {
//                            Toast.makeText(mAppContext, "获取存储权限失败", (int) 1000).show();
//                        }
//                    }
//                });
//
//        mPermissionsGranted = true;
//    }
//
//    private final Object lock = new Object();
//    // 预测图片线程
//    private Runnable periodicClassify =
//            new Runnable() {
//                @Override
//                public void run() {
//                    synchronized (lock) {
//                        if (runClassifier) {
//                            // 开始预测前要判断相机是否已经准备好
//                            if (mCamera != null) {
//                                try {
//                                    Log.d("periodicClassify", "可以走到这里");
//                                    if (bitmapRgb != null) {
//                                        NcnnMobileDetection.Obj[] objects = ncnnDet.Detect(bitmapRgb, true);
//
//                                        runOnUiThread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                try {
//                                                    showObjects(objects, bitmapRgb);
//                                                } catch (Exception e) {
//                                                    e.printStackTrace();
//                                                }
//                                            }
//                                        });
//                                    }
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                    }
//                    if (handler != null) {
//                        handler.postDelayed(periodicClassify, 0);
//                    }
//                }
//            };
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
//            return;
//        }
//        mPermissionsGranted = true;
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
////        if (mPermissionsGranted) {
////            init();
////            mCamera.setSlamMode(isMixedMode ? 0 : 2);
////            //打开slam摄像头以及rgb
////            mCamera.startStream(XCamera.Stream.SLAM);
////            mCamera.startStream(XCamera.Stream.RGB);
////        } else {
////            Log.e(TAG, "missing permissions");
////        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (mCamera != null) {
//            mCamera.stopStreams();
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//
//        if (mCamera != null) {
//            mCamera.stopStreams();
//        }
//        if (handler != null) {
//            handler.removeCallbacks(periodicClassify);
//        }
//    }
//
//    private void init() {
//        if (mCamera == null) {
//            mCamera = new XCamera();
//            mCamera.init(mAppContext);
//            mCamera.setDevicesChangedCallback(mListener);
////            mCamera.setImuCallback(mImuListener);
////            mCamera.setStereoCallback(mStereoListener);
//            mCamera.setRgbCallback(mRgbListener);
////            mCamera.setTofCallback(mTofListener);
////            mCamera.setTofIrCallback(mTofIrListener);
////            mCamera.setSgbmCallback(mSgbmListener);
////            mCamera.setPoseCallback(mPoseListener);//设置slam模式
//
////            mCamera.setSlamMode(isMixedMode ? 0 : 2);
////            //打开slam摄像头以及rgb
////            mCamera.startStream(XCamera.Stream.SLAM);
////            mCamera.startStream(XCamera.Stream.RGB);
////            synchronized (lock) {
////                runClassifier = true;
////            }
////            handler.post(periodicClassify);
//
//        }
//        mCamera.setRgbSolution(rgbSolution);
//    }
//
////    RadioGroup.OnCheckedChangeListener mSlamModeListener = new RadioGroup.OnCheckedChangeListener() {
////        @Override
////        public void onCheckedChanged(RadioGroup radioGroup, int i) {
////            if (modeSelect == i) {
////                return;
////            }
////            modeSelect = i;
////
////            switch (i) {
////                case R.id.radio_mixed:
////                    isMixedMode = true;
////                    break;
////
////                case R.id.radio_edge:
////                    isMixedMode = false;
////                    break;
////
////                default:
////                    break;
////            }
////
////            mCamera.setSlamMode(isMixedMode ? 0 : 2);
////        }
////    };
//
//    RadioGroup.OnCheckedChangeListener mCemeraSelectListener = new RadioGroup.OnCheckedChangeListener() {
//        @Override
//        public void onCheckedChanged(RadioGroup radioGroup, int i) {
//            if (cameraSelect == i) {
//                return;
//            }
//            cameraSelect = i;
//            mCamera.stopStream(XCamera.Stream.RGB);
//            mCamera.stopStream(XCamera.Stream.TOF);
//            mCamera.stopStream(XCamera.Stream.STEREO);
//            mCamera.stopStream(XCamera.Stream.SGBM);
//
//            switch (i) {
//                case R.id.radio_rgb:
//                    mCamera.startStream(XCamera.Stream.RGB);
//                    break;
//
//                case R.id.radio_tof:
//                    mCamera.startStream(XCamera.Stream.TOF);
//                    break;
//
//                case R.id.radio_stereo:
//                    mCamera.startStream(XCamera.Stream.STEREO);
//                    break;
//
//                case R.id.radio_sgbm:
//                    mCamera.startStream(XCamera.Stream.SGBM);
//                    break;
//
//                default:
//                    break;
//            }
//        }
//    };
//
//    public class StreamHandler extends Handler {
//
//        public static final int RGB = 1;
//        public static final int TOF = 2;
//        public static final int STEREO = 3;
//        public static final int SGBM = 4;
//
//        public StreamHandler(Looper looper) {
//            super(looper);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            Log.e(TAG, "handleMessage " + msg.what);
//            switch (msg.what) {
//                case RGB:
//                    mBtRgbSolution.setVisibility(View.VISIBLE);
//                    onRgbCallback((StreamData) msg.obj);
//                    Log.d("bitmapRgb.setPixels====", "RGB");
//                    break;
//
//                case TOF:
//                    mBtRgbSolution.setVisibility(View.GONE);
//                    onTofCallback((StreamData) msg.obj);
//                    break;
//
//                case STEREO:
//                    mBtRgbSolution.setVisibility(View.GONE);
//                    onStereoCallback((StreamData) msg.obj);
//                    break;
//
//                case SGBM:
//                    mBtRgbSolution.setVisibility(View.GONE);
//                    onSgbmCallback((StreamData) msg.obj);
//                    break;
//
//                default:
//                    break;
//            }
//        }
//    }
//
//    public void showRgbSolutionDialog() {
//        String[] rgbSolutionItems = {"1920x1080", "1280x720", "640x480"};
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setSingleChoiceItems(rgbSolutionItems, rgbSolution, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                rgbSolution = which;
//                if (mCamera != null) {
//                    mCamera.setRgbSolution(which);
//                }
//                dialog.dismiss();
//            }
//        });
//        builder.show();
//    }
//
//    private final DeviceListener mListener = new DeviceListener() {
//        @Override
//        public void onDeviceAttach() {
//        }
//
//        @Override
//        public void onDeviceDetach() {
//        }
//    };
//
//    private final PoseListener mPoseListener = new PoseListener() {
//        @Override
//        public void onPose(final double x, final double y, final double z, final double roll, final double pitch, final double yaw) {
////            mMainHandler.post(new Runnable() {
////                @Override
////                public void run() {
////                    PoseDisplay pose_display = findViewById(R.id.poseDisplay);
////                    pose_display.setValue(x, y, z, roll, pitch, yaw);
////                }
////            });
//
//        }
//    };
//
//    private final ImuListener mImuListener = new ImuListener() {
//        @Override
//        public void onImu(final double x, final double y, final double z) {
////            mMainHandler.post(new Runnable() {
////                @Override
////                public void run() {
////                    AccelerometerDisplay a = findViewById(R.id.accelerometerDisplay);
////                    a.setValues(x, y, z);
////                }
////            });
//        }
//    };
//
//    private void onStereoCallback(StreamData data) {
////        mTvSolution.setText(data.getWidth() + "X" + data.getHeight());
////        mTvFps.setText("");
////        Bitmap bitmap = Bitmap.createBitmap(data.getWidth(), data.getHeight(), Bitmap.Config.ARGB_8888);
////        bitmap.setPixels(data.getPixels(), 0, data.getWidth(), 0, 0, data.getWidth(), data.getHeight());
////        mIvStream.setImageBitmap(bitmap);
//    }
//
//    private final StereoListener mStereoListener = new StereoListener() {
//        @Override
//        public void onStereo(final int width, final int height, final int[] pixels) {
////            sendStreamMessage(StreamHandler.STEREO, new StreamData(width, height, pixels));
//
//        }
//    };
//
//    private void sendStreamMessage(int what, StreamData data) {
//        mMainHandler.obtainMessage(what, data).sendToTarget();
//    }
//
//
//    private Bitmap bitmapRgb = null;
//
//    public byte[] byteArray2RgbArray(int[] data) {
//        byte[] byteArr = new byte[data.length * 4];
//        for (int i = 0; i < data.length; i++) {
//            byteArr[i * 4 + 0] = (byte) ((data[i] >> 24) & 0xff);
//            byteArr[i * 4 + 1] = (byte) ((data[i] >> 16) & 0xff);
//            byteArr[i * 4 + 2] = (byte) ((data[i] >> 8) & 0xff);
//            byteArr[i * 4 + 3] = (byte) (data[i] & 0xff);
//        }
//        return byteArr;
//    }
//
//    private void onRgbCallback(StreamData data) {
////        mTvSolution.setText(data.getWidth() + "X" + data.getHeight());
//        bitmapRgb = Bitmap.createBitmap(data.getPixels(), data.getWidth(), data.getHeight(), Bitmap.Config.ARGB_8888);
////        bitmapRgb.setPixels(data.getPixels(), 0, data.getWidth(), 0, 0, data.getWidth(), data.getHeight());
//
////        if (data.getHeight() != 480) {
//        mIvStream.setImageBitmap(bitmapRgb);
////        }
//
////        float scale = data.getHeight() == 720 ? 0.75f : 0.5f;
////        scaled = Bitmap.createScaledBitmap(bitmapRgb, (int) (data.getWidth() * scale), (int) (data.getHeight() * scale), false);
////        Log.d("bitmapRgb.setPixels====scaled======", scaled.toString());
////
//
////        NcnnMobileDetection.Obj[] objects = ncnnDet.Detect(bitmapRgb, true);
////        showObjects(objects,bitmapRgb);
//
////        bitmapRgb.recycle();
////        mIvStream.setImageBitmap(scaled);
////        Log.d("bitmapRgb.setPixels====scaled2======", scaled.toString());
//
//
//    }
//
//    private final RgbListener mRgbListener = new RgbListener() {
//        @Override
//        public void onFps(final int fps) {
////            mMainHandler.post(new Runnable() {
////                @Override
////                public void run() {
////                    mTvFps.setVisibility(View.VISIBLE);
////                    mTvFps.setText("FPS:  " + fps);
////                }
////            });
//        }
//
//        @Override
//        public void onRgb(final int width, final int height, final int[] pixels) {
//            sendStreamMessage(StreamHandler.RGB, new StreamData(width, height, pixels));
//        }
//    };
//
//    private void onSgbmCallback(StreamData data) {
////        mTvSolution.setText(data.getWidth() + "X" + data.getHeight());
////        mTvFps.setText("");
////        Bitmap bitmap = Bitmap.createBitmap(data.getWidth(), data.getHeight(), Bitmap.Config.ARGB_8888);
////        bitmap.setPixels(data.getPixels(), 0, data.getWidth(), 0, 0, data.getWidth(), data.getHeight());
////        mIvStream.setImageBitmap(bitmap);
//    }
//
//    private final SgbmListener mSgbmListener = new SgbmListener() {
//        @Override
//        public void onSgbm(int width, int height, int[] pixels) {
////            sendStreamMessage(StreamHandler.SGBM, new StreamData(width, height, pixels));
//        }
//    };
//
//    private void onTofCallback(StreamData data) {
////        mTvSolution.setText(data.getWidth() + "X" + data.getHeight());
////        mTvFps.setText("");
////        Bitmap bitmap = Bitmap.createBitmap(data.getWidth(), data.getHeight(), Bitmap.Config.ARGB_8888);
////        bitmap.setPixels(data.getPixels(), 0, data.getWidth(), 0, 0, data.getWidth(), data.getHeight());
////        mIvStream.setImageBitmap(bitmap);
//    }
//
//    private final TofListener mTofListener = new TofListener() {
//        @Override
//        public void onTof(int width, int height, int[] pixels) {
////            sendStreamMessage(StreamHandler.TOF, new StreamData(width, height, pixels));
//        }
//    };
//
//    private final TofIrListener mTofIrListener = new TofIrListener() {
//        @Override
//        public void onTofIr(int width, int height, int[] pixels) {
//
//        }
//    };
//    private Bitmap rgbaBitmap = null;
//    private Canvas canvas;
//    private Paint paint;
//
//    private void showObjects(NcnnMobileDetection.Obj[] objects, Bitmap bitmap) {
//        if (objects == null) {
//            img_bitmapRgb.setImageBitmap(bitmap);
//            Log.d("periodicClassify", "可以走到这里---objects == null");
//            return;
//        }
//        // draw objects on bitmap
//        rgbaBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
////        rgbaBitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
//
//        final int[] colors = new int[]{
//                Color.rgb(54, 67, 244),
////                Color.rgb(99, 30, 233),
////                Color.rgb(176, 39, 156),
////                Color.rgb(183, 58, 103),
////                Color.rgb(181, 81, 63),
////                Color.rgb(243, 150, 33),
////                Color.rgb(244, 169, 3),
////                Color.rgb(212, 188, 0),
////                Color.rgb(136, 150, 0),
////                Color.rgb(80, 175, 76),
////                Color.rgb(74, 195, 139),
////                Color.rgb(57, 220, 205),
////                Color.rgb(59, 235, 255),
////                Color.rgb(7, 193, 255),
////                Color.rgb(0, 152, 255),
////                Color.rgb(34, 87, 255),
////                Color.rgb(72, 85, 121),
////                Color.rgb(158, 158, 158),
////                Color.rgb(139, 125, 96)
//        };
//
//        canvas = new Canvas(rgbaBitmap);
//
//        paint = new Paint();
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
//        for (int i = 0; i < objects.length; i++) {
//            paint.setColor(colors[0]);
////            float left, x轴距离左边的距离
////            float top, y轴距离上边的距离
////            float right, left加上物体的长度  就是矩形的终点
////            float bottom, top加上物体的宽度   就是矩形宽度的终点
////            android.graphics.Paint paint  实例化的画笔及样式
//            canvas.drawRect(objects[i].x, objects[i].y, objects[i].x + objects[i].w, objects[i].y + objects[i].h, paint);
//
//            // draw filled text inside image
//            {
//                String text = objects[i].label + " = " + String.format("%.1f", objects[i].prob * 100) + "%";
//
//                float text_width = textpaint.measureText(text);
//                float text_height = -textpaint.ascent() + textpaint.descent();
//
//                float x = objects[i].x;
//                float y = objects[i].y - text_height;
//                if (y < 0)
//                    y = 0;
//                if (x + text_width > rgbaBitmap.getWidth())
//                    x = rgbaBitmap.getWidth() - text_width;
//
//                canvas.drawRect(x, y, x + text_width, y + text_height, textbgpaint);
//
//                canvas.drawText(text, x, y - textpaint.ascent(), textpaint);
//            }
//        }
//
//        img_bitmapRgb.setImageBitmap(rgbaBitmap);
//    }
//}
