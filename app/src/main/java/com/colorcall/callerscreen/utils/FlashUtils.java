package com.colorcall.callerscreen.utils;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.recyclerview.widget.ItemTouchHelper;

public class FlashUtils implements Runnable {
    public static FlashUtils instance;
    private boolean b;
    private CameraManager camManager;
    private Context context;
    private int count;
    public volatile boolean done;
    private boolean isStopping;
    private Camera mCamera;
    private boolean normalMode;
    private Camera.Parameters parameters;
    private Camera.Parameters parameters1;
    private int repeat;
    private int time_off;
    private int time_on;

    public int getTime_on() {
        return this.time_on;
    }

    public void setTime_on(int i) {
        this.time_on = i;
    }

    public Context getContext() {
        return this.context;
    }

    public void setContext(Context context2) {
        this.context = context2;
    }

    public int getTime_off() {
        return this.time_off;
    }

    public void setTime_off(int i) {
        this.time_off = i;
    }

    public int getRepeat() {
        return this.repeat;
    }

    public void setRepeat(int i) {
        this.repeat = i;
    }

    public boolean isNormalMode() {
        return this.normalMode;
    }

    public void setNormalMode(boolean z) {
        this.normalMode = z;
    }

    public void increaseCountNormalMode() {
        this.count++;
    }

    public int getCountNormalMode() {
        return this.count;
    }

    public static FlashUtils getInstance(boolean z, Context context2) {
        if (instance == null) {
            instance = new FlashUtils();
        }
        instance.setNormalMode(z);
        instance.setTime_on(500);
        instance.setTime_off(500);
        instance.setRepeat(0);
        instance.setContext(context2);
        return instance;
    }

    public static FlashUtils getInstance() {
        if (instance == null) {
            instance = new FlashUtils();
        }
        return instance;
    }

    private FlashUtils() {
        this.b = false;
        this.isStopping = true;
        this.done = true;
        this.time_on = ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION;
        this.time_off = ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION;
        this.repeat = 0;
        this.time_on = 500;
        this.time_off = 500;
        this.repeat = 0;
    }

    public void run() {
        if (this.isStopping) {
            this.isStopping = false;
            if (this.repeat == 0) {
                while (!this.isStopping) {
                    turnOnFlash();
                    SystemClock.sleep(this.time_on);
                    turnOffFlash();
                    SystemClock.sleep(this.time_off);
                }
            } else {
                for (int i = 0; i < this.repeat && !this.isStopping; i++) {
                    turnOnFlash();
                    SystemClock.sleep(this.time_on);
                    turnOffFlash();
                    SystemClock.sleep(this.time_off);
                }
            }
            turnOffFlash();
            this.isStopping = true;
        }
    }

    private void turnOnFlash() {
        if (this.b) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                this.b = true;
                CameraManager cameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
                this.camManager = cameraManager;
                if (cameraManager != null) {
                    this.camManager.setTorchMode(cameraManager.getCameraIdList()[0], true);
                }
            } catch (CameraAccessException unused) {
            }
        } else {
            try {
                this.b = true;
                releaseCamera();
                Camera open = Camera.open();
                this.mCamera = open;
                Camera.Parameters parameters2 = open.getParameters();
                this.parameters1 = parameters2;
                parameters2.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                this.mCamera.setParameters(this.parameters1);
                this.mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void turnOffFlash() {
        if (this.b) {
            if (Build.VERSION.SDK_INT >= 23) {
                try {
                    this.b = false;
                    CameraManager cameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
                    this.camManager = cameraManager;
                    if (cameraManager != null) {
                        this.camManager.setTorchMode(cameraManager.getCameraIdList()[0], false);
                    }
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    releaseCamera();
                    Camera open = Camera.open();
                    this.mCamera = open;
                    Camera.Parameters parameters2 = open.getParameters();
                    this.parameters = parameters2;
                    parameters2.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    this.mCamera.setParameters(this.parameters);
                    this.mCamera.stopPreview();
                    //them vao fix
                    mCamera.release();
                    this.b = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop() {
        this.isStopping = true;
        this.count = 0;
    }

    public boolean isRunning() {
        return !this.isStopping;
    }
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
}
