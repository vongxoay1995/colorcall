package com.colorcall.callerscreen.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class FullScreenVideoView extends VideoView {

    public FullScreenVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(VideoView.getDefaultSize(0, widthMeasureSpec), VideoView.getDefaultSize(0, heightMeasureSpec));
    }
}
