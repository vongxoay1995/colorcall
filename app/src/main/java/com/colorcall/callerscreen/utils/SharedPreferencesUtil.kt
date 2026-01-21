package com.colorcall.callerscreen.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.common.reflect.TypeToken
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.orhanobut.hawk.Hawk

object SharedPreferencesUtil {
    private const val PREFS = "PREFS"
    private const val PREFS_NAME = "PixScanPrefs"
    private lateinit var preference: SharedPreferences
    private const val IS_PURCHASE = "IS_PURCHASE"

    fun init(context: Context) {
        preference = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        migrateDataFromHawk()
    }

    fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun migrateDataFromHawk() {
        if (getBoolean("DATA_MIGRATED", false)) {
            return
        }
        putBoolean("DATA_MIGRATED", true)
        isPurchase = Hawk.get("KEY_IS_BILLING", false)
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return preference.getBoolean(key, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean) {
        preference.edit { putBoolean(key, value) }
    }

    fun getInt(key: String): Int {
        return preference.getInt(key, 0)
    }

    fun putInt(key: String, value: Int) {
        preference.edit { putInt(key, value) }
    }

    fun getLong(key: String): Long = preference.getLong(key, 0L)

    fun putLong(key: String, value: Long) = preference.edit { putLong(key, value) }

    /* var isPurchase: Boolean = false
         get() = preference.getBoolean(IS_PURCHASE, false)
         set(value) {
             field = value
             preference.edit { putBoolean(IS_PURCHASE, value) }
         }*/
    var isPurchase: Boolean
        get() = true
        set(value) {
            // không làm gì cả, hoặc ghi log nếu muốn
            // preference.edit { putBoolean(IS_PURCHASE, true) } // tuỳ chọn
        }

    inline fun <reified T> getObject(
        context: Context,
        key: String,
        defaultValue: T? = null
    ): T? {
        val json = getPrefs(context).getString(key, null) ?: return defaultValue

        return try {
            val gson = Gson()
            val type = object : TypeToken<T>() {}.type
            gson.fromJson<T>(json, type)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
            getPrefs(context).edit { remove(key) } // xóa dữ liệu hỏng
            defaultValue
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
            defaultValue
        }
    }

    inline fun <reified T> saveObject(context: Context, key: String, data: T?) {
        if (data == null) {
            getPrefs(context).edit { remove(key) }
            return
        }

        val gson = Gson()
        val json = gson.toJson(data)
        getPrefs(context).edit { putString(key, json) }
    }

}