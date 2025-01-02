package com.example.wearosapp.bluetooth

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.wearosapp.inteface.bluetooth.OnFormatData
import com.example.wearosapp.model.Dog
import com.example.wearosapp.viewmodel.DogViewModel
import java.util.TimeZone

object FormatCommand {
    private var timeZone = TimeZone.getDefault().getOffset(System.currentTimeMillis())
    private var dataBytes = ArrayList<Byte>()
    private var isR = false
    private var isN = false
    var onFormatData : OnFormatData? = null
    private var isTrailStart = false
    private var deviceVer: String? = null
    private var viewModel: DogViewModel? = null
    private var dogs = ArrayList<Dog>()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var appContext: Context
}