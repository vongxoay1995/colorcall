package com.htn.colorcall.model;

import java.util.ArrayList;

public class Category {
    private int numberCategory;
    private ArrayList<Background> listFile;
    private boolean isDefault;
    public Category(int numberCategory, ArrayList<Background> listFile, boolean isDefault) {
        this.numberCategory = numberCategory;
        this.listFile = listFile;
        this.isDefault = isDefault;
    }
    public Category(){}
    public int getCategoryNum() {
        return numberCategory;
    }

    public void setCategoryNum(int numberCategory) {
        this.numberCategory = numberCategory;
    }

    public ArrayList<Background> getListFile() {
        return listFile;
    }

    public void setListFile(ArrayList<Background> listFile) {
        this.listFile = listFile;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    @Override
    public String toString() {
        return "Category{" +
                "numberCategory='" + numberCategory + '\'' +
                ", listFile=" + listFile +
                '}'+"\n";
    }
}
