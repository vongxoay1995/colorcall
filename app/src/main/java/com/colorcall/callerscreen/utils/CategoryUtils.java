package com.colorcall.callerscreen.utils;

import android.content.Context;
import android.os.Build;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.database.Background;
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
}
