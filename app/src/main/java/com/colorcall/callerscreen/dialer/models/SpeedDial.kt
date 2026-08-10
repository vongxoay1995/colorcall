package com.colorcall.callerscreen.dialer.models

import com.google.gson.annotations.SerializedName

data class SpeedDial(
    @field:SerializedName(value = "id", alternate = ["a"])
    val id: Int,
    @field:SerializedName(value = "number", alternate = ["b"])
    var number: String,
    @field:SerializedName(value = "displayName", alternate = ["c"])
    var displayName: String
) {
    fun isValid() = number.trim().isNotEmpty()
}
