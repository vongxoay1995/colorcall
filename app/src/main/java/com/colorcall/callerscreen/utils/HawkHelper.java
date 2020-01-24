package com.colorcall.callerscreen.utils;


import com.colorcall.callerscreen.model.Background;
import com.colorcall.callerscreen.model.Category;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;

public class HawkHelper {
    private static String LOAD_DATA_FIRST_FIRST = "LOAD_DATA_FIRST_FIRST";
    private static String ENABLE_COLOR = "ENABLE_COLOR";
    private static String BACKGROUND_SELECT = "BACKGROUND_SELECT";


    private static String LIST_CATEGORY = "LIST_CATEGORY";
    public static boolean isLoadDataFirst() {
        return Hawk.get(LOAD_DATA_FIRST_FIRST, false);
    }

    public static void setLoadDataFirst(boolean value) {
        Hawk.put(LOAD_DATA_FIRST_FIRST, value);
    }
    public static ArrayList<Category> getListCategory() {
        return Hawk.get(LIST_CATEGORY, new ArrayList<>());
    }

    public static void setListCategory(ArrayList<Category> listCategory) {
        Hawk.put(LIST_CATEGORY, listCategory);
    }
    public static boolean isEnableColorCall() {
        return Hawk.get(ENABLE_COLOR, true);
    }

    public static void setStateColorCall(boolean value) {
        Hawk.put(ENABLE_COLOR, value);
    }

    public static void setBackgroundSelect(Background backgroundSelect){
        Hawk.put(BACKGROUND_SELECT,backgroundSelect);
    }
    public static Background getBackgroundSelect(){
        Background background = new Background(null,0,"thumb/lovely/ll_f.webp","/raw/ll_f",false);
       return Hawk.get(BACKGROUND_SELECT,background);
    }
}
