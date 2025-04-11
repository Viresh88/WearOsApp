package com.example.wearosapp.ui.utils

import android.content.Context

object SharedPreferencesUtils {
    private const val SP_NAME = "newdogneck_sp"

    fun putString(context: Context , key: String , value: String): Boolean {
        val sharedPreferences = context.getSharedPreferences(SP_NAME , Context.MODE_PRIVATE)
        return sharedPreferences.edit().run {
            putString(key, value)
            commit()
        }
    }

    fun remove(context: Context , key: String) {
        val sp = context.getSharedPreferences(SP_NAME , Context.MODE_PRIVATE)
        sp.edit().run {
            remove(key)
            commit()
        }
    }

    fun getString(context: Context , key: String , defValue: String): String {
        val sp = context.getSharedPreferences(SP_NAME , Context.MODE_PRIVATE)
        return sp.getString(key, defValue) ?: ""
    }

    fun putBoolean(context: Context , prefSwitchPush: String , b: Boolean): Boolean {
        val sharedPreferences = context.getSharedPreferences(SP_NAME , Context.MODE_PRIVATE)
        return sharedPreferences.edit().run {
            putBoolean(prefSwitchPush, b)
            commit()
        }
    }

    fun getBoolean(context: Context , prefSwitchPush: String , boolean: Boolean): Boolean {
        val sharedPreferences = context.getSharedPreferences(SP_NAME , Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(prefSwitchPush, boolean)
    }
}