package com.iray.infiray_lt_m3_sdk.utils.Util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ScreenCap {
    private static final String SCREENSHOTS_DIR_NAME = "Screenshots";
    private static final String SCREENSHOT_FILE_NAME_TEMPLATE = "%s_%s_%s.jpg";
    private static final String SCREENSHOT_FILE_PATH_TEMPLATE = "%s/%s/%s";
    private Context context;
    /**
     * 存储到sdcard
     *
     * @param bmp
     * @return
     */
    public static File saveToSD(Bitmap bmp, String mFileName, Handler mhandler) {
        //判断sd卡是否存在
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //文件名
            if(mFileName.equals("")) {
                long systemTime = System.currentTimeMillis();
                mFileName = String.format(SCREENSHOT_FILE_NAME_TEMPLATE, FileUtils.DEVICE_PN,FileUtils.DEVICE_SN, String.valueOf(systemTime));
            } else {
                mFileName = mFileName.replaceAll("irg", "jpg");
            }

            File dir = new File(Environment.getExternalStorageDirectory().toString()+File.separator+"iRayUVC");
            //判断文件是否存在，不存在则创建
            if (!dir.exists()) {
                dir.mkdirs();
            }

            //Log.i("tag", "file path:" + mFilePath);
            File file = new File(dir,mFileName);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.i("tag", "file path:" + file.getAbsolutePath());
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                if (fos != null) {
                    //第一参数是图片格式，第二参数是图片质量，第三参数是输出流
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    Log.d("ScreenCap", "文件已保存");
                  //qiaoxp  mhandler.sendEmptyMessage(MainActivity.TAKE_PHOTO_SUCCESS);
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

            return file;
        }
        return null;
    }


    public static Bitmap activityShot(Activity activity,int imageWidth,int imageHeight){
        /*获取windows中最顶层的view*/
        View view = activity.getWindow().getDecorView();

        //允许当前窗口保存缓存信息
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();

        //获取状态栏高度
        Rect rect = new Rect();
        view.getWindowVisibleDisplayFrame(rect);
        int statusBarHeight = rect.top;

        WindowManager windowManager = activity.getWindowManager();

        //获取屏幕宽和高
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        int width = outMetrics.widthPixels;
        int height = outMetrics.heightPixels;

        int realHeightAbove=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,50,outMetrics);
        int realHeightBelow=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,30,outMetrics);

        //去掉状态栏
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache(), (width-imageWidth)/2, realHeightAbove+statusBarHeight, imageWidth,
                height-realHeightBelow-realHeightAbove-statusBarHeight);

        //销毁缓存信息
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(false);

        return bitmap;

    }
}
