package com.simplemobiletools.commons.models

import com.google.gson.annotations.SerializedName

data class AlarmSound(
    @field:SerializedName(value = "id", alternate = ["a"])
    val id: Int,
    @field:SerializedName(value = "title", alternate = ["b"])
    var title: String,
    @field:SerializedName(value = "uri", alternate = ["c"])
    var uri: String
)
