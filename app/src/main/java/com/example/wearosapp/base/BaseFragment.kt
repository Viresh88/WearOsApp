package com.example.wearosapp.base

import android.bluetooth.BluetoothDevice
import android.databinding.tool.writer.ViewBinding
import androidx.fragment.app.Fragment
import com.example.wearosapp.inteface.bluetooth.BluetoothEventCallback


class BaseFragment<T : ViewBinding> : Fragment(), BluetoothEventCallback {
    override fun onScanning(bluetoothDevice: BluetoothDevice) {
        TODO("Not yet implemented")
    }

    override fun onScanStarted() {
        TODO("Not yet implemented")
    }

    override fun onScanFinished() {
        TODO("Not yet implemented")
    }

    override fun onStartConnect(mac: String) {
        TODO("Not yet implemented")
    }

    override fun onConnectSuccess(bleDevice: BluetoothDevice?) {
        TODO("Not yet implemented")
    }

    override fun onConnectDeviceSuccess(bleDevice: BluetoothDevice?) {
        TODO("Not yet implemented")
    }

    override fun onPasswordIncorrect() {
        TODO("Not yet implemented")
    }

    override fun onScanFailed(errorCode: Int) {
        TODO("Not yet implemented")
    }

}