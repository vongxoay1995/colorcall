package com.htn.colorcall.myapplication;

import android.app.Application;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.htn.colorcall.main.ImageLoading;

import ss.com.bannerslider.Slider;

public class MyApplication extends MultiDexApplication {
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        Slider.init(new ImageLoading(this));
    }
}
