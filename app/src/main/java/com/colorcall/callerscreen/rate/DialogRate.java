package com.colorcall.callerscreen.rate;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.Analystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.databinding.DialogRateBinding;
import com.colorcall.callerscreen.utils.AppUtils;


public class DialogRate extends Dialog {
    private final DialogRateListener dialogRateListener;
    private final Analystic analystic;
    private int rate;
    private boolean isClickMoreVideo, isClickMoreImage, isClickMoreAudio, isClickLeastAds;
    private String content = "FeedBack Call Color ";
    private final StringBuilder sb = new StringBuilder();
    private DialogRateBinding binding;

    public DialogRate(@NonNull Activity context, DialogRateListener dialogRateListener) {
        super(context);
        analystic = Analystic.getInstance(context);
        this.dialogRateListener = dialogRateListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = DialogRateBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        Window window = getWindow();
        setCancelable(false);
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.getAttributes().windowAnimations = R.style.DialogAnimationInOut;
        }
        binding.txtAnotherFeedback.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                AppUtils.hideKeyboard(binding.txtAnotherFeedback);
            }
            return false;
        });
        binding.ratingbar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            rate = (int) rating;
            setTitleFeedBackRate(rate);
            if (rating < 5) {
                binding.footer.setVisibility(View.VISIBLE);
                binding.txtRate.setVisibility(View.GONE);
                binding.ratingbar.setLayoutParams(binding.ratingbar.getLayoutParams());
            } else {
                binding.footer.setVisibility(View.GONE);
                binding.txtRate.setVisibility(View.VISIBLE);
                binding.ratingbar.setLayoutParams(binding.ratingbar.getLayoutParams());
                AppUtils.hideKeyboard(binding.txtAnotherFeedback);
            }
        });
        setListeners();
    }

    private void setListeners() {
        binding.btnNotNow.setOnClickListener(v -> {
            analystic.trackEvent(ManagerEvent.rateNotNow());
            AppUtils.hideKeyboard(binding.txtAnotherFeedback);
            dismiss();
        });
        binding.txtRate.setOnClickListener(v -> {
            dismiss();
            AppUtils.hideKeyboard(binding.txtAnotherFeedback);
            dialogRateListener.onRate(rate);
        });
        binding.btnFeedBack.setOnClickListener(v -> {
            if (rate < 1) {
                Toast.makeText(getContext(), getContext().getString(R.string.giveStar), Toast.LENGTH_SHORT).show();
            } else {
                dismiss();
                AppUtils.hideKeyboard(binding.txtAnotherFeedback);
                addContent();
                content = content + binding.txtAnotherFeedback.getText().toString() + " and " + sb.toString();
                dialogRateListener.onFeedBack(content, rate);
            }
        });
        binding.moreVideo.setOnClickListener(v -> checkStateClick(1, isClickMoreVideo, binding.moreVideo));
        binding.moreImage.setOnClickListener(v -> checkStateClick(2, isClickMoreImage, binding.moreImage));
        binding.moreAudio.setOnClickListener(v -> checkStateClick(3, isClickMoreAudio, binding.moreAudio));
        binding.leastAds.setOnClickListener(v -> checkStateClick(4, isClickLeastAds, binding.leastAds));
    }

    private void setTitleFeedBackRate(int rate) {
        switch (rate) {
            case 1:
                binding.feedbackRate.setText(getContext().getString(R.string.very_bad));
                break;
            case 2:
                binding.feedbackRate.setText(getContext().getString(R.string.bad));
                break;
            case 3:
                binding.feedbackRate.setText(getContext().getString(R.string.normal));
                break;
            case 4:
                binding.feedbackRate.setText(getContext().getString(R.string.good));
                break;
            case 5:
                binding.feedbackRate.setText(getContext().getString(R.string.very_good));
                break;
        }
    }

    private void addContent() {
        if (isClickMoreVideo) {
            sb.append("[Add more video] ");
        }
        if (isClickMoreImage) {
            sb.append("[Add more image] ");
        }
        if (isClickMoreAudio) {
            sb.append("[Add more audio] ");
        }
        if (isClickLeastAds) {
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
        void onRate(int rate);
        void onFeedBack(String content, int rate);
    }
}

