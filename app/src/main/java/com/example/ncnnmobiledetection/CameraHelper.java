package com.example.ncnnmobiledetection;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;

import com.unity3d.player.UnityPlayer;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;


public class CameraHelper {

    private static CameraHelper instance;

    private Camera camera;

    private boolean isPreview;

    private static int cameraId = 0;

    private SurfaceTexture surfaceTexture;

    private WeakReference<Activity> mActivity;

    public static CameraHelper get() {
        if (instance == null) {
            instance = new CameraHelper();
        }
        return instance;
    }


    public void openCamera(Activity activity) {
        mActivity = new WeakReference<>(activity);
        if (camera == null) {
            try {
                camera = Camera.open(cameraId);
                setParameters();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void openCamera(Activity activity, int cameraId) {
        mActivity = new WeakReference<>(activity);
        if (camera == null) {
            try {
                camera = Camera.open(cameraId);
                setParameters();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setParameters() {
        Camera.Parameters parameters = camera.getParameters();
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        parameters.setPreviewSize(sizes.get(0).width, sizes.get(0).height);
        List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
        parameters.setPictureSize(pictureSizes.get(0).width, pictureSizes.get(0).height);
        setCameraDisplayOrientation(mActivity.get());
        camera.setParameters(parameters);
    }

    public void switchCamera() {
        stopCamera();
        cameraId = cameraId == 0 ? 1 : 0;
        openCamera(mActivity.get(), cameraId);
        startPreview(surfaceTexture);
    }

    public void startPreview(SurfaceTexture mTexture) {
        this.surfaceTexture = mTexture;
        if (camera != null && !isPreview) {
            try {
                camera.setPreviewTexture(mTexture);
                camera.startPreview();
                isPreview = true;

                camera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        Log.d("onPreviewFrame","onPreviewFrame ----------------------------------");
                        UnityPlayer.UnitySendMessage("Canvas", "fromAndroidMsg", "5");
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopPreview() {
        if (camera != null && isPreview) {
            camera.stopPreview();
            isPreview = false;
        }
    }

    public boolean isPreviewing() {
        return isPreview;
    }

    public void stopCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            stopPreview();
            camera.release();
            camera = null;
        }
    }

    /**
     * 旋转处理: 调整画面方向和手机的方向一致
     * 获取摄像头默认旋转角度后, 逆时针旋转回来
     */
    public void setCameraDisplayOrientation(Activity activity) {
        CameraInfo cameraInfo = getCameraInfo();
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
                break;
        }
        int result;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    /**
     * 获取相机信息
     */
    public CameraInfo getCameraInfo() {
        if (camera != null) {
            Camera.Size size = camera.getParameters().getPreviewSize();
            CameraInfo cameraInfo = new CameraInfo();
            cameraInfo.previewWidth = size.width;
            cameraInfo.previewHeight = size.height;
            Camera.CameraInfo cameraInfo1 = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo1);
            cameraInfo.orientation = cameraInfo1.orientation;
            cameraInfo.facing = cameraInfo1.facing;
            Camera.Size pictureSize = camera.getParameters().getPictureSize();
            cameraInfo.pictureWidth = pictureSize.width;
            cameraInfo.pictureHeight = pictureSize.height;
            return cameraInfo;
        }
        return null;
    }

    public Camera getCamera() {
        return camera;
    }

    public class CameraInfo {

        public int previewWidth;

        public int previewHeight;

        public int orientation;

        /**
         * 前置摄像头: Camera.CameraInfo.CAMERA_FACING_FRONT
         * 后置摄像头: Camera.CameraInfo.CAMERA_FACING_BACK
         */
        public int facing;

        public int pictureWidth;

        public int pictureHeight;
    }
}
