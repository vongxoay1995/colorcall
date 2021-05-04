package com.colorcall.callerscreen.response;

import com.colorcall.callerscreen.database.Background;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class AppData {
    @SerializedName("data")
    private ArrayList<Background> app;

    @SerializedName("change-log")
    private ChangeLog changeLog;

    public AppData (){}
    public AppData(ArrayList<Background> app) {
        this.app = app;
    }

    public ArrayList<Background> getApp() {
        return app;
    }

    public void setApp(ArrayList<Background> app) {
        this.app = app;
    }

    public ChangeLog getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(ChangeLog changeLog) {
        this.changeLog = changeLog;
    }
}
