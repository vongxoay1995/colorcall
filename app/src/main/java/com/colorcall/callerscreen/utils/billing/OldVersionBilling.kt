package com.colorcall.callerscreen.utils.billing

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.colorcall.callerscreen.constan.Constant
import com.colorcall.callerscreen.utils.AppHelper


class OldVersionBilling private constructor(private val billingClient: BillingClient) {
    private var context: Context? = null

    private var listener: BillingListener? = null
    private var isQuerySubDone = false
    private var isQueryInAppDone = false
    var countResult: Int = 0

    private var list: MutableList<SkuDetails> = ArrayList()

    fun queryProduct(context: Context?, listener: BillingListener?) {
        this.context = context
        this.listener = listener
        list = ArrayList()
        querySubscription()
        queryInApp()
    }

    private fun querySubscription() {
        val skuList: List<String> = mutableListOf(Constant.WEEK_LY)
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)

        billingClient.querySkuDetailsAsync(
            params.build()
        ) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                isQuerySubDone = true
                list.addAll(skuDetailsList!!)
                countResult++
                if (countResult == 2) {
                    checkBuyIap()
                    listener?.onPurchaseUpdatedV5Below(list)
                }
                checkSetupDone()
            }
        }
    }

    private fun queryInApp() {
        val skuList: List<String> = mutableListOf(Constant.LIFE_TIME)
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)

        billingClient.querySkuDetailsAsync(
            params.build()
        ) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                isQueryInAppDone = true
                skuDetailsList?.let { list.addAll(it) }
                countResult++
                if (countResult == 2) {
                    checkBuyIap()
                    listener?.onPurchaseUpdatedV5Below(list)
                }
                checkSetupDone()
            }
        }
    }

    private fun checkBuyIap() {
        AppHelper.isPurchased = list.size > 0
    }

    private fun checkSetupDone() {
        if (isQuerySubDone && isQueryInAppDone) {
            listener?.setupBillingDone()
        }
    }

    val skuList: List<SkuDetails>
        get() = list

    companion object {
        private var instance: OldVersionBilling? = null
        fun getInstance(client: BillingClient): OldVersionBilling? {
            if (instance == null) {
                instance = OldVersionBilling(client)
            }
            return instance
        }
    }
}