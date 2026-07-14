package com.colorcall.callerscreen.paywall;


import static com.colorcall.callerscreen.constan.Constant.POLICY_URL;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;

import com.colorcall.callerscreen.constan.Constant;

public class WordClickableSpan2 extends ClickableSpan {
    private boolean isUnderline;

    private boolean policy;

    public WordClickableSpan2(boolean isUnderline) {
        this.isUnderline = isUnderline;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(Color.parseColor("#2196F3"));
        ds.setUnderlineText(isUnderline);
        ds.bgColor = 0;
    }

    public void setPolicy(boolean policy) {
        this.policy = policy;
    }

    @Override
    public void onClick(@NonNull View widget) {
        widget.invalidate();
        if(policy) {
            gotoPolicy(widget.getContext());
        }
        else {
            gotoSubcriptTerm(widget.getContext());
        }
    }

    private void gotoPolicy(Context context) {
        try {
            Intent intentUpdate = new Intent(Intent.ACTION_VIEW);
            intentUpdate.setData(Uri.parse(POLICY_URL));
            context.startActivity(intentUpdate);
        } catch (ActivityNotFoundException anfe) {
            anfe.printStackTrace();
        }
    }

    private void gotoSubcriptTerm(Context context) {
        try {
            Intent intentUpdate = new Intent(Intent.ACTION_VIEW);
            intentUpdate.setData(Uri.parse(Constant.TERMS_URL));
            context.startActivity(intentUpdate);
        } catch (ActivityNotFoundException anfe) {
            anfe.printStackTrace();
        }
    }
}
