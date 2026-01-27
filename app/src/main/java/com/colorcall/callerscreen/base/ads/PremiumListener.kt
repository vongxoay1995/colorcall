package com.eco.core.common.ads

import android.content.Context

interface PremiumListener {
    fun isPremium(context: Context): Boolean
}