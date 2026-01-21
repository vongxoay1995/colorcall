package com.colorcall.callerscreen.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.colorcall.callerscreen.database.Background
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.orhanobut.hawk.Hawk

object SharedPreferencesUtil {
    private const val PREFS = "PREFS"
    private const val PREFS_NAME = "PixScanPrefs"
    private lateinit var preference: SharedPreferences
    private const val IS_PURCHASE = "IS_PURCHASE"
    private const val KEY_LIST_BACKGROUND = "LIST_BACKGROUND"  // key lưu list
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

    @JvmStatic  // cho Java gọi như static method
    fun saveObjectJava( key: String, data: Any?) {
        if (data == null) {
           preference.edit { remove(key) }
            return
        }
        val gson = Gson()
        val json = gson.toJson(data)
        preference.edit { putString(key, json) }
    }

    @JvmStatic
    fun <T> getObjectJava(key: String, clazz: Class<T>, defaultValue: T? = null): T? {
        val json = preference.getString(key, null) ?: return defaultValue

        return try {
            val gson = Gson()
            gson.fromJson(json, clazz) as T?
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
            preference.edit { remove(key) }
            defaultValue
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
            defaultValue
        }
    }
    fun saveListBackground(list: ArrayList<Background>) {
        val gson = Gson()
        val json = gson.toJson(list)  // convert list → JSON string
        preference.edit { putString(KEY_LIST_BACKGROUND, json) }
    }

    // Lấy ArrayList<Background> (nếu không có thì return empty list)
    fun getListBackground():ArrayList<Background> {
        val json = preference.getString(KEY_LIST_BACKGROUND, null) ?: return ArrayList()

        return try {
            val gson = Gson()
            val type = object : TypeToken<ArrayList<Background>>() {}.type
            gson.fromJson<ArrayList<Background>>(json, type) ?: ArrayList()
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
            preference.edit { remove(KEY_LIST_BACKGROUND) } // xóa nếu hỏng
            ArrayList()
        }
    }
}