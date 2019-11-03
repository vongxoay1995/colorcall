package com.htn.colorcall.main;

import android.util.Log;

import com.htn.colorcall.R;

import ss.com.bannerslider.adapters.SliderAdapter;
import ss.com.bannerslider.viewholder.ImageSlideViewHolder;

public class MainSliderAdapter extends SliderAdapter {
    public int getItemCount() {
        return 3;
    }

    public void onBindImageSlide(int position, ImageSlideViewHolder viewHolder) {
        StringBuilder sb = new StringBuilder();
        sb.append(position);
        sb.append("");
        Log.e("Pos", sb.toString());
        switch (position) {
            case 0:
                viewHolder.bindImageSlide( R.drawable.test3);
                return;
            case 1:
                viewHolder.bindImageSlide( R.drawable.test1);
                return;
            case 2:
                viewHolder.bindImageSlide( R.drawable.test2);
                return;
            default:
                return;
        }
    }
}
