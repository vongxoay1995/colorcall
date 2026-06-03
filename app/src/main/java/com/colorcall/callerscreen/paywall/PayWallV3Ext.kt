package com.colorcall.callerscreen.paywall

import android.app.Activity
import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.net.Uri
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.colorcall.callerscreen.R
import com.colorcall.callerscreen.constan.Constant
import com.colorcall.callerscreen.main.MainActivity
import com.colorcall.callerscreen.utils.billing.BillingHelper
import com.colorcall.callerscreen.utils.billing.BillingListener

// ─── Plan constants ─────────────────────────────────────────────────────────────
private const val PLAN_WEEKLY_V3  = 0
private const val PLAN_MONTHLY_V3 = 1
private const val PLAN_YEARLY_V3  = 2

// ══════════════════════════════════════════════════════════════════════════════
// Entry point
// ══════════════════════════════════════════════════════════════════════════════
fun PayWallV3Activity.setupViewV3() {
    val fromScr = intent.getStringExtra("from_scr") ?: ""
    when (fromScr) {
        "FeatureGate", "Splash" -> {
            mBinding.icCloseV3.visibility = View.INVISIBLE
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (isActiveV3()) mBinding.icCloseV3.visibility = View.VISIBLE
            }, 3000)
        }
        else -> mBinding.icCloseV3.visibility = View.VISIBLE
    }
    setupFeatureAutoScrollV3()
    initBillingV3()
    setupListenersV3()
}

// ── Gradient text cho title "COLOR FLASH CALL" ────────────────────────────────
// Màu theo Figma: 0%=#FF00C8 (pink) → 50%=#B000FF (purple) → 100%=#00E5FF (cyan)
private fun PayWallV3Activity.applyTitleGradientV3() {
    val tv = mBinding.tvTitleV3
    tv.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            tv.viewTreeObserver.removeOnGlobalLayoutListener(this)
            if (tv.width <= 0) return
            val gradient = LinearGradient(
                0f, 0f, tv.width.toFloat(), 0f,
                intArrayOf(
                    0xFFFF00C8.toInt(),  // 0%   — pink
                    0xFFB000FF.toInt(),  // 50%  — purple
                    0xFF00E5FF.toInt()   // 100% — cyan
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            tv.paint.shader = gradient
            tv.invalidate()
        }
    })
}

// ══════════════════════════════════════════════════════════════════════════════
// Billing
// ══════════════════════════════════════════════════════════════════════════════
fun PayWallV3Activity.initBillingV3() {
    billingHelper = BillingHelper(this).apply {
        init()
        setListener(object : BillingListener {
            override fun onPurchaseUpdatedV5Below(list: List<SkuDetails?>?) {}

            override fun onPurchaseUpdatedV5(list: List<Purchase?>?) {
                if (list != null && !isBought) {
                    val bought = list.find { it?.purchaseState == Purchase.PurchaseState.PURCHASED }
                    if (bought != null) {
                        isBought = true
                        analystic.trackEvent("PayWallV3_buy_success")
                        if (isActiveV3()) {
                            runOnUiThread { handlePurchaseSuccessV3() }
                        }
                    }
                }
            }

            override fun onUserCanceled() {}

            override fun setupBillingDone() {
                if (!isBought) {
                    runOnUiThread {
                        mBinding.layoutLoadingV3.visibility = View.GONE
                        mBinding.scrollIapV3.visibility = View.VISIBLE
                        loadPricesV3()
                        selectPlanV3(PLAN_YEARLY_V3)
                        applyTitleGradientV3()
                        mBinding.rcvFeaturesV3.startAutoScroll()
                    }
                }
            }

            override fun setupBillingFailed(s: String) {
                Log.e("PayWallV3", "Billing failed: $s")
                runOnUiThread {
                    mBinding.layoutLoadingV3.visibility = View.GONE
                    mBinding.scrollIapV3.visibility = View.VISIBLE
                    selectPlanV3(PLAN_YEARLY_V3)
                    applyTitleGradientV3()
                    mBinding.rcvFeaturesV3.startAutoScroll()
                }
            }
        })
    }
}

private fun PayWallV3Activity.handlePurchaseSuccessV3() {
    Toast.makeText(this, getString(R.string.pw_v3_purchase_success), Toast.LENGTH_SHORT).show()
    if (intent.getStringExtra("from_scr") == "Splash") {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    } else {
        setResult(Activity.RESULT_OK)
        finish()
    }
}

private fun PayWallV3Activity.loadPricesV3() {
    billingHelper.getProductDetails().forEach { product ->
        when (product.productId) {
            Constant.WEEK_LY -> {
                product.subscriptionOfferDetails?.forEach { sub ->
                    val firstPhase = sub.pricingPhases.pricingPhaseList[0]
                    if (sub.offerId != null && firstPhase.priceAmountMicros == 0L) {
                        hasFreeTrial = true
                        offerIdIap = sub.offerId!!
                    } else {
                        val price = sub.pricingPhases.pricingPhaseList.last().formattedPrice
                        if (price.isNotEmpty()) {
                            priceWeekly = price
                            mBinding.tvPriceWeeklyV3.text = price
                        }
                    }
                }
            }

            Constant.MONTH_LY -> {
                product.subscriptionOfferDetails?.forEach { sub ->
                    val price = sub.pricingPhases.pricingPhaseList.last().formattedPrice
                    if (price.isNotEmpty()) {
                        priceMonthly = price
                        mBinding.tvPriceMonthlyV3.text = price
                    }
                }
            }

            Constant.YEAR_LY -> {
                product.subscriptionOfferDetails?.forEach { sub ->
                    val price = sub.pricingPhases.pricingPhaseList.last().formattedPrice
                    if (price.isNotEmpty()) {
                        priceYearly = price
                        mBinding.tvPriceYearlyV3.text = price
                    }
                }
            }
        }
    }
    updateCtaTextV3()
}

// ══════════════════════════════════════════════════════════════════════════════
// Plan selection UI
// ══════════════════════════════════════════════════════════════════════════════
fun PayWallV3Activity.selectPlanV3(plan: Int) {
    selectedPlan = plan
    with(mBinding) {
        val unselectedBg = resources.getDrawable(R.drawable.bg_plan_unselected_v3, null)
        val selectedBg   = resources.getDrawable(R.drawable.bg_plan_selected_v3, null)
        val radioOff     = resources.getDrawable(R.drawable.bg_radio_unselected_v3, null)
        val radioOn      = resources.getDrawable(R.drawable.bg_radio_selected_v3, null)

        // Reset all
        layoutPlanWeeklyV3.background  = unselectedBg
        layoutPlanMonthlyV3.background = unselectedBg
        layoutPlanYearlyV3.background  = unselectedBg
        radioWeeklyV3.setImageDrawable(radioOff)
        radioMonthlyV3.setImageDrawable(radioOff)
        radioYearlyV3.setImageDrawable(radioOff)

        // Highlight selected
        when (plan) {
            PLAN_WEEKLY_V3  -> { layoutPlanWeeklyV3.background  = selectedBg; radioWeeklyV3.setImageDrawable(radioOn) }
            PLAN_MONTHLY_V3 -> { layoutPlanMonthlyV3.background = selectedBg; radioMonthlyV3.setImageDrawable(radioOn) }
            PLAN_YEARLY_V3  -> { layoutPlanYearlyV3.background  = selectedBg; radioYearlyV3.setImageDrawable(radioOn) }
        }
    }
    updateCtaTextV3()
}

private fun PayWallV3Activity.updateCtaTextV3() {
    mBinding.txtContinueV3.text = when (selectedPlan) {
        PLAN_WEEKLY_V3  -> if (hasFreeTrial) getString(R.string.pw_v3_cta_trial) else getString(R.string.pw_v3_cta_weekly)
        PLAN_MONTHLY_V3 -> getString(R.string.pw_v3_cta)
        PLAN_YEARLY_V3  -> getString(R.string.pw_v3_cta)
        else            -> getString(R.string.pw_v3_cta)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Listeners
// ══════════════════════════════════════════════════════════════════════════════
fun PayWallV3Activity.setupListenersV3() {
    with(mBinding) {
        layoutPlanWeeklyV3.setOnClickListener  { selectPlanV3(PLAN_WEEKLY_V3)  }
        layoutPlanMonthlyV3.setOnClickListener { selectPlanV3(PLAN_MONTHLY_V3) }
        layoutPlanYearlyV3.setOnClickListener  { selectPlanV3(PLAN_YEARLY_V3)  }

        layoutContinueV3.setOnClickListener { buyNowV3() }

        icCloseV3.setOnClickListener {
            analystic.trackEvent("PayWallV3_Close_Clicked")
            closeOrNavigateV3()
        }

        tvTermsV3.paintFlags = tvTermsV3.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
        tvPolicyV3.paintFlags = tvPolicyV3.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
        tvTermsV3.setOnClickListener   { openUrlV3(Constant.TERMS_URL)  }
        tvPolicyV3.setOnClickListener  { openUrlV3(Constant.POLICY_URL) }
    }

    // Setup spannable Terms note giống Paywall V1
    setupTermsSpannableV3()

    onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            analystic.trackEvent("PayWallV3_Back_Pressed")
            closeOrNavigateV3()
        }
    })
}

// ══════════════════════════════════════════════════════════════════════════════
// Purchase
// ══════════════════════════════════════════════════════════════════════════════
fun PayWallV3Activity.buyNowV3() {
    analystic.trackEvent("PayWallV3_buy_clicked_plan_$selectedPlan")
    when (selectedPlan) {
        PLAN_WEEKLY_V3 -> {
            val offerId = if (hasFreeTrial) offerIdIap else null
            billingHelper.launchPurchaseSubFlow(
                this,
                BillingClient.ProductType.SUBS,
                Constant.WEEK_LY,
                offerId
            )
        }
        PLAN_MONTHLY_V3 -> {
            billingHelper.launchPurchaseSubFlow(
                this,
                BillingClient.ProductType.SUBS,
                Constant.MONTH_LY,
                null
            )
        }
        PLAN_YEARLY_V3 -> {
            billingHelper.launchPurchaseSubFlow(
                this,
                BillingClient.ProductType.SUBS,
                Constant.YEAR_LY,
                null
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Helpers
// ══════════════════════════════════════════════════════════════════════════════
fun PayWallV3Activity.isActiveV3(): Boolean = !isFinishing && !isDestroyed

private fun PayWallV3Activity.closeOrNavigateV3() {
    if (intent.getStringExtra("from_scr") == "Splash") {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    } else {
        finish()
    }
}

private fun PayWallV3Activity.openUrlV3(url: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Auto-scroll marquee features
// ══════════════════════════════════════════════════════════════════════════════
private fun PayWallV3Activity.setupFeatureAutoScrollV3() {
    val features = listOf(
        PaywallV3Feature(getString(R.string.pw_v3_feat_no_ads), R.drawable.ic_ad),
        PaywallV3Feature(getString(R.string.pw_v3_feat_faster), R.drawable.ic_time),
        PaywallV3Feature(getString(R.string.pw_v3_feat_unlimited), R.drawable.ic_unlimited)
    )

    mBinding.rcvFeaturesV3.setAdapter(PaywallV3FeatureAdapter(features))
    mBinding.rcvFeaturesV3.isLoopEnabled = true
    mBinding.rcvFeaturesV3.startAutoScroll()
}

fun PayWallV3Activity.stopFeatureAutoScrollV3() {
    mBinding.rcvFeaturesV3.pauseAutoScroll(true)
}

// ══════════════════════════════════════════════════════════════════════════════
// Terms spannable (giống Paywall V1)
// ══════════════════════════════════════════════════════════════════════════════
private fun PayWallV3Activity.setupTermsSpannableV3() {
    with(mBinding.tvTermsNoteV3) {
        val policy = getString(R.string.policy)
        val subscriptionTerm = getString(R.string.terms)
        val text = getString(R.string.term_paywall, subscriptionTerm, policy)
        var spannable = SpannableString(text)

        // Policy link
        val policyStart = text.indexOf(policy)
        if (policyStart >= 0) {
            val clickPolicy = WordClickableSpan2(true).apply { setPolicy(true) }
            spannable.setSpan(clickPolicy, policyStart, policyStart + policy.length, 0)
        }

        // Terms link
        val termsStart = text.indexOf(subscriptionTerm)
        if (termsStart >= 0) {
            val clickTerms = WordClickableSpan2(true).apply { setPolicy(false) }
            spannable.setSpan(clickTerms, termsStart, termsStart + subscriptionTerm.length, 0)
        }

        this.text = spannable
        movementMethod = LinkMovementMethod.getInstance()
        // Xoá highlight màu nền khi click
        highlightColor = 0x00000000
    }
}
