package com.htn.colorcall.main;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;

import ss.com.bannerslider.ImageLoadingService;

public class ImageLoading implements ImageLoadingService {
    public Context context;

    public ImageLoading(Context context2) {
        this.context = context2;
    }

    public void loadImage(String url, ImageView imageView) {
        Glide.with(this.context).load(url).into(imageView);
    }

    public void loadImage(int resource, ImageView imageView) {
        StringBuilder sb = new StringBuilder();
        sb.append(resource);
        sb.append("");
        Log.e("Load,", sb.toString());
        Glide.with(this.context).load(Integer.valueOf(resource)).into(imageView);
    }

    public void loadImage(String url, int placeHolder, int errorDrawable, ImageView imageView) {
        ((RequestBuilder) ((RequestBuilder) Glide.with(this.context).load(url).placeholder(placeHolder)).error(errorDrawable)).into(imageView);
    }
}
