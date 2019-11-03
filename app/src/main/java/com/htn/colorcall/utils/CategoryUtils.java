package com.htn.colorcall.utils;

import android.content.Context;
import android.util.Log;

import com.htn.colorcall.R;
import com.htn.colorcall.constan.Constant;
import com.htn.colorcall.model.Category;
import com.htn.colorcall.model.Thumb;

import java.io.IOException;
import java.util.ArrayList;

public class CategoryUtils {
    public static ArrayList<Category> getListCategory(Context context, String... path) {
        ArrayList<Category> categories = new ArrayList<>();
        ArrayList<String> arrTitleName = new ArrayList<>();
        ArrayList<Integer> arrSymbol = new ArrayList<>();
        initArrCategory(context, arrTitleName, arrSymbol);
        try {
            for (String dir : path) {
                String[] list = context.getAssets().list(dir);
                StringBuilder sb = new StringBuilder();
                sb.append(list.length);
                sb.append("");
                Log.e("List", sb.toString());
                for (int i = 0; i < list.length; i++) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(list[i]);
                    sb2.append("");
                    Log.e("DS", sb2.toString());
                    if (i == 2) {
                        categories.add(new Category((String) arrTitleName.get(i), ((Integer) arrSymbol.get(i)).intValue(), ""));
                    } else {
                        categories.add(new Category((String) arrTitleName.get(i), ((Integer) arrSymbol.get(i)).intValue(), Constant.thumbPath[i]));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public static void initArrCategory(Context context, ArrayList<String> arrTitleName, ArrayList<Integer> arrSymbol) {
        arrTitleName.add(context.getString(R.string.recommend));
        arrTitleName.add(context.getString(R.string.popular));
        arrTitleName.add(context.getString(R.string.mytheme));
        arrTitleName.add(context.getString(R.string.colorEffect));
        arrTitleName.add(context.getString(R.string.lovely));
        arrSymbol.add(Integer.valueOf(R.drawable.ic_recommend));
        arrSymbol.add(Integer.valueOf(R.drawable.ic_pop));
        arrSymbol.add(Integer.valueOf(R.drawable.ic_file));
        arrSymbol.add(Integer.valueOf(R.drawable.ic_color_effect));
        arrSymbol.add(Integer.valueOf(R.drawable.ic_lovely));
    }

    public static ArrayList<Thumb> getListThumb(Context context, String... path) {
        String[] list;
        ArrayList<Thumb> thumbs = new ArrayList<>();
        try {
            for (String dir : path) {
                for (String stickerName : context.getAssets().list(dir)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(dir);
                    sb.append("/");
                    sb.append(stickerName);
                    thumbs.add(new Thumb(sb.toString()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return thumbs;
    }
}
