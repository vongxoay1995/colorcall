package com.htn.colorcall.utils;


import com.htn.colorcall.R;
import com.htn.colorcall.model.Category;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

public class HawkHelper {
    private static String LOAD_DATA_FIRST_FIRST = "LOAD_DATA_FIRST_FIRST";
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
}
