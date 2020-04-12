package com.colorcall.callerscreen.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.model.Background;
import com.colorcall.callerscreen.model.Category;
import com.colorcall.callerscreen.model.Thumb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class CategoryUtils {
    public static void setInforCategory(Context context, int num, TextView txtName, ImageView imgSymbol) {
        switch (num) {
            case 0:
                txtName.setText(context.getString(R.string.recommend));
                setImageSymbol(context, imgSymbol, R.drawable.ic_recommend);
                break;
            case 1:
                txtName.setText(context.getString(R.string.popular));
                setImageSymbol(context, imgSymbol, R.drawable.ic_pop);
                break;
            case 2:
                txtName.setText(context.getString(R.string.mytheme));
                setImageSymbol(context, imgSymbol, R.drawable.ic_file);
                break;
            case 3:
                txtName.setText(context.getString(R.string.colorEffect));
                setImageSymbol(context, imgSymbol, R.drawable.ic_color_effect);
                break;
            case 4:
                txtName.setText(context.getString(R.string.lovely));
                setImageSymbol(context, imgSymbol, R.drawable.ic_lovely);
                break;
        }
    }

    public static void setImageSymbol(Context context, ImageView imgSymbol, int Res) {
        if (Build.VERSION.SDK_INT >= 21) {
            imgSymbol.setImageResource(Res);
        } else {
            imgSymbol.setImageDrawable(VectorDrawableCompat.create(context.getResources(), Res, context.getTheme()));
        }
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
                    thumbs.add(new Thumb(sb.toString(), true));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return thumbs;
    }

    public static ArrayList<Category> loadData(Context context, String path) {
        ArrayList<Category> list = new ArrayList<>();
        ArrayList<Background> listBackground;
        String pathThumb, pathFile;
        Category category;
        String prefixVideo = "/raw/";
        try {
            String[] listCategory = context.getAssets().list(path);
            if (listCategory.length > 0) {
                listBackground = new ArrayList<>();
                listBackground.add(new Background(1, "", "", false));
                list.add(new Category(1, listBackground, false));
            }
            for (int i = 0; i < listCategory.length; i++) {
                category = new Category();
                category.setCategoryNum(i + 2);
                category.setDefault(true);
                listBackground = new ArrayList<>();
                String[] listItem = context.getAssets().list(path + "/" + listCategory[i]);
                for (int j = 0; j < listItem.length; j++) {
                    pathThumb = path + "/" + listCategory[i] + "/" + listItem[j];
                    pathFile = prefixVideo + listItem[j].substring(0, listItem[j].length() - 5) ;
                    Background background = new Background(0, pathThumb, pathFile, false);
                    listBackground.add(background);
                }
                category.setListFile(listBackground);
                list.add(category);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void addRecommendCategory(Context context, ArrayList<Category> list) {
        ArrayList<Background> listBackground;
        String pathThumb, pathFile;
        Category category;
        String prefixVideo = "/raw/";
        String[] arrRecommend = context.getResources().getStringArray(R.array.recommend);
        ArrayList<String> listRecommend;
        listRecommend =   new ArrayList<>(Arrays.asList(arrRecommend));
        category = new Category();
        category.setCategoryNum(4);
        category.setDefault(true);
        listBackground = new ArrayList<>();
        for (int i = 0; i <listRecommend.size();i++){
            pathThumb = listRecommend.get(i);
            pathFile = prefixVideo + listRecommend.get(i).substring(listRecommend.get(i).lastIndexOf("/")+1,listRecommend.get(i).length() -5) ;
            Background background = new Background(0, pathThumb, pathFile, false);
            listBackground.add(background);
        }
        category.setListFile(listBackground);
        list.add(0,category);
        addPopularCategory(context,list);
    }
    public static void addPopularCategory(Context context, ArrayList<Category> list) {
        ArrayList<Background> listBackground;
        String pathThumb, pathFile;
        Category category;
        String prefixVideo = "/raw/";
        String[] arrPopular = context.getResources().getStringArray(R.array.popular);
        ArrayList<String> listRecommend;
        listRecommend = new ArrayList<>(Arrays.asList(arrPopular));
        category = new Category();
        category.setCategoryNum(5);
        category.setDefault(true);
        listBackground = new ArrayList<>();
        for (int i = 0; i <listRecommend.size();i++){
            pathThumb = listRecommend.get(i);
            pathFile = prefixVideo + listRecommend.get(i).substring(listRecommend.get(i).lastIndexOf("/")+1,listRecommend.get(i).length() -5) ;
            Background background = new Background(0, pathThumb, pathFile, false);
            listBackground.add(background);
        }
        category.setListFile(listBackground);
        list.add(1,category);
    }
}
