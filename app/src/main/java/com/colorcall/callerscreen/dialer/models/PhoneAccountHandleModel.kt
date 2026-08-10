package com.colorcall.callerscreen.dialer.models

import com.google.gson.annotations.SerializedName

data class PhoneAccountHandleModel(
    @field:SerializedName(value = "packageName", alternate = ["a"])
    val packageName: String?,
    @field:SerializedName(value = "className", alternate = ["b"])
    val className: String?,
    @field:SerializedName(value = "id", alternate = ["c"])
    val id: String?
) {
    fun isValid(): Boolean =
        !packageName.isNullOrBlank() && !className.isNullOrBlank() && id != null
}
