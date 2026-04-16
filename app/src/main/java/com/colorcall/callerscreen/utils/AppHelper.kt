package com.colorcall.callerscreen.utils

import com.orhanobut.hawk.Hawk

object AppHelper {
    private const val PURCHASED = "PURCHASED"
    private const val IS_INIT_BILLING = "IS_INIT_BILLING"

    var isInitBilling: Boolean = false
        get() {
            return if (Hawk.isBuilt()) Hawk.get(IS_INIT_BILLING, false) else false
        }
        set(value) {
            field = value
            if (Hawk.isBuilt()) Hawk.put(IS_INIT_BILLING, value)
        }
    var isPurchased: Boolean = false
        get() {
            return if (Hawk.isBuilt()) Hawk.get(PURCHASED, false) else false
        }
        set(value) {
            field = value
            if (Hawk.isBuilt()) Hawk.put(PURCHASED, value)
        }
}