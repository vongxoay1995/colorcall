package com.colorcall.callerscreen.dialer.extensions

import android.content.SharedPreferences
import android.telecom.PhoneAccountHandle
import com.colorcall.callerscreen.dialer.models.PhoneAccountHandleModel
import com.google.gson.Gson

fun SharedPreferences.Editor.putPhoneAccountHandle(
    key: String,
    parcelable: PhoneAccountHandle
): SharedPreferences.Editor {
    val componentName = parcelable.componentName
    val myPhoneAccountHandleModel = PhoneAccountHandleModel(
        componentName.packageName, componentName.className, parcelable.id
    )
    val json = Gson().toJson(myPhoneAccountHandleModel)
    return putString(key, json)
}

fun SharedPreferences.getPhoneAccountHandleModel(
    key: String,
    default: PhoneAccountHandleModel?
): PhoneAccountHandleModel? {
    val json = getString(key, null) ?: return default
    return try {
        val model = Gson().fromJson(json, PhoneAccountHandleModel::class.java)
        if (model?.isValid() == true) {
            model
        } else {
            edit().remove(key).apply()
            default
        }
    } catch (_: RuntimeException) {
        edit().remove(key).apply()
        default
    }
}
