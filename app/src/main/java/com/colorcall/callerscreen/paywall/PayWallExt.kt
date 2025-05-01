package com.colorcall.callerscreen.paywall

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.colorcall.callerscreen.BuildConfig
import com.colorcall.callerscreen.R
import com.colorcall.callerscreen.constan.Constant
import com.colorcall.callerscreen.main.MainActivity
import com.colorcall.callerscreen.utils.ConstantAds
import com.colorcall.callerscreen.utils.HawkHelper
import com.colorcall.callerscreen.utils.billing.BillingHelper
import com.colorcall.callerscreen.utils.billing.BillingListener
import com.colorcall.callerscreen.utils.shineAnimationView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

fun PayWallActivity.setupView() {
    if (intent.getStringExtra("from_scr") == "Splash" && !HawkHelper.isPayed()) {
        loadInterAds()
    }
    setupWordSpannable()
    initBilling()
    listener()
    mBinding.layoutBuyNow.shineAnimationView()
}

fun PayWallActivity.loadInterAds() {
    val idInter = if (BuildConfig.DEBUG) {
        Constant.ID_INTER_TEST
    } else {
        ConstantAds.interPaywall
    }
    //idInter = ID_ADS;
    val adRequest = AdRequest.Builder().build()
    InterstitialAd.load(
        this, idInter, adRequest,
        object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
                mInterstitialAd?.setFullScreenContentCallback(object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        moveMain()
                        Log.e("TAG", "The ad was dismissed.")
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        // Called when fullscreen content failed to show.
                        moveMain()
                        Log.d("TAG", "The ad failed to show.")
                    }

                    override fun onAdShowedFullScreenContent() {
                        // Called when fullscreen content is shown.
                        // Make sure to set your reference to null so you don't
                        // show it a second time.
                        mInterstitialAd = null
                        Log.e("TAG", "The ad was shown.")
                    }
                })
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // Handle the error
                mInterstitialAd = null
            }
        })
}

fun PayWallActivity.moveMain() {
    startActivity(Intent(this, MainActivity::class.java))
    finish()
}

fun PayWallActivity.listener() {
    with(mBinding) {
        swEnableTrial.setOnCheckedChangeListener { _, isChecked ->
            analystic.trackEvent("Paywall_Switch_Clicked")
            switchProduct(if (isChecked) 0 else 1)
        }
        layoutBuyNow.setOnClickListener { buyNow() }
        icClose.setOnClickListener {
            analystic.trackEvent("PayWallScr_Close_Clicked")
            if (intent.getStringExtra("from_scr")=="Splash"){
                showInterAds()
            }else finish()
        }
    }
    onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(intent.getStringExtra("from_scr")=="Splash"){
                    moveMain()
                }else{
                    finish()
                }
            }
        }
    )
}

fun PayWallActivity.showInterAds() {
    if(mInterstitialAd != null){
        mInterstitialAd?.show(this)
    }else{
        moveMain()
    }
}

fun PayWallActivity.isHasActive(): Boolean {
    return !isFinishing && !isDestroyed
}

fun PayWallActivity.initBilling() {
    billingHelper = BillingHelper(this).apply {
        init()
        setListener(object : BillingListener {
            override fun onPurchaseUpdatedV5Below(list: List<SkuDetails?>?) {}
            override fun onPurchaseUpdatedV5(list: List<Purchase?>?) {
                if (list != null) {
                    if (!isGetTempValue) {
                        countSizeList = list.size
                        isGetTempValue = true
                    }
                    if (countSizeList < list.size) {
                        is_just_bought = true
                        analystic.trackEvent("PayWallScr_Buy_Success")
                        if (isHasActive()) {
                            runOnUiThread {
                                Toast.makeText(this@initBilling, "Purchased success!", Toast.LENGTH_SHORT).show()
                                if (intent.getStringExtra("from_scr") == "Splash"){
                                    moveMain()
                                }else{
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }

                            }
                        }
                    }
                }
            }

            override fun onUserCanceled() {}

            override fun setupBillingDone() {
                if (!is_just_bought) {
                    runOnUiThread {
                        with(mBinding) {
                            layoutLoading.visibility = View.GONE
                            scrollIap.visibility = View.VISIBLE
                        }
                        processProductDetails()
                        setPrice()
                    }
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
            txtBuyNow.text = getString(R.string.start_3_days)
            offerIdTemp = offerIdIap
        } else {
            layoutToggle.visibility = View.GONE
            content1.text = getString(R.string.unleadsh_your_limit)
            txtBuyNow.text = getString(R.string.continues)
            offerIdTemp = ""
        }
    }
}

fun PayWallActivity.buyNow() {
    analystic.trackEvent("PayWallScr_Buy_Clicked")
    //billingHelper.fakeBoughtIap()
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
                content1.text = getString(R.string.free_trial_for_3_days_no_payment_now)
                txtBuyNow.text = getString(R.string.start_3_days)
                offerIdTemp = offerIdIap
                content3.text = getString(R.string.free_for_first_3_days_then_s_week, "3", priceP1)
            }

            1 -> {
                swEnableTrial.isChecked = false
                offerIdTemp = ""
                content1.text = getString(R.string.unleadsh_your_limit)
                txtBuyNow.text = getString(R.string.continues)
                content3.text = getString(R.string.only_s_per_week, priceP1)
            }
        }
    }
}

fun PayWallActivity.formatToZero(price: String): String =
    price.replace(Regex("\\d+(\\.\\d+)?"), "0")