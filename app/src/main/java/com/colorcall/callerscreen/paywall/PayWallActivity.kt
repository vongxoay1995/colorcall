package com.colorcall.callerscreen.paywall

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.colorcall.callerscreen.R
import com.colorcall.callerscreen.analystic.Analystic
import com.colorcall.callerscreen.databinding.ActivityPayWallBinding
import com.colorcall.callerscreen.utils.AppUtils
import com.colorcall.callerscreen.utils.billing.BillingHelper
import com.google.android.gms.ads.interstitial.InterstitialAd
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PayWallActivity : AppCompatActivity() {
    lateinit var mBinding: ActivityPayWallBinding
    val analystic:Analystic by lazy {
        Analystic.getInstance(this)
    }
    lateinit var billingHelper:BillingHelper
    var hasFreeTrial: Boolean = false
    var priceP1 = ""
    var offerIdIap = ""
    var offerIdTemp = ""
    var is_just_bought = false
    var isGetTempValue = false
    var countSizeList = 0
    var mInterstitialAd: InterstitialAd? = null

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
        AppUtils.setFullNav(this)
        analystic.trackEvent("PayWallShow")
        setupView()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) AppUtils.setFullNav(this)
    }

    override fun onDestroy() {
        if (::billingHelper.isInitialized) billingHelper.destroy()
        super.onDestroy()
    }
}
