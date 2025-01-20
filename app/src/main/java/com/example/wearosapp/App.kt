package com.example.wearosapp

import android.app.Application
import com.example.wearosapp.bluetooth.BluetoothManagerClass

class App : Application() {

    companion object{
        private lateinit var instance: App
        fun getInstance() = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        BluetoothManagerClass.initializeBluetooth(this)
    }
}