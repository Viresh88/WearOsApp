package com.example.wearosapp.base

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.example.wearosapp.bluetooth.BluetoothManagerClass
import com.example.wearosapp.inteface.bluetooth.BluetoothEventCallback

abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() , BluetoothEventCallback {

    var bindingT: T? = null
    protected val binding: T
        get() = bindingT!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        bindingT = createBinding()
        setContentView(binding.root)
        create(savedInstanceState)
        BluetoothManagerClass.addBleInfoCallback(this)
    }

    abstract fun createBinding(): T
    abstract fun create(savedInstanceState: Bundle?)

    fun startInActivity(clazz: Class<out Activity>) {
        val intent = Intent(this, clazz)
        startActivity(intent)
    }

    fun fill() {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }

    override fun onScanning(bluetoothDevice: BluetoothDevice) {}
    override fun onScanStarted() {}
    override fun onScanFinished() {}
    override fun onStartConnect(mac: String) {}
    override fun onConnectSuccess(bleDevice: BluetoothDevice?) {}
    override fun onConnectDeviceSuccess(bleDevice: BluetoothDevice?) {}
    override fun onPasswordIncorrect() {}
    override fun onScanFailed(errorCode: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        BluetoothManagerClass.removeBleInfoCallback(this)
    }
}