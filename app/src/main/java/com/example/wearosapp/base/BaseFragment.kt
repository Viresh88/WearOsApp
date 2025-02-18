package com.example.wearosapp.base

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import androidx.fragment.app.Fragment
import com.example.wearosapp.MainActivity
import com.example.wearosapp.bluetooth.BluetoothManagerClass
import com.example.wearosapp.inteface.bluetooth.BluetoothEventCallback


abstract class BaseFragment<T : ViewBinding> : Fragment(), BluetoothEventCallback {

    var bindingT: T? = null
    protected open val binding : T
        get() = bindingT!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingT = createBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        create(savedInstanceState)
        BluetoothManagerClass.addBleInfoCallback(this)
    }

    abstract fun create(savedInstanceState: Bundle?)
    abstract fun createBinding(inflater: LayoutInflater , container: ViewGroup?): T
    override fun onScanning(bluetoothDevice: BluetoothDevice) {}
    override fun onScanStarted() {}
    override fun onScanFinished() {}
    override fun onStartConnect(mac: String) {}
    override fun onConnectSuccess(bleDevice: BluetoothDevice?) {}
    override fun onConnectDeviceSuccess(bleDevice: BluetoothDevice?) {}
    override fun onPasswordIncorrect() {}

    override fun onScanFailed(errorCode: Int) {}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            context as MainActivity
        } else {
            throw IllegalStateException("The attached context is not of the expected activity type.")
        }
    }

    fun startActivity(clazz: Class<out Activity>) {
        val intent = Intent(requireActivity(), clazz)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        BluetoothManagerClass.removeBleInfoCallback(this)
    }

}