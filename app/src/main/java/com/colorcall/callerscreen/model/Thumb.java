package com.colorcall.callerscreen.model;

public class Thumb {
    private String path;
    private boolean typeThumb;

    public Thumb() {
    }

    public Thumb(String path2, boolean typeThumb) {
        this.path = path2;
        this.typeThumb = typeThumb;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path2) {
        this.path = path2;
    }

    public boolean isTypeThumb() {
        return typeThumb;
    }

    public void setTypeThumb(boolean typeThumb) {
        this.typeThumb = typeThumb;
    }

    @Override
    public String toString() {
        return "Thumb{" +
                "path='" + path + '\'' +
                ", typeThumb=" + typeThumb +
                '}';
    }
}
