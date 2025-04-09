package com.colorcall.callerscreen.paywall

import android.annotation.SuppressLint
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.view.View
import com.colorcall.callerscreen.R

fun PayWallActivity.setupView() {
    setupWordSpannable()
}
fun PayWallActivity.listener(){
    mBinding.apply {
        swEnableTrial.setOnCheckedChangeListener { _, isChecked ->
            analystic.trackEvent("Paywall_Switch_Clicked")
            switchProduct(if (isChecked) 0 else 1)
        }
        layoutBuyNow.setOnClickListener {
            buyNow()
        }
        icClose.setOnClickListener {finish() }
    }
}
fun PayWallActivity.buyNow() {

}
fun PayWallActivity.setupWordSpannable() {
    val policy: String = getString(R.string.policy)
    val subscriptionTerm: String = getString(R.string.terms)
    val text: String = getString(R.string.term_paywall, subscriptionTerm, policy)
    var spannable = SpannableString(text)
    spannable = getWorkSpannable(spannable, text, policy, true)
    spannable = getWorkSpannable(spannable, text, subscriptionTerm, false)
    mBinding.terms.text = spannable
    mBinding.terms.movementMethod = LinkMovementMethod.getInstance()
}
fun PayWallActivity.getWorkSpannable(
    spannable: SpannableString,
    text: String,
    policy: String,
    isPolicy: Boolean
): SpannableString {
    val startIndex = text.indexOf(policy)
    val endIndex = startIndex + policy.length
    val clickableSpan = WordClickableSpan2(true)
    clickableSpan.setPolicy(isPolicy)
    spannable.setSpan(clickableSpan, startIndex, endIndex, 0)
    return spannable
}
@SuppressLint("SetTextI18n")
fun PayWallActivity.switchProduct(position: Int) {
    mBinding.apply {
        when (position) {
            0 -> {
                swEnableTrial.isChecked = true
                layoutPrice.apply {
                    tvToday.text = getString(R.string.today)
                    tvDayFree.text = getString(R.string.s_days_free, "3")
                    tvPurchaseDay.text = getString(R.string.until)+" "+next3DayFormat
                    tvPriceSave.visibility = View.VISIBLE
                    tvPriceSave.text = if (priceP1 == loadingText) "" else formatToZero(priceP1)
                    tvPrice.text = priceP1
                }
                content3.text = getString(R.string.free_for_first_3_days_then_s_week, "3", priceP1)
                txtBuyNow.text = getString(R.string.start_free_trial_now)
            }
            1 -> {
                swEnableTrial.isChecked = false
                layoutPrice.apply {
                    tvToday.text = getString(R.string.today)
                    tvDayFree.visibility = View.GONE
                    tvPurchaseDay.text = getString(R.string.until)+" "+next7DayFormat
                    tvPriceSave.visibility = View.VISIBLE
                    tvPriceSave.text = priceP1
                    tvPrice.text = priceP1
                }
                content3.text = getString(R.string.only_s_per_week, priceP1)
                txtBuyNow.text = getString(R.string.start_free_trial_now)
            }
        }
    }
}
fun formatToZero(price: String): String {
    val regex = Regex("\\d+(\\.\\d+)?")
    return price.replace(regex, "0")
}