package com.example.mynewsapp.Utils

import android.content.Context
import android.content.SharedPreferences

object SharedPrefHelper {
    private lateinit var sharedPreferences: SharedPreferences
    //    private val gson = Gson()
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    }

    // Save String
    fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    // Get String
    fun getString(key: String, defaultValue: String = ""): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    // Save Int
    fun saveInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    // Get Int
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    // Save Boolean
    fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    // Get Boolean
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    // Save Float
    fun saveFloat(key: String, value: Float) {
        sharedPreferences.edit().putFloat(key, value).apply()
    }

    // Get Float
    fun getFloat(key: String, defaultValue: Float = 0.0f): Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }

    // Save Long
    fun saveLong(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }

    // Get Long
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }

    // Save Set<String>
    fun saveStringSet(key: String, value: Set<String>) {
        sharedPreferences.edit().putStringSet(key, value).apply()
    }

    // Get Set<String>
    fun getStringSet(key: String, defaultValue: Set<String> = emptySet()): Set<String> {
        return sharedPreferences.getStringSet(key, defaultValue) ?: defaultValue
    }


    // Clear specific key
    fun clearKey(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    // Clear all data
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }


}
