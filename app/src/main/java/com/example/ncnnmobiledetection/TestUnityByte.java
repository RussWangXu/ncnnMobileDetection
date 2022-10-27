package com.example.ncnnmobiledetection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.unity3d.player.UnityPlayerActivity;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

import androidx.annotation.RequiresApi;

public class TestUnityByte extends UnityPlayerActivity {


    private NcnnMobileDetection ncnnDet = new NcnnMobileDetection();
    private Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_test_unity_byte);
        initView();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void initView() {
        boolean ret_init = ncnnDet.Init(getAssets());
        if (!ret_init) {
            Log.d("unity_ai", "初始化失败");
        } else {
            Log.d("unity_ai", "初始化成功");
        }

    }

    //com.example.ncnnmobiledetection    unityDataByte(帧画面)
    public void unityDataByte(String data) {
        runOnUiThread(new Runnable() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                byte[] bis = Base64.getDecoder().decode(data);
                Matrix matrix = new Matrix();
                matrix.setScale(0.5f, 0.5f);
                matrix.postRotate(180); /*翻转180度*/ //后置摄像头是90°  前置摄像头是270°
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                bitmap = BitmapFactory.decodeByteArray(bis, 0, bis.length, options);

                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                if (bitmap != null) {
                    NcnnMobileDetection.Obj[] objects = ncnnDet.Detect(bitmap, false);
                    showObjects(objects);
                    recycle(bitmap);
                }
            }
        });
    }

    public String imgContent;

    private void showObjects(NcnnMobileDetection.Obj[] objects) {
        if (objects == null) {
//            iv_byte_image.setImageBitmap(bitmap);
//            Log.e("byte-------------", objects.length + "=======" + objects.toString());
            return;
        }


        Bitmap rgba = bitmap.copy(Bitmap.Config.RGB_565, true);
//        Bitmap rgba = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

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
        paint.setStrokeWidth(4);

        Paint textbgpaint = new Paint();
        textbgpaint.setColor(Color.WHITE);
        textbgpaint.setStyle(Paint.Style.FILL);

        Paint textpaint = new Paint();
        textpaint.setColor(Color.BLACK);
        textpaint.setTextSize(26);
        textpaint.setTextAlign(Paint.Align.LEFT);

        for (int i = 0; i < objects.length; i++) {
            paint.setColor(colors[i % 19]);
//            float left, x轴距离左边的距离
//            float top, y轴距离上边的距离
//            float right, left加上物体的长度  就是矩形的终点
//            float bottom, top加上物体的宽度   就是矩形宽度的终点
//            android.graphics.Paint paint  实例化的画笔及样式


            // draw filled text inside image
            if (objects[i].prob > 0.70f) {
                canvas.drawRect(objects[i].x, objects[i].y, objects[i].x + objects[i].w, objects[i].y + objects[i].h, paint);


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
//        Toast.makeText(TestUnityByte.this, "图片中识别到了几个物体：" + objects.length, Toast.LENGTH_LONG).show();
//        iv_byte_image.setImageBitmap(rgba);
        imgContent = bitmaptoString(rgba, 40);

    }

    // 销毁无用的Bitmap
    private void recycle(Bitmap bitmap) {
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    public String OnSendImageArray() {
        return imgContent;
    }

    public String bitmaptoString(Bitmap bitmap, int bitmapQuality) {
        // 将Bitmap转换成字符串
        String string = null;
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, bitmapQuality, bStream);
        byte[] bytes = bStream.toByteArray();
        string = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
        return string;
    }


}