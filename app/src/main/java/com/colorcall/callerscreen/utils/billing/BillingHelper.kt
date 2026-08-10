package com.colorcall.callerscreen.utils.billing

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.colorcall.callerscreen.constan.Constant
import com.colorcall.callerscreen.utils.AppHelper
import com.colorcall.callerscreen.utils.HawkHelper

class BillingHelper(private val context: Context) {
    private var billingClient: BillingClient? = null

    private val productDetails: MutableList<ProductDetails> = ArrayList()
    private val purchases: MutableList<Purchase> = ArrayList()

    private var isQuerySubDone = false
    private var isQueryInAppDone = false
    private var isQueryPurchaseDone = false
    private var listener: BillingListener? = null
    private var billingStatus: BillingStatus = BillingStatus.NOT_YET_CONNECTED

    fun init() {
        billingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases(
                com.android.billingclient.api.PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .enableAutoServiceReconnection()
            .setListener { billingResult: BillingResult, list: List<Purchase>? ->
                Log.e("TAN", "init: "+billingResult.responseCode )
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    if (list != null) {
                        for (purchase in list) {
                            verifyPurchase(purchase)
                        }
                    }
                } else if(billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                    listener?.onUserCanceled()
                }
            }
            .build()
        establishConnection()
    }

    fun verifyPurchase(purchases: Purchase) {
        if (!purchases.isAcknowledged) {
            billingClient!!.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchases.purchaseToken).build()
            ) { billingResult: BillingResult? ->
                queryPurchase()
            }
        }
    }

    fun establishConnection() {
        Log.e("TAN", "establishConnection: ")
        billingStatus = BillingStatus.NOT_YET_CONNECTED
        if (billingClient!!.isReady) return
        try {
            billingClient!!.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    billingStatus = BillingStatus.DISCONNECTED
                }

                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (!AppHelper.isInitBilling) AppHelper.isInitBilling = true
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        productDetails.clear()
                        isQuerySubDone = false
                        isQueryInAppDone = false
                        isQueryPurchaseDone = false
                        queryIap()
                        queryPurchase()
                    } else {
                        listener?.setupBillingFailed("Ma loi la "+billingResult.responseCode)
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("TAN", "establishConnection: "+e.message )
        }
    }

    private fun querySubscription() {
        val products = ArrayList<QueryProductDetailsParams.Product>()
        val productId: List<String> = mutableListOf(Constant.WEEK_LY, Constant.MONTH_LY, Constant.YEAR_LY)
        for (s in productId) {
            val product = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(s)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
            products.add(product)
        }
        val productListInApp = ArrayList(products)
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productListInApp)
            .build()
        billingClient!!.queryProductDetailsAsync(
            params
        ) { billingResult, result ->
            // Process the result
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.e("TAN", "querySubscription: 111")
                isQuerySubDone = true
                productDetails.addAll(result.productDetailsList)
                checkSetupDone()
            } else {
                listener?.setupBillingFailed("Subscription products: ${billingResult.debugMessage}")
            }
        }
    }

    private fun queryIap() {
        querySubscription()
        queryInApp()
    }

    private fun queryInApp() {
        val products = ArrayList<QueryProductDetailsParams.Product>()
        val productId: List<String> = mutableListOf(Constant.LIFE_TIME)
        for (s in productId) {
            val product = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(s)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
            products.add(product)
        }
        val productListInApp = ArrayList(products)
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productListInApp)
            .build()
        billingClient?.queryProductDetailsAsync(
            params
        ) { billingResult, result ->
            // Process the result
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                isQueryInAppDone = true
                productDetails.addAll(result.productDetailsList)
                Log.e("TAN", "queryInApp: 2222", )
                checkSetupDone()
            } else {
                listener?.setupBillingFailed("One-time products: ${billingResult.debugMessage}")
            }
        }
    }

    var countResult: Int = 0

    private fun queryPurchase() {
        Log.e("TAN", "queryPurchase: "+productDetails+"##\n"+billingClient )
        purchases.clear()
        val purchase = BooleanArray(2)
        countResult = 0
        var params: QueryPurchasesParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient!!.queryPurchasesAsync(params) { billingResult: BillingResult?, list: List<Purchase>? ->
            Log.e("TAN", "queryPurchase: list in app da mua "+list )
            purchases.addAll(
                list!!
            )

            purchase[0] = true
            isQueryPurchaseDone = purchase[0] && purchase[1]
            countResult++
            if (countResult == 2) {
                checkBuyIap()
                listener?.onPurchaseUpdatedV5(purchases)
            }
            checkSetupDone()
        }
        params =
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        billingClient!!.queryPurchasesAsync(params) { billingResult: BillingResult?, list: List<Purchase>? ->
            Log.e("TAN", "queryPurchase: list sub da mua"+list )
            purchases.addAll(
                list!!
            )
            purchase[1] = true
            isQueryPurchaseDone = purchase[0] && purchase[1]
            //
            countResult++
            if (countResult == 2) {
                checkBuyIap()
                listener?.onPurchaseUpdatedV5(purchases)
            }
            checkSetupDone()
        }
    }

    private fun checkBuyIap() {
        HawkHelper.setPay(purchases.size>0)
    }

    @Synchronized
    private fun checkSetupDone() {
        if (isQuerySubDone && isQueryInAppDone && isQueryPurchaseDone) {
            billingStatus = BillingStatus.CONNECTED
            listener?.setupBillingDone()
            HawkHelper.setPay(purchases.isNotEmpty())
        }
    }

    fun setListener(listener: BillingListener?) {
        this.listener = listener
    }

    private fun launchPurchaseInAppFlow(activity: Activity, productDetails: ProductDetails) {
        val productDetailsParamsList = listOf(
            ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient!!.launchBillingFlow(activity, billingFlowParams)
    }

    private fun launchPurchaseSubFlow(
        activity: Activity,
        productDetails: ProductDetails,
        dynamicProductId: String?
    ) {
        val list = productDetails.subscriptionOfferDetails
        checkNotNull(productDetails.subscriptionOfferDetails)
        var offerToken = ""
        for (details in list!!) {
            if ((dynamicProductId == null && details.offerId == null) || (dynamicProductId != null && dynamicProductId == details.offerId)) {
                offerToken = details.offerToken
                break
            }
        }
        val productDetailsParamsList = listOf(
            ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )
        Log.e("TAN", "launchPurchaseSubFlow: $offerToken##$dynamicProductId")
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        billingClient!!.launchBillingFlow(activity, billingFlowParams)
    }

    fun launchPurchaseSubFlow(
        activity: Activity,
        type: String,
        productDetailsId: String,
        dynamicProductId: String?
    ) {
        Log.e(
            "TAN",
            "launchPurchaseSubFlow: 000##" + dynamicProductId
        )
       // fakeBoughtIap()
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(productDetailsId)
            .setProductType(type)
            .build()
        val productListInApp = arrayListOf(product)
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productListInApp)
            .build()
        billingClient!!.queryProductDetailsAsync(
            params
        ) { billingResult, result ->
            // Process the result
            val details = result.productDetailsList.firstOrNull()
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && details != null) {
                if (type == BillingClient.ProductType.SUBS) {
                    launchPurchaseSubFlow(activity, details, dynamicProductId)
                } else if (type == BillingClient.ProductType.INAPP) {
                    launchPurchaseInAppFlow(activity, details)
                }
            } else {
                listener?.setupBillingFailed("Purchase product: ${billingResult.debugMessage}")
            }
        }
    }

    fun consumePurchased(purchase: Purchase) {
        val consumeParams =
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

        val listener =
            ConsumeResponseListener { billingResult, purchaseToken ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.e("HAI", "onConsumeResponse: $billingResult")
                    // Handle the success of the consume operation.
                }
            }
        billingClient!!.consumeAsync(consumeParams, listener)
    }

    fun getProductDetails(): List<ProductDetails> {
        return productDetails
    }

    fun getBillingStatus(): BillingStatus {
        return billingStatus
    }

    fun getPurchases(): List<Purchase> {
        return purchases
    }

    /*  companion object {
          private var instance: com.colorcall.callerscreen.utils.billing.BillingHelper? = null
          fun getInstance(context: Context): com.colorcall.callerscreen.utils.billing.BillingHelper? {
              if (instance == null) {
                  instance = com.colorcall.callerscreen.utils.billing.BillingHelper(context)
              }
              return instance
          }
      }*/
    fun destroy() {
        try {
            billingClient?.endConnection()
            billingClient = null
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun fakeBoughtIap() {
        if (listener != null) {
            Handler().postDelayed({
                HawkHelper.setPay(true)
               purchases.add(Purchase("{\"orderId\":\"GPA.3322-6123-2845-14223284\",\"packageName\":\"com.colorcall.callerscreen\",\"productId\":\"weekly399_1st_saleoff\",\"purchaseTime\":1733818993940,\"purchaseState\":0,\"purchaseToken\":\"fmbaacehncggolklmlnegbhg.AO-J1OwIzu_ASJnGmezJ2FU481vFwE4P0ZsTyTAF09x_xyw66eM5KOoLrdkJ_Gz_tNkk6fYfxs5S6bh14wSqaHAzXM0tFR82Hw\",\"quantity\":1,\"autoRenewing\":true,\"acknowledged\":true}",""))
                listener?.onPurchaseUpdatedV5(purchases)
            }, 1500)
            Handler().postDelayed({
                HawkHelper.setPay(false)
            }, 30000)
        }
    }

}
