/*
 * Developed by Infrared Products Department.
 * Copyright(C) 2020 IRAY, All Rights Reserved.
 *
 * Filename: FileUtils.java
 * Packagename: com.example.m300analysis.Utils.FileUtils
 * Summary:
 *
 * Author: LiXiao
 * Version:
 * Date: 2020年01月02日 09:43:52
 * History:
 *
 * Author: LiXiao
 * Version:
 * Date: 2020年01月02日 09:43:54
 * History:
 */

package com.iray.infiray_lt_m3_sdk.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class FileUtils {
    private static String TAG = "fileUtils";
    public static final String FILE_SAVE_DIR_NAME="iRAY";
    public static final String FILE_SAVE_NAME_TEMPLATE = "iRay%s.jpg";
    public static final String FILE_SAVE_NAME_PDF="iRay%s.pdf";

    public static final String DIR_PATH= Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"iRayUVC";
    private FileUtils() {
    }

//****系统文件目录**********************************************************************************************

    /**
     * @return 程序系统文件目录
     */
    public static String getFileDir(Context context) {
        return String.valueOf(context.getFilesDir());
    }

    /**
     * @param context    上下文
     * @param customPath 自定义路径
     * @return 程序系统文件目录绝对路径
     */
    public static String getFileDir(Context context, String customPath) {
        String path = context.getFilesDir() + formatPath(customPath);
        mkdir(path);
        return path;
    }

//****系统缓存目录**********************************************************************************************

    /**
     * @return 程序系统缓存目录
     */
    public static String getCacheDir(Context context) {
        return String.valueOf(context.getCacheDir());
    }

    /**
     * @param context    上下文
     * @param customPath 自定义路径
     * @return 程序系统缓存目录
     */
    public static String getCacheDir(Context context, String customPath) {
        String path = context.getCacheDir() + formatPath(customPath);
        mkdir(path);
        return path;
    }

//****Sdcard文件目录**********************************************************************************************

    /**
     * @return 内存卡文件目录
     */
    public static String getExternalFileDir(Context context) {
        return String.valueOf(context.getExternalFilesDir(""));
    }

    /**
     * @param context    上下文
     * @param customPath 自定义路径
     * @return 内存卡文件目录
     */
    public static String getExternalFileDir(Context context, String customPath) {
        String path = context.getExternalFilesDir("") + formatPath(customPath);
        mkdir(path);
        return path;
    }

//****Sdcard缓存目录**********************************************************************************************

    /**
     * @return 内存卡缓存目录
     */
    public static String getExternalCacheDir(Context context) {
        return String.valueOf(context.getExternalCacheDir());
    }

    /**
     * @param context    上下文
     * @param customPath 自定义路径
     * @return 内存卡缓存目录
     */
    public static String getExternalCacheDir(Context context, String customPath) {
        String path = context.getExternalCacheDir() + formatPath(customPath);
        mkdir(path);
        return path;
    }

//****公共文件夹**********************************************************************************************

    /**
     * @return 公共下载文件夹
     */
    public static String getPublicDownloadDir() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    }

//****相关工具**********************************************************************************************

    /**
     * 创建文件夹
     *
     * @param DirPath 文件夹路径
     */
    public static void mkdir(String DirPath) {
        File file = new File(DirPath);
        if (!(file.exists() && file.isDirectory())) {
            file.mkdirs();
        }
    }

    /**
     * 格式化文件路径
     * 示例：  传入 "sloop" "/sloop" "sloop/" "/sloop/"
     * 返回 "/sloop"
     */
    private static String formatPath(String path) {
        if (!path.startsWith("/"))
            path = "/" + path;
        while (path.endsWith("/"))
            path = new String(path.toCharArray(), 0, path.length() - 1);
        return path;
    }

    /**
     * @return 存储卡是否挂载(存在)
     */
    public static boolean isMountSdcard() {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 判断文件是否已经存在
     *@param fileName 要检查的文件名
     * @return boolean, true表示存在，false表示不存在
     */
    public static boolean isFileExist(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    public static File saveToSD(Bitmap bmp, String mFileName)
    {
        if (isMountSdcard())
        {
            //生成文件名
//            String imageDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date(System.currentTimeMillis()));
////            String mFileName = String.format(FILE_SAVE_NAME_TEMPLATE, imageDate);
//            mFileName = mFileName.replaceAll("irg", "jpg");
            if(mFileName.equals("")) {
                long systemTime = System.currentTimeMillis();
                String imageDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date(systemTime));
                mFileName = String.format(FILE_SAVE_NAME_TEMPLATE, imageDate);
            } else {
                mFileName = mFileName.replaceAll("irg", "jpg");
            }
            //创建文件夹
            File dir = new File(DIR_PATH);
            //判断文件夹是否存在，不存在则创建
            if (!dir.exists()) {
                dir.mkdirs();
            }
            //创建文件
            File file = new File(dir,mFileName);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                if (fos != null) {
                    //第一参数是图片格式，第二参数是图片质量，第三参数是输出流
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    Log.d("ScreenCap", "文件已保存,filepath="+file.getPath());
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
    public static ArrayList<String> getAllFiles(String path, ArrayList<Integer> videoIndex) {
        Log.i(TAG, "getAllFiles: path="+path);
        ArrayList<String> urls = new ArrayList<>();
        File file = new File(path);
        File files[] = file.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    String path1 = files[i].getPath() + "/";
                    getAllFiles(path1,videoIndex);
                } else {
                    String name[] = files[i].getName().split("\\.");
                    Log.i(TAG, "getAllFiles: name="+name[0]);
                    if (name.length == 2 && (name[1].equals("jpg") )) {
                        urls.add(files[i].getPath());
                    } else if (name.length == 2 && (name[1].equals("h264"))) {
                        urls.add(files[i].getPath());
                        videoIndex.add(i);
                    }
                }
            }
        }
        else {
            Log.i(TAG, "getAllFiles: files==null");
        }
        return urls;
    }

    public static File saveToSDFromInput(String path, String fileName, InputStream inputStream)
    {
        File file=null;
        FileOutputStream fos=null;
        //创建文件夹
        File dir = new File(DIR_PATH);
        //判断文件夹是否存在，不存在则创建
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //创建文件
        file = new File(dir,fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            fos = new FileOutputStream(file);
            if (fos != null) {
                byte[] buffer = new byte[1*1024 * 1024];
                int len;
                while ((len=inputStream.read(buffer))!= -1) {
                    fos.write(buffer,0,len);
                }
            }
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

    /**
     * 保留文件名及后缀
     */
    public static String getFileNameWithSuffix(String pathandname) {
        int start = pathandname.lastIndexOf("/");
        if (start != -1) {
            return pathandname.substring(start + 1);
        } else {
            return null;
        }
    }
}


