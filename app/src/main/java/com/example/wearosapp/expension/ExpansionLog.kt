package com.example.wearosapp.expension

import android.util.Log


fun Any.myLog(msg: String) {
    if (!BuildConfig.DEBUG) {
        return
    }

    Log.e(msg, "my Log")
}

fun Any.myLog(vararg msg: Any?) {
    if (!BuildConfig.DEBUG) {
        return
    }
    val logMessage = msg.joinToString(", ")
    myLog(logMessage)
}