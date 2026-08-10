package com.colorcall.callerscreen.dialer.models

import com.google.gson.annotations.SerializedName

// a simpler Contact model containing just info needed at the call screen
data class CallContact(
    @field:SerializedName(value = "contactId", alternate = ["a"])
    var contactId: Int,
    @field:SerializedName(value = "name", alternate = ["b"])
    var name: String,
    @field:SerializedName(value = "photoUri", alternate = ["c"])
    var photoUri: String,
    @field:SerializedName(value = "number", alternate = ["d"])
    var number: String,
    @field:SerializedName(value = "numberLabel", alternate = ["e"])
    var numberLabel: String
)
