package com.colorcall.callerscreen.main

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.annotation.NonNull
import com.colorcall.callerscreen.databinding.DialogDialerPermissionBinding

class DialogPermissionCall(@NonNull context: Context): Dialog(context) {
    private val binding: DialogDialerPermissionBinding =
        DialogDialerPermissionBinding.inflate(LayoutInflater.from(context))

    var listenerDialer: DialogPermissionDialerListener? = null

    init {
        // Set content view từ binding
        setContentView(binding.root)

        // Thiết lập background trong suốt
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Không cho phép cancel dialog bằng nút back
        setCancelable(true)

        // Xử lý sự kiện nút Cancel
        // Xử lý sự kiện nút OK
        binding.swDialer.setOnClickListener {
            listenerDialer?.onSwDialerClick()
        }
    }

    fun setStateSw(state:Boolean){
        binding.swDialer.isChecked =state
    }

    interface DialogPermissionDialerListener {
        fun onSwDialerClick()
        fun onDialogDismissed()
    }

    override fun dismiss() {
        super.dismiss()
        listenerDialer?.onDialogDismissed()
    }
}