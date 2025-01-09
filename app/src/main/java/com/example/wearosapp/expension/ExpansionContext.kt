package com.example.wearosapp.expension

import android.view.View

fun View.dp2px(dp: Int): Int {
    val scale = this.resources.displayMetrics.density
    return  ((dp * scale + 0.9f).toInt())
}