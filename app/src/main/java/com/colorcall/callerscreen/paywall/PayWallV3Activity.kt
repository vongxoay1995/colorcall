package com.colorcall.callerscreen.paywall

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.colorcall.callerscreen.analystic.Analystic
import com.colorcall.callerscreen.databinding.ActivityPayWallV3Binding
import com.colorcall.callerscreen.utils.billing.BillingHelper

class PayWallV3Activity : AppCompatActivity() {

    lateinit var mBinding: ActivityPayWallV3Binding
    val analystic: Analystic by lazy { Analystic.getInstance(this) }
    lateinit var billingHelper: BillingHelper

    // Pricing strings loaded from billing
    var priceWeekly = ""
    var priceMonthly = ""
    var priceYearly = ""

    // Currently selected plan: 0=weekly, 1=monthly, 2=yearly
    var selectedPlan = 2 // default: yearly (best value)

    // Offer token for free trial
    var offerIdIap = ""
    var hasFreeTrial = false
    var isBought = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityPayWallV3Binding.inflate(layoutInflater)
        setContentView(mBinding.root)
        analystic.trackEvent("PayWallV3_Show")
        setupViewV3()
    }

    override fun onDestroy() {
        stopFeatureAutoScrollV3()
        if (::billingHelper.isInitialized) billingHelper.destroy()
        super.onDestroy()
    }
}
