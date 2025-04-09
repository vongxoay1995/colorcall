package com.colorcall.callerscreen.paywall

import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.colorcall.callerscreen.R
import com.colorcall.callerscreen.analystic.Analystic
import com.colorcall.callerscreen.databinding.ActivityOnboardingBinding
import com.colorcall.callerscreen.databinding.ActivityPayWallBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PayWallActivity : AppCompatActivity() {
    lateinit var mBinding: ActivityPayWallBinding
    val analystic:Analystic by lazy {
        Analystic.getInstance(this)
    }
    var hasFreeTrial: Boolean = false
    var priceP1 = ""
    var priceP2 = ""
    var savePercent = 0.0
    val loadingText by lazy {
        ContextCompat.getString(this, R.string.loading)
    }
    val next3DayFormat: String by lazy {
        SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
            .format(Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 3)
            }.time)
    }
    val next7DayFormat: String by lazy {
        SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
            .format(Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 7)
            }.time)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityPayWallBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        analystic.trackEvent("PayWallShow")
        setupView()
        listener()
    }

}