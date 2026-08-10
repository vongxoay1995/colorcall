package com.simplemobiletools.commons.models.contacts

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class IM(
    @field:SerializedName(value = "value", alternate = ["a"])
    var value: String,
    @field:SerializedName(value = "type", alternate = ["b"])
    var type: Int,
    @field:SerializedName(value = "label", alternate = ["c"])
    var label: String
)
