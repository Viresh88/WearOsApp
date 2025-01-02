package com.example.wearosapp.base

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import androidx.fragment.app.Fragment
import com.example.wearosapp.inteface.bluetooth.BluetoothEventCallback


abstract class BaseFragment<T : ViewBinding> : Fragment(), BluetoothEventCallback {

      var bindingT : T? =null
      protected val binding: T
          get() = binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingT = createBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        create(savedInstanceState)
        BluetoothManagerClass.addBleInfoCallback(this)
    }

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

    abstract fun createBinding(inflater: LayoutInflater , container: ViewGroup?): T

}