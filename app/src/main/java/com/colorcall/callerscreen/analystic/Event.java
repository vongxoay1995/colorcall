package com.colorcall.callerscreen.analystic;

import android.os.Bundle;

public class Event {
    private String key;
    private Bundle bundleValue;

    public Event() {
    }

    public Event(String key, Bundle bundleValue) {
        this.key = key;
        this.bundleValue = bundleValue;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Bundle getBundleValue() {
        return bundleValue;
    }

    public void setBundleValue(Bundle bundleValue) {
        this.bundleValue = bundleValue;
    }

    @Override
    public String toString() {
        return "Event{" +
                "key='" + key + '\'' +
                ", bundleValue=" + bundleValue +
                '}';
    }
}
