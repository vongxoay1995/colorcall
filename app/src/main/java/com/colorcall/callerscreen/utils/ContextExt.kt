package com.colorcall.callerscreen.utils

import android.content.Context
import androidx.core.content.edit

inline fun <reified T> Context.getApplication(callback: (T) -> Unit) {
    if (applicationContext is T) callback.invoke(applicationContext as T)
}

private fun Context.getSharedPreferences() =
    getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

fun Context.getBoolean(key: String, defaultValue: Boolean = false) =
    getSharedPreferences().getBoolean(key, defaultValue)

fun Context.getInt(key: String, defaultValue: Int = -1) =
    getSharedPreferences().getInt(key, defaultValue)

fun Context.getFloat(key: String, defaultValue: Float = -1f) =
    getSharedPreferences().getFloat(key, defaultValue)

fun Context.getLong(key: String, defaultValue: Long = -1) =
    getSharedPreferences().getLong(key, defaultValue)

fun Context.getString(key: String, defaultValue: String = "") =
    getSharedPreferences().getString(key, defaultValue)

fun Context.putExtra(key: String, value: Any) {
    getSharedPreferences().edit {
        when (value) {
            is Boolean -> putBoolean(key, value)
            is Int -> putInt(key, value)
            is String -> putString(key, value)
            is Float -> putFloat(key, value)
            is Long -> putLong(key, value)
        }
    }
}