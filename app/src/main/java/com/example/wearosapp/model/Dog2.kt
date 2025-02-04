package com.example.wearosapp.model

data class Dog2(
    val id: Int,
    val name: String,
    val imageResId: Int,
    val beepProgress: Int = 100,
    val beepSound: String = "Beep Sound",
    var isSelected: Boolean = false,
    var angle: Float = 0f
)
