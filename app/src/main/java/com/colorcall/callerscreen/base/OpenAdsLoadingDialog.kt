package com.colorcall.callerscreen.base

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import com.colorcall.callerscreen.R
import com.colorcall.callerscreen.databinding.DialogLoadingOpenAdsBinding

class OpenAdsLoadingDialog(
    private val activity: AppCompatActivity
) : Dialog(activity, R.style.FullScreenDialog) {

    private lateinit var binding: DialogLoadingOpenAdsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        //binding = DialogLoadingOpenAdsBinding.inflate(activity.layoutInflater)
        //setContentView(binding.root)
        setupWindow()
        initView()
    }

    private fun setupWindow() {
        window?.apply {
            WindowCompat.setDecorFitsSystemWindows(this, true)
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }
    }

    private fun initView() {
        setCancelable(true)
        setCanceledOnTouchOutside(false)
    }

}
