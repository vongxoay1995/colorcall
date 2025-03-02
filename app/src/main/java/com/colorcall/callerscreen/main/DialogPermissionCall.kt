package com.colorcall.callerscreen.main

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.NonNull
import com.colorcall.callerscreen.databinding.DialogDialerPermissionBinding
import com.intuit.sdp.R

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
        val displayMetrics = DisplayMetrics()
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(displayMetrics)
        window!!.setLayout(
            (displayMetrics.widthPixels - context.resources.getDimension(R.dimen._20sdp)).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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