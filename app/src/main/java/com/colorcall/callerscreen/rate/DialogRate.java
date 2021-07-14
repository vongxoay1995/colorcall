package com.colorcall.callerscreen.rate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.custom.RatingBar;
import com.colorcall.callerscreen.utils.AppUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DialogRate extends Dialog {
    @BindView(R.id.ratingbar)
    RatingBar ratingBar;
    @BindView(R.id.footer)
    LinearLayout footer;
    @BindView(R.id.txtRate)
    TextView txtRate;
    @BindView(R.id.imgExit)
    ImageView imgExit;
    @BindView(R.id.moreAudio)
    TextView moreAudio;
    @BindView(R.id.moreImage)
    TextView moreImage;
    @BindView(R.id.moreVideo)
    TextView moreVideo;
    @BindView(R.id.feedbackRate)
    TextView feedbackRate;
    @BindView(R.id.leastAds)
    TextView leastAds;
    @BindView(R.id.txtAnotherFeedback)
    EditText edtAnotherFeedback;
    private int rate;
    private StringBuilder sb = new StringBuilder();
    private boolean isClickMoreVideo, isClickMoreImage, isClickMoreAudio, isClickLeastAds;
    private DialogRateListener dialogRateListener;
    private String content = "FeedBack Call Color ";
    private RelativeLayout.LayoutParams layoutParams;
    private Activity activity;
    public DialogRate(Activity context, DialogRateListener dialogRateListener) {
        super(context);
        activity = context;
        this.dialogRateListener = dialogRateListener;
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_rate, null);
        ButterKnife.bind(this, view);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(view);
        Window window = getWindow();
        setCancelable(false);
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.getAttributes().windowAnimations = R.style.DialogAnimationInOut;
        }
        edtAnotherFeedback.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                AppUtils.hideKeyboard(edtAnotherFeedback);
            }
            return false;
        });
        layoutParams = (RelativeLayout.LayoutParams) ratingBar.getLayoutParams();
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            rate = (int) rating;
            setTitleFeedBackRate(rate);
            if (rating < 5) {
                footer.setVisibility(View.VISIBLE);
                txtRate.setVisibility(View.GONE);
                layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, context.getResources().getDimensionPixelSize(R.dimen._5sdp));
            } else {
                footer.setVisibility(View.GONE);
                txtRate.setVisibility(View.VISIBLE);
                layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, context.getResources().getDimensionPixelSize(R.dimen._35sdp));
                AppUtils.hideKeyboard(edtAnotherFeedback);
            }
        });
    }

    private void setTitleFeedBackRate(int rate) {
        switch (rate){
            case 1:
                feedbackRate.setText(getContext().getString(R.string.very_bad));
                break;
            case 2:
                feedbackRate.setText(getContext().getString(R.string.bad));
                break;
            case 3:
                feedbackRate.setText(getContext().getString(R.string.normal));
                break;
            case 4:
                feedbackRate.setText(getContext().getString(R.string.good));
                break;
            case 5:
                feedbackRate.setText(getContext().getString(R.string.very_good));
                break;
        }
    }

    @SuppressLint("ResourceType")
    @OnClick({R.id.btnNotNow,R.id.txtRate, R.id.btnFeedBack, R.id.imgExit, R.id.moreVideo, R.id.moreImage, R.id.moreAudio, R.id.leastAds,R.id.root})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnFeedBack:
                if (rate < 1) {
                    Toast.makeText(getContext(), getContext().getString(R.string.giveStar), Toast.LENGTH_SHORT).show();
                } else {
                    dismiss();
                    AppUtils.hideKeyboard(edtAnotherFeedback);
                    addContent();
                    content = content + edtAnotherFeedback.getText().toString() + " and " + sb.toString();
                    dialogRateListener.onFeedBack(content, rate);
                }
                break;
            case R.id.txtRate:
                dismiss();
                AppUtils.hideKeyboard(edtAnotherFeedback);
                dialogRateListener.onRate();
                break;
            case R.id.btnNotNow:
                AppUtils.hideKeyboard(edtAnotherFeedback);
                dismiss();
                break;
            case R.id.root:
                AppUtils.hideKeyboard(edtAnotherFeedback);
                break;
            case R.id.imgExit:
                break;
            case R.id.moreVideo:
                checkStateClick(1, isClickMoreVideo, moreVideo);
                break;
            case R.id.moreImage:
                checkStateClick(2, isClickMoreImage, moreImage);
                break;
            case R.id.moreAudio:
                checkStateClick(3, isClickMoreAudio, moreAudio);
                break;
            case R.id.leastAds:
                checkStateClick(4, isClickLeastAds, leastAds);
                break;
        }
    }

    private void addContent() {
       if(isClickMoreVideo){
           sb.append("[Add more video] ");
       }
        if(isClickMoreImage){
            sb.append("[Add more image] ");
        }
        if(isClickMoreAudio){
            sb.append("[Add more audio] ");
        }
        if(isClickLeastAds){
            sb.append("[Remove ads] ");
        }
    }

    private void checkStateClick(int valueClick, boolean isClick, TextView view) {
        if (!isClick) {
            setTrueValueClick(valueClick);
            view.setTextColor(Color.WHITE);
            view.setBackground(getContext().getResources().getDrawable(R.drawable.bg_feedback_select));
        } else {
            setFalseValueClick(valueClick);
            view.setTextColor(Color.parseColor("#292929"));
            view.setBackground(getContext().getResources().getDrawable(R.drawable.bg_feedback_unselect));
        }
    }

    private void setFalseValueClick(int value) {
        switch (value) {
            case 1:
                isClickMoreVideo = false;
                break;
            case 2:
                isClickMoreImage = false;
                break;
            case 3:
                isClickMoreAudio = false;
                break;
            case 4:
                isClickLeastAds = false;
                break;
        }
    }

    private void setTrueValueClick(int value) {
        switch (value) {
            case 1:
                isClickMoreVideo = true;
                break;
            case 2:
                isClickMoreImage = true;
                break;
            case 3:
                isClickMoreAudio = true;
                break;
            case 4:
                isClickLeastAds = true;
                break;
        }
    }

    public interface DialogRateListener {
        void onRate();
        void onFeedBack(String content, int rate);
    }
}
