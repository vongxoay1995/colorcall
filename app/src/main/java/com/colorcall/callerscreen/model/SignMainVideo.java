package com.colorcall.callerscreen.model;

public class SignMainVideo {
    private boolean isloadComplete;
    private boolean isRefresh;

    public SignMainVideo(boolean isloadComplete, boolean isRefresh) {
        this.isloadComplete = isloadComplete;
        this.isRefresh = isRefresh;
    }

    public boolean isIsloadComplete() {
        return isloadComplete;
    }

    public void setIsloadComplete(boolean isloadComplete) {
        this.isloadComplete = isloadComplete;
    }

    public boolean isRefresh() {
        return isRefresh;
    }

    public void setRefresh(boolean refresh) {
        isRefresh = refresh;
    }

}
