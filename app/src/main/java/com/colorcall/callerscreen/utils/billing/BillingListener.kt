package com.colorcall.callerscreen.utils.billing

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails

interface BillingListener {
    fun setupBillingDone()
    fun onUserCanceled()
    fun setupBillingFailed(s: String)
    fun onPurchaseUpdatedV5(list: List<Purchase?>?)
    fun onPurchaseUpdatedV5Below(list: List<SkuDetails?>?)
}