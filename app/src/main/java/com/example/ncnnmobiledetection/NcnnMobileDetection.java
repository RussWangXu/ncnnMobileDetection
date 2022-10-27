package com.example.ncnnmobiledetection;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

public class NcnnMobileDetection {
    public class Obj
    {
        public float x;
        public float y;
        public float w;
        public float h;
        public String label;
        public float prob;
    }
    public native boolean Init(AssetManager mrg);
    public native Obj[] Detect(Bitmap bitmap, boolean use_gpu);
    static {
        System.loadLibrary("ncnnDetection");
    }
}
