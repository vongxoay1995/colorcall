package com.colorcall.callerscreen.response;

import com.google.gson.annotations.SerializedName;

public class ChangeLog {
    @SerializedName("version")
    private int version;
    @SerializedName("description")
    private String description;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
