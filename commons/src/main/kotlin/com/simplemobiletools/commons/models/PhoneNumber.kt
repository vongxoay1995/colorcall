package com.simplemobiletools.commons.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class PhoneNumber(
    @field:SerializedName(value = "value", alternate = ["a"])
    var value: String,
    @field:SerializedName(value = "type", alternate = ["b"])
    var type: Int,
    @field:SerializedName(value = "label", alternate = ["c"])
    var label: String,
    @field:SerializedName(value = "normalizedNumber", alternate = ["d"])
    var normalizedNumber: String,
    @field:SerializedName(value = "isPrimary", alternate = ["e"])
    var isPrimary: Boolean = false
)
