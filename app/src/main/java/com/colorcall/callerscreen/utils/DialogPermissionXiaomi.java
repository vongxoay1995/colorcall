package com.colorcall.callerscreen.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.colorcall.callerscreen.databinding.DialogPermissionXiaomiBinding;

import java.util.Objects;

public class DialogPermissionXiaomi extends Dialog {
    private DialogPermissionXiaomiBinding binding;
    public DialogPermissionXiaomiListener listener;
    public void setListener(DialogPermissionXiaomiListener listener) {
        this.listener = listener;
    }
    public DialogPermissionXiaomi(@NonNull Context context) {
        super(context);
       binding = DialogPermissionXiaomiBinding.inflate(LayoutInflater.from(context));

        // Bỏ tiêu đề của Dialog
      //  Objects.requireNonNull(getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        // Set content view là view từ binding
        setContentView(binding.getRoot());
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        getWindow().setLayout((int) (displayMetrics.widthPixels - context.getResources().getDimension(com.intuit.sdp.R.dimen._20sdp)), ViewGroup.LayoutParams.WRAP_CONTENT);
        setCancelable(false);
        binding.txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        binding.txtOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onOkClicked();
                }
                dismiss(); //
            }
        });

    }
    public interface DialogPermissionXiaomiListener {
        void onOkClicked();     // Gọi khi nút Ok được nhấn
        void onDialogDismissed(); // Gọi khi dialog bị dismiss
    }
    @Override
    public void dismiss() {
        super.dismiss();
        if (listener != null) {
            listener.onDialogDismissed();
        }
    }
}
