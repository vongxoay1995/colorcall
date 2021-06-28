package com.colorcall.callerscreen.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Checkable;
import android.widget.ImageView;

public class CircleSelectImageView extends androidx.appcompat.widget.AppCompatImageView implements Checkable {
    public boolean isChecked;
    private static final int[] CHECKED_STATE_SET =
            {android.R.attr.state_checked};
    public CircleSelectImageView(Context context) {
        super(context);
    }

    public CircleSelectImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public CircleSelectImageView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public boolean performClick() {
        toggle();
        boolean performClick = super.performClick();
        if (!performClick) {
            playSoundEffect(0);
        }
        return performClick;
    }

    public int[] onCreateDrawableState(int i) {
        int[] onCreateDrawableState = super.onCreateDrawableState(i + 1);
        if (isChecked()) {
            ImageView.mergeDrawableStates(onCreateDrawableState, CHECKED_STATE_SET);
        }
        return onCreateDrawableState;
    }

    @Override
    public void setChecked(boolean checked) {
        if (this.isChecked != checked) {
            this.isChecked = checked;
            refreshDrawableState();
        }
    }

    @Override
    public boolean isChecked() {
        return this.isChecked;
    }

    @Override
    public void toggle() {
        Log.e("TAN", "toggle: "+isChecked);
        setChecked(!this.isChecked);
    }
}
