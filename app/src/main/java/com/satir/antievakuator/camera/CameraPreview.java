package com.satir.antievakuator.camera;

import android.content.Context;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import java.io.IOException;
import java.io.Serializable;

import static com.satir.antievakuator.utils.ToastMaker.toastLongMessage;


public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Serializable{

    SurfaceHolder mHolder;
    Camera mCamera;
    Context mContext;

    public CameraPreview(Context context, Camera camera, boolean shouldAddCallback) {
        super(context);
        mContext = context;
        mCamera = camera;
        mHolder = getHolder();
        if(shouldAddCallback) {
            mHolder.addCallback(this);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if (mHolder.getSurface() == null)
            return;

        mCamera.stopPreview();

        setCameraDisplayOrientation();

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        }
        catch (IOException e) {
            toastLongMessage("Camera preview failed");
        }
    }

    public void setCameraDisplayOrientation() {
        if (mCamera == null)
            return;

        Camera.CameraInfo info = new Camera.CameraInfo(); // 1
        Camera.getCameraInfo(0, info);

        WindowManager winManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int rotation = winManager.getDefaultDisplay().getRotation();

        int degrees = 0;

        switch (rotation) { //3
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result; //4
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        }
        else {
            result = (info.orientation - degrees + 360) % 360;
        }

        mCamera.setDisplayOrientation(result);

        Camera.Parameters parameters = mCamera.getParameters();
        int rotate = (degrees + 270) % 360;
        parameters.setRotation(rotate);
        mCamera.setParameters(parameters);
    }

    public void cleanPreview(){
        if(mHolder != null){
            mHolder.removeCallback(this);
        }
        mContext = null;
        mCamera = null;
        mHolder = null; //2
    }

    public SurfaceHolder getHolder(){
        return mHolder;
    }

}
