package com.colorcall.callerscreen.paywall

import android.annotation.SuppressLint
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.colorcall.callerscreen.R
import com.colorcall.callerscreen.constan.Constant
import com.colorcall.callerscreen.utils.billing.BillingHelper
import com.colorcall.callerscreen.utils.billing.BillingListener

fun PayWallActivity.setupView() {
    setupWordSpannable()
    initBilling()
    listener()
}

fun PayWallActivity.listener() {
    with(mBinding) {
        swEnableTrial.setOnCheckedChangeListener { _, isChecked ->
            analystic.trackEvent("Paywall_Switch_Clicked")
            switchProduct(if (isChecked) 0 else 1)
        }
        layoutBuyNow.setOnClickListener { buyNow() }
        icClose.setOnClickListener { finish() }
    }
}

fun PayWallActivity.initBilling() {
    billingHelper = BillingHelper(this).apply {
        init()
        setListener(object : BillingListener {
            override fun onPurchaseUpdatedV5Below(list: List<SkuDetails?>?) {}
            override fun onPurchaseUpdatedV5(list: List<Purchase?>?) {

            }
            override fun onUserCanceled() {}

            override fun setupBillingDone() {
                runOnUiThread {
                    with(mBinding) {
                        layoutLoading.visibility = View.GONE
                        scrollIap.visibility = View.VISIBLE
                    }
                    processProductDetails()
                    setPrice()
                }
            }

            override fun setupBillingFailed(s: String) {
                Log.e("Billing", "Setup failed: $s")
            }
        })
    }
}

private fun PayWallActivity.processProductDetails() {
    billingHelper.getProductDetails().forEach { product ->
        if (product.productId == Constant.WEEK_LY) {
            product.subscriptionOfferDetails?.forEach { sub ->
                with(sub) {
                    if (offerId != null && pricingPhases.pricingPhaseList[0].priceAmountMicros == 0L) {
                        hasFreeTrial = true
                        offerIdIap = offerId!!
                    } else {
                        priceP1 = pricingPhases.pricingPhaseList[0].formattedPrice
                    }
                }
            }
        }
    }
}

fun PayWallActivity.setPrice() {
    with(mBinding) {
        if (hasFreeTrial) {
            switchProduct(0)
            content1.text = getString(R.string.free_trial_for_3_days_no_payment_now)
            txtBuyNow.text = getString(R.string.start_free_trial_now)
            tvDayFree.visibility = View.VISIBLE
            tvPrice.text = priceP1
            tvPriceSave.text = formatToZero(priceP1)
            offerIdTemp = offerIdIap
        } else {
            layoutToggle.visibility = View.GONE
            content1.text = getString(R.string.unleadsh_your_limit)
            txtBuyNow.text = getString(R.string.continues)
            tvDayFree.visibility = View.GONE
            tvPrice.text = priceP1
            tvPriceSave.text = priceP1
            offerIdTemp = ""
        }
    }
}

fun PayWallActivity.buyNow() {
    billingHelper.launchPurchaseSubFlow(
        this,
        BillingClient.ProductType.SUBS,
        Constant.WEEK_LY,
        offerIdTemp.takeIf { it.isNotEmpty() }
    )
}

fun PayWallActivity.setupWordSpannable() {
    with(mBinding.terms) {
        val policy = getString(R.string.policy)
        val subscriptionTerm = getString(R.string.terms)
        val text = getString(R.string.term_paywall, subscriptionTerm, policy)
        var spannable = SpannableString(text)

        spannable = getWorkSpannable(spannable, text, policy, true)
        spannable = getWorkSpannable(spannable, text, subscriptionTerm, false)

        this.text = spannable
        movementMethod = LinkMovementMethod.getInstance()
    }
}

fun PayWallActivity.getWorkSpannable(
    spannable: SpannableString,
    text: String,
    target: String,
    isPolicy: Boolean
): SpannableString {
    val startIndex = text.indexOf(target)
    val endIndex = startIndex + target.length
    val clickableSpan = WordClickableSpan2(true).apply { setPolicy(isPolicy) }
    spannable.setSpan(clickableSpan, startIndex, endIndex, 0)
    return spannable
}

@SuppressLint("SetTextI18n")
fun PayWallActivity.switchProduct(position: Int) {
    with(mBinding) {
        when (position) {
            0 -> {
                swEnableTrial.isChecked = true
                layoutPrice.apply {
                    tvToday.text = getString(R.string.today)
                    tvDayFree.text = getString(R.string.s_days_free, "3")
                    tvPurchaseDay.text = "${getString(R.string.until)} $next3DayFormat"
                    tvPriceSave.apply {
                        visibility = View.VISIBLE
                        text = if (priceP1 == loadingText) "" else formatToZero(priceP1)
                    }
                    tvPrice.text = priceP1
                    content1.text = getString(R.string.free_trial_for_3_days_no_payment_now)
                    txtBuyNow.text = getString(R.string.start_free_trial_now)
                }
                offerIdTemp = offerIdIap
                content3.text = getString(R.string.free_for_first_3_days_then_s_week, "3", priceP1)
            }
            1 -> {
                swEnableTrial.isChecked = false
                offerIdTemp=""
                layoutPrice.apply {
                    tvToday.text = getString(R.string.today)
                    tvDayFree.visibility = View.GONE
                    tvPurchaseDay.text = "${getString(R.string.until)} $next7DayFormat"
                    tvPriceSave.apply {
                        visibility = View.VISIBLE
                        text = priceP1
                    }
                    tvPrice.text = priceP1
                    content1.text = getString(R.string.unleadsh_your_limit)
                    txtBuyNow.text = getString(R.string.continues)
                }
                content3.text = getString(R.string.only_s_per_week, priceP1)
            }
        }
    }
}

fun PayWallActivity.formatToZero(price: String): String =
    price.replace(Regex("\\d+(\\.\\d+)?"), "0")