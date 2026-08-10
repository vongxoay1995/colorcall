package com.colorcall.callerscreen.utils.billing

import com.android.billingclient.api.Purchase

interface BillingListener {
    fun setupBillingDone()
    fun onUserCanceled()
    fun setupBillingFailed(s: String)
    fun onPurchaseUpdatedV5(list: List<Purchase?>?)
}
