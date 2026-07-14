package com.colorcall.callerscreen.utils;

import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.os.SystemClock;

public class FlashUtils implements Runnable {
    // Volatile for thread-safe singleton
    private static volatile FlashUtils instance;

    private boolean torchOn;
    private CameraManager camManager;
    private Context context;
    private int count;
    public volatile boolean done;
    private volatile boolean isStopping;
    private boolean normalMode;
    private int repeat;
    private int time_off;
    private int time_on;

    // Safety timeout to prevent infinite flash loop (60 seconds)
    private static final long MAX_FLASH_DURATION_MS = 60_000;

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
            synchronized (FlashUtils.class) {
                if (instance == null) {
                    instance = new FlashUtils();
                }
            }
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
            synchronized (FlashUtils.class) {
                if (instance == null) {
                    instance = new FlashUtils();
                }
            }
        }
        return instance;
    }

    private FlashUtils() {
        this.torchOn = false;
        this.isStopping = true;
        this.done = true;
        this.time_on = 500;
        this.time_off = 500;
        this.repeat = 0;
    }

    public void run() {
        if (this.isStopping) {
            this.isStopping = false;
            long startTime = SystemClock.elapsedRealtime();

            if (this.repeat == 0) {
                while (!this.isStopping) {
                    // Safety timeout check
                    if (SystemClock.elapsedRealtime() - startTime > MAX_FLASH_DURATION_MS) {
                        break;
                    }
                    turnOnFlash();
                    SystemClock.sleep(this.time_on);
                    turnOffFlash();
                    SystemClock.sleep(this.time_off);
                }
            } else {
                for (int i = 0; i < this.repeat && !this.isStopping; i++) {
                    // Safety timeout check
                    if (SystemClock.elapsedRealtime() - startTime > MAX_FLASH_DURATION_MS) {
                        break;
                    }
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
        if (this.torchOn) {
            return;
        }
        try {
            this.torchOn = true;
            CameraManager cameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
            this.camManager = cameraManager;
            if (cameraManager != null) {
                this.camManager.setTorchMode(cameraManager.getCameraIdList()[0], true);
            }
        } catch (Exception unused) {
            // Silently handle camera access errors
        }
    }

    private void turnOffFlash() {
        if (this.torchOn) {
            try {
                this.torchOn = false;
                CameraManager cameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
                this.camManager = cameraManager;
                if (cameraManager != null) {
                    this.camManager.setTorchMode(cameraManager.getCameraIdList()[0], false);
                }
            } catch (Exception e) {
                // Silently handle camera access errors
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
}

