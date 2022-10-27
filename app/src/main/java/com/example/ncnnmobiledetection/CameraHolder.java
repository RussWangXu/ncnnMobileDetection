package com.example.ncnnmobiledetection;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.unity3d.player.UnityPlayer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class CameraHolder implements SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = CameraHolder.class.getSimpleName();

    private SurfaceTexture mSurfaceTexture; //camera preview
    private GLTextureOES mTextureOES;       //GL_TEXTURE_EXTERNAL_OES
    private GLTexture2D mUnityTexture;      //GL_TEXTURE_2D 用于在Unity里显示的贴图
    private FBO mFBO;

    private GLTexture2D mUnityTextureCopy;  //GL_TEXTURE_2D 用于拷回Unity渲染结果
    private FBO mFBOCopy;

    private float[] mMVPMatrix = new float[16];
    private boolean mFrameUpdated;  //帧是否更新
    private boolean mIsCopyed = true;

    private Camera mCamera;
    public String imgString;

    public void openCamera() {
        Log.d(TAG, "openCamera");
        mFrameUpdated = false;
        mMVPMatrix = new float[16];

        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
        }

        mCamera = Camera.open(1);

        // 利用OpenGL生成OES纹理并绑定到mSurfaceTexture
        // 再把camera的预览数据设置显示到mSurfaceTexture，OpenGL就能拿到摄像头数据。
        mTextureOES = new GLTextureOES(UnityPlayer.currentActivity, 300,300);
        mSurfaceTexture = new SurfaceTexture(mTextureOES.getTextureID());
        mSurfaceTexture.setOnFrameAvailableListener(this);


        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
//        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
//            @Override
//            public void onPreviewFrame(byte[] data, Camera camera) {
//                imgString = Base64.encodeToString(data, android.util.Base64.DEFAULT);
//                UnityPlayer.UnitySendMessage("Canvas", "fromAndroidMsg", data.toString());
//                Log.d("onPreviewFrame", imgString);
//            }
//        });

    }

    public void closeCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    public boolean isFrameUpdated() {
        return mFrameUpdated;
    }

    public int getWidth() {
        return mCamera.getParameters().getPreviewSize().width;
    }

    public int getHeight() {
        return mCamera.getParameters().getPreviewSize().height;
    }

    public int updateTexture() {
        Log.d(TAG, "updateTexture: ");
        synchronized (this) {
            if (mFrameUpdated) { mFrameUpdated = false; }
            UnityPlayer.UnitySendMessage("Canvas", "fromAndroidMsg", "回不回走这个");
            mSurfaceTexture.updateTexImage();
            int width = mCamera.getParameters().getPreviewSize().width;
            int height = mCamera.getParameters().getPreviewSize().height;

            // 根据宽高创建Unity使用的GL_TEXTURE_2D纹理
            if (mUnityTexture == null) {
                Log.d(TAG, "width = " + width + ", height = " + height);
                mUnityTexture = new GLTexture2D(UnityPlayer.currentActivity, width, height);
                mFBO = new FBO(mUnityTexture);
            }
            Matrix.setIdentityM(mMVPMatrix, 0);
            mFBO.FBOBegin();
            GLES20.glViewport(0, 0, width, height);
            mTextureOES.draw(mMVPMatrix);
            mFBO.FBOEnd();

            Point size = new Point();
            if (Build.VERSION.SDK_INT >= 17) {
                UnityPlayer.currentActivity.getWindowManager().getDefaultDisplay().getRealSize(size);
            } else {
                UnityPlayer.currentActivity.getWindowManager().getDefaultDisplay().getSize(size);
            }
            GLES20.glViewport(0, 0, size.x, size.y);


            // 创建读出的GL_TEXTURE_2D纹理
            if (mUnityTextureCopy == null) {
                Log.d(TAG, "width = " + width + ", height = " + height);
                mUnityTextureCopy = new GLTexture2D(UnityPlayer.currentActivity, size.x, size.y);
                mFBOCopy = new FBO(mUnityTextureCopy);
            }
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mUnityTextureCopy.mTextureID);
            GLES20.glCopyTexSubImage2D(GLES20.GL_TEXTURE_2D, 0,0,0,0,0,size.x, size.y);
            mFBOCopy.FBOBegin();
//            // test是否是当前FBO
//            GLES20.glClearColor(1,0,0,1);
//            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//            GLES20.glFinish();
            int mImageWidth = size.x;
            int mImageHeight = size.y;
            Bitmap dest = Bitmap.createBitmap(mImageWidth, mImageHeight, Bitmap.Config.ARGB_8888);
            final ByteBuffer buffer = ByteBuffer.allocateDirect(mImageWidth * mImageHeight * 4);
            GLES20.glReadPixels(0, 0, mImageWidth, mImageHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
            dest.copyPixelsFromBuffer(buffer);
            dest = null;
            mFBOCopy.FBOEnd();

            return mUnityTexture.getTextureID();
        }
    }

    public void OpenTextureView() {
        UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("Unity", "initGLTexture");


                LayoutInflater inflater = LayoutInflater.from(UnityPlayer.currentActivity);
                View view = inflater.inflate(R.layout.activity_usbcamera, null);
                TextureView textureView = view.findViewById(R.id.camera_view);

                GLTextureOES mTextureOES = new GLTextureOES(UnityPlayer.currentActivity, 300,300);
                SurfaceTexture mSurfaceTexture = new SurfaceTexture(mTextureOES.getTextureID());

//                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                layoutParams.height = 300;
//                layoutParams.width = 300;
//                textureView.setLayoutParams(layoutParams);

                UnityPlayer.UnitySendMessage("Canvas", "fromAndroidMsg", "1");
//                TextureView textureView = new TextureView(UnityPlayer.currentActivity);

                ViewGroup totalViewAd = (ViewGroup) UnityPlayer.currentActivity.getWindow().getDecorView();
                totalViewAd.addView(view);

                textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                    @Override
                    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                        CameraHelper.get().openCamera(UnityPlayer.currentActivity);
                        CameraHelper.get().startPreview(surface);
                        UnityPlayer.UnitySendMessage("Canvas", "fromAndroidMsg", "2");
                    }

                    @Override
                    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                        UnityPlayer.UnitySendMessage("Canvas", "fromAndroidMsg", "3");
                    }

                    @Override
                    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                        CameraHelper.get().stopCamera();
                        return false;
                    }

                    @Override
                    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                        UnityPlayer.UnitySendMessage("Canvas", "fromAndroidMsg", "4");
                        Toast.makeText(UnityPlayer.currentActivity, "surface", Toast.LENGTH_LONG).show();

                    }
                });

                mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        UnityPlayer.UnitySendMessage("Canvas", "fromAndroidMsg", surfaceTexture.toString());
                    }
                });
            }
        });
    }

    public boolean copyTexture() {
        Log.d(TAG, "copyTexture: ");
        synchronized (this) {
            if(mIsCopyed){
                mIsCopyed = false;
                UnityPlayer.UnitySendMessage("Plane","setIsLock","0");
                Point size = new Point();
                if (Build.VERSION.SDK_INT >= 17) {
                    UnityPlayer.currentActivity.getWindowManager().getDefaultDisplay().getRealSize(size);
                } else {
                    UnityPlayer.currentActivity.getWindowManager().getDefaultDisplay().getSize(size);
                }
                if (mUnityTextureCopy == null) {
                    Log.d(TAG, "width = " + size.x + ", height = " + size.y);
                    // 根据宽高创建GL_TEXTURE_2D纹理
                    mUnityTextureCopy = new GLTexture2D(UnityPlayer.currentActivity, size.x, size.y);
                    mFBOCopy = new FBO(mUnityTextureCopy);
                }
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mUnityTextureCopy.mTextureID);
                GLES20.glCopyTexSubImage2D(GLES20.GL_TEXTURE_2D, 0,0,0,0,0,size.x, size.y);
                mFBOCopy.FBOBegin();
        //            GLES20.glClearColor(1,0,0,1);
        //            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                GLES20.glFinish();
//                int mImageWidth = size.x;
//                int mImageHeight = size.y;
//                Bitmap dest = Bitmap.createBitmap(mImageWidth, mImageHeight, Bitmap.Config.ARGB_8888);
//                final ByteBuffer buffer = ByteBuffer.allocateDirect(mImageWidth * mImageHeight * 4);
//                GLES20.glReadPixels(0, 0, mImageWidth, mImageHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
//                dest.copyPixelsFromBuffer(buffer);
//                dest = null;
                //UnityPlayer.UnitySendMessage("Plane","callUpdate","str");
                mFBOCopy.FBOEnd();

//                //测试同步
//                try {
//                    Thread.sleep(1000);//休眠3秒
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                Log.d(TAG, "run: Waiting 1 s");

                UnityPlayer.UnitySendMessage("Plane","setIsLock","1");
                mIsCopyed = true;
                return mIsCopyed;
            }
            else
                return false;
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        //Log.d(TAG, "onFrameAvailable");
        UnityPlayer.UnitySendMessage("Canvas", "fromAndroidMsg", surfaceTexture.toString());
        mFrameUpdated = true;
    }
}
