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
import com.android.billingclient.api.SkuDetails
import com.colorcall.callerscreen.constan.Constant
import com.colorcall.callerscreen.utils.AppHelper

class BillingHelper(private val context: Context) {
    private var billingClient: BillingClient? = null

    private val productDetails: MutableList<ProductDetails> = ArrayList()
    private val purchases: MutableList<Purchase> = ArrayList()

    private var isQuerySubDone = false
    private var isQueryInAppDone = false
    private var isQueryPurchaseDone = false
    private var listener: BillingListener? = null
    private var oldVersionBilling: OldVersionBilling? = null
    private var billingStatus: BillingStatus = BillingStatus.NOT_YET_CONNECTED
    var isSupportNewFeature: Boolean = true
        private set

    fun init() {
        billingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases()
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
                        if (billingClient!!.isFeatureSupported(BillingClient.FeatureType.PRODUCT_DETAILS).responseCode != BillingClient.BillingResponseCode.OK) {
                            this@BillingHelper.isSupportNewFeature = false
                            oldVersionBilling = OldVersionBilling.getInstance(billingClient!!)
                            oldVersionBilling?.queryProduct(context, object : BillingListener {
                                override fun setupBillingDone() {
                                    checkSetupDone()
                                }

                                override fun onUserCanceled() {
                                }

                                override fun setupBillingFailed(s:String) {
                                }

                                override fun onPurchaseUpdatedV5(list: List<Purchase?>?) {

                                }

                                override fun onPurchaseUpdatedV5Below(list: List<SkuDetails?>?) {

                                }
                            })
                        } else {
                            this@BillingHelper.isSupportNewFeature = true
                            queryIap()
                        }
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
        val productId: List<String> = mutableListOf(Constant.WEEK_LY)
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
        ) { billingResult: BillingResult, list: List<ProductDetails>? ->
            // Process the result
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.e("TAN", "querySubscription: 111")
                isQuerySubDone = true
                productDetails.addAll(list!!)
                checkSetupDone()
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
        ) { billingResult: BillingResult, list: List<ProductDetails> ->
            // Process the result
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                isQueryInAppDone = true
                productDetails.addAll(list)
                checkSetupDone()
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
            isQueryPurchaseDone = purchase[1]
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
            isQueryPurchaseDone = purchase[0]
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
        AppHelper.isPurchased = purchases.size > 0
    }

    @Synchronized
    private fun checkSetupDone() {
        if (isSupportNewFeature) {
            if (isQuerySubDone && isQueryInAppDone && isQueryPurchaseDone) {
                billingStatus = BillingStatus.CONNECTED
                listener?.setupBillingDone()
                AppHelper.isPurchased = purchases.isNotEmpty()
            }
        } else {
            if (isQueryPurchaseDone) {
                billingStatus = BillingStatus.CONNECTED
                listener?.setupBillingDone()
                AppHelper.isPurchased = purchases.isNotEmpty()
            }
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
            "launchPurchaseSubFlow: 000" + isSupportNewFeature + "##" + dynamicProductId
        )
       // fakeBoughtIap()
        if (isSupportNewFeature) {
            val product = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productDetailsId)
                .setProductType(type)
                .build()
            val productListInApp = ArrayList<QueryProductDetailsParams.Product>()
            productListInApp.add(product)
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productListInApp)
                .build()
            billingClient!!.queryProductDetailsAsync(
                params
            ) { billingResult, list ->
                // Process the result
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    if (type == BillingClient.ProductType.SUBS) {
                        launchPurchaseSubFlow(activity, list[0], dynamicProductId)
                    } else if (type == BillingClient.ProductType.INAPP) {
                        launchPurchaseInAppFlow(activity, list[0])
                    }
                }
            }
        } else {
            for (details in skuDetails) {
                if (productDetailsId == details.sku) {
                    val params = BillingFlowParams.newBuilder().setSkuDetails(details).build()
                    billingClient!!.launchBillingFlow(activity, params)
                }
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

    val skuDetails: List<SkuDetails>
        get() = oldVersionBilling!!.skuList

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
        billingClient?.endConnection()
        billingClient = null
    }

    fun fakeBoughtIap() {
        if (listener != null) {
            Handler().postDelayed({
                AppHelper.isPurchased = true
              //  purchases.add(Purchase("{\"orderId\":\"GPA.3322-6123-2845-14284\",\"packageName\":\"com.eco.flashlight\",\"productId\":\"weekly399_1st_saleoff\",\"purchaseTime\":1733818993940,\"purchaseState\":0,\"purchaseToken\":\"fmbaacehncggolklmlnegbhg.AO-J1OwIzu_ASJnGmezJ2FU481vFwE4P0ZsTyTAF09x_xyw66eM5KOoLrdkJ_Gz_tNkk6fYfxs5S6bh14wSqaHAzXM0tFR82Hw\",\"quantity\":1,\"autoRenewing\":true,\"acknowledged\":true}",""))
                listener?.onPurchaseUpdatedV5(purchases)
            }, 1500)
            Handler().postDelayed({
                AppHelper.isPurchased = false
            }, 30000)
        }
    }

}
