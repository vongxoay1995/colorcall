package com.colorcall.callerscreen.utils

import com.orhanobut.hawk.Hawk

object AppHelper {
    private const val PURCHASED = "PURCHASED"
    private const val IS_INIT_BILLING = "IS_INIT_BILLING"

    var isInitBilling: Boolean = false
        get() {
            return Hawk.get(IS_INIT_BILLING, false)
        }
        set(value) {
            field = value
            Hawk.put(IS_INIT_BILLING, value)
        }
    var isPurchased: Boolean = false
        get() {
            return Hawk.get(PURCHASED, false)
        }
        set(value) {
            field = value
            Hawk.put(PURCHASED, value)
        }
}