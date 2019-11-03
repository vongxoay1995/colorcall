package com.htn.colorcall.model;

public class Category {
    private String assetsDir;
    private int symbol;
    private String title;

    public Category() {
    }

    public Category(String title2, int symbol2, String assetsDir2) {
        this.title = title2;
        this.symbol = symbol2;
        this.assetsDir = assetsDir2;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title2) {
        this.title = title2;
    }

    public int getSymbol() {
        return this.symbol;
    }

    public void setSymbol(int symbol2) {
        this.symbol = symbol2;
    }

    public String getAssetsDir() {
        return this.assetsDir;
    }

    public void setAssetsDir(String assetsDir2) {
        this.assetsDir = assetsDir2;
    }
}
