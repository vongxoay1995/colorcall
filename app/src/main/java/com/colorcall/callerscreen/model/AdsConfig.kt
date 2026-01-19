package com.colorcall.callerscreen.model

import com.google.gson.annotations.SerializedName

data class AdsConfig(
    @SerializedName("ads_enable")
    val ads_enable: Boolean = false,
    @SerializedName("banner_enable")
    val banner_enable: Boolean = false,
    @SerializedName("inter_enable")
    val inter_enable: Boolean = false,
    @SerializedName("open_ads_enable")
    val open_ads_enable: Boolean = false
)