package com.example.ncnnmobiledetection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import androidx.annotation.RequiresApi;

public class BitmapToUnity extends UnityPlayerActivity {

    public NcnnMobileDetection ncnnDet = new NcnnMobileDetection();

    public static BitmapToUnity instance = getInstance();  //单例类

    public Handler handler = new Handler();
    public final Object lock = new Object();
    public boolean runClassifier = false;
    public String imagePath;//文件存储路径
    public byte[] bis;
    public Bitmap bitmap;
    public String fileName = "AI_image.jpg";//图片名称


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    public static BitmapToUnity getInstance() {
        if (instance == null) {
            instance = new BitmapToUnity();
        }
        return instance;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    // 预测图片线程
    public Runnable periodicClassify =
            new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier) {
                            // 开始预测
                            UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        //读取文本字符串转byte
                                        bis = Base64.getDecoder().decode(readTxt(imagePath + "/save.txt"));

                                        Matrix matrix = new Matrix();
                                        //同比例缩小
                                        //matrix.setScale(0.5f, 0.5f);
                                        matrix.postRotate(180);//旋转180°
                                        bitmap = BitmapFactory.decodeByteArray(bis, 0, bis.length);
                                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                                        if (bitmap != null) {
                                            //实体检测
                                            NcnnMobileDetection.Obj[] objects = ncnnDet.Detect(bitmap, false);
                                            showObjects(objects, bitmap);
                                            bitmap.recycle();
                                        }

//                                        saveTmpBitmap(bitmap);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });


                        }
                    }
                    if (handler != null) {
                        handler.postDelayed(periodicClassify, 0);
                    }
                }
            };

    //提供unity调用打开检测线程方法
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void initOpenPath(String path) {
        imagePath = path;

        boolean ncnn_init = ncnnDet.Init(UnityPlayer.currentActivity.getAssets());
        if (!ncnn_init) {
            Log.d("unity_ai", "初始化失败");
        }

        synchronized (lock) {
            runClassifier = true;
        }
        handler.post(periodicClassify);

    }

    //关闭释放
    public void closeRunnable() {
        if (handler != null) {
            runClassifier = false;
            handler.removeCallbacks(periodicClassify);
        }
    }


    public static String readTxt(String path) {
        String str = "";
        try {
            File urlFile = new File(path);
            InputStreamReader isr = new InputStreamReader(new FileInputStream(urlFile), "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            String mimeTypeLine = null;
            while ((mimeTypeLine = br.readLine()) != null) {
                str = str + mimeTypeLine;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
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

    public void showObjects(NcnnMobileDetection.Obj[] objects, Bitmap bitmap) {
        if (objects == null) {
            saveTmpBitmap(bitmap);
            return;
        }

        Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);

//        Bitmap rgba = Bitmap.createBitmap(mTextureView.getWidth(), mTextureView.getHeight(), Bitmap.Config.ARGB_8888);


        final int[] colors = new int[]{
                Color.rgb(54, 67, 244),
                Color.rgb(99, 30, 233),
                Color.rgb(176, 39, 156),
                Color.rgb(183, 58, 103),
                Color.rgb(181, 81, 63),
                Color.rgb(243, 150, 33),
                Color.rgb(244, 169, 3),
                Color.rgb(212, 188, 0),
                Color.rgb(136, 150, 0),
                Color.rgb(80, 175, 76),
                Color.rgb(74, 195, 139),
                Color.rgb(57, 220, 205),
                Color.rgb(59, 235, 255),
                Color.rgb(7, 193, 255),
                Color.rgb(0, 152, 255),
                Color.rgb(34, 87, 255),
                Color.rgb(72, 85, 121),
                Color.rgb(158, 158, 158),
                Color.rgb(139, 125, 96)
        };

        Canvas canvas = new Canvas(rgba);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);

        Paint textbgpaint = new Paint();
        textbgpaint.setColor(Color.WHITE);
        textbgpaint.setStyle(Paint.Style.FILL);

        Paint textpaint = new Paint();
        textpaint.setColor(Color.BLACK);
        textpaint.setTextSize(18);
        textpaint.setTextAlign(Paint.Align.LEFT);


        List<Object> list = new ArrayList<>();
        List<String> newList = new ArrayList<>();
        list.clear();
        newList.clear();
        for (int i = 0; i < objects.length; i++) {
            paint.setColor(colors[i % 19]);
//            paint.setColor( Color.rgb(54, 67, 244));

            //只显示大于60%的实体
            if (objects[i].prob > 0.60f) {
                list.add(objects[i].x);
                list.add(objects[i].y);
                list.add(objects[i].w);
                list.add(objects[i].h);
                list.add(objects[i].label);
                list.add(objects[i].prob);
                newList.add("[x=" + objects[i].x + ", y=" + objects[i].y + ", w=" + objects[i].w + ", h=" + objects[i].h + ", lable=" + objects[i].label + ", prob=" + objects[i].prob + "]");
                canvas.drawRect(objects[i].x, objects[i].y, objects[i].x + objects[i].w, objects[i].y + objects[i].h, paint);//左边，上边，右边，下边


                String text = objects[i].label + " = " + String.format("%.1f", objects[i].prob * 100) + "%";
                float text_width = textpaint.measureText(text);
                float text_height = -textpaint.ascent() + textpaint.descent();

                float x = objects[i].x;
                float y = objects[i].y - text_height;
                if (y < 0)
                    y = 0;
                if (x + text_width > rgba.getWidth())
                    x = rgba.getWidth() - text_width;

                canvas.drawRect(x, y, x + text_width, y + text_height, textbgpaint);

                canvas.drawText(text, x, y - textpaint.ascent(), textpaint);

            }
        }

        saveTmpBitmap(rgba);
    }

    //压缩并保存到本地路径
    public void saveTmpBitmap(Bitmap bmp) {

        File appDir = new File(imagePath);

        if (!appDir.exists()) {
            appDir.mkdir();
        }

        File file = new File(appDir, fileName);
        try {
            UnityPlayer.UnitySendMessage("AiActionPanel", "Show", "");
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
