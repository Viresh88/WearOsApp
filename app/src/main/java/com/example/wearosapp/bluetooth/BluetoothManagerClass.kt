@file:Suppress("DEPRECATION")

package com.example.wearosapp.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.wearosapp.inteface.bluetooth.BluetoothEventCallback

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@SuppressLint("MissingPermission" , "StaticFieldLeak")
object BluetoothManagerClass {

    private lateinit var context: Context
    private var password: String = ""
    private val UUID_SERVICE = UUID.fromString("0000AB00-0000-1000-8000-00805F9B34FB")
    private val UUID_NOTIFY = UUID.fromString("0000AB02-0000-1000-8000-00805F9B34FB")
    private val UUID_WRITE = UUID.fromString("0000AB01-0000-1000-8000-00805F9B34FB")
    private val UUID_DESCRIPTOR = UUID.fromString("00002A05-0000-1000-8000-00805F9B34FB")
    private var bleDevice: BluetoothDevice? = null
    private val deviceGattMap = ConcurrentHashMap<BluetoothDevice , BluetoothGatt>()
    var bluetoothEventCallbacks = ArrayList<BluetoothEventCallback>()
    private var characteristicNotify: BluetoothGattCharacteristic? = null
    private var characteristicWrite: BluetoothGattCharacteristic? = null
    private var scanCallback: ScanCallback? = null


    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private fun BluetoothDevice.isConnected() = deviceGattMap.containsKey(this)

    init {
        bluetoothEventCallbacks = ArrayList()
    }

    fun initializeBluetooth(context: Context): Boolean {
        BluetoothManagerClass.context = context
        return true
    }

//    fun startScan() {
//        val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
//        val scanCallback = createScanCallback()
//        val scanSettings = ScanSettings.Builder()
//            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//            .build()
//        val scanFilter = ScanFilter.Builder()
//            .setServiceUuid(ParcelUuid(UUID_SERVICE))
//            .build()
//        val scanFilters = listOf(scanFilter)
//        bluetoothLeScanner?.startScan(scanFilters, scanSettings, scanCallback)
//        this.bluetoothEventCallbacks.forEach { it.onScanStarted() }
//        stopScanAfterDelay()
//    }


fun startScan() {
    val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    if (scanCallback == null) {
        scanCallback = createScanCallback()
    }
    val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    // Start scanning without filters
    bluetoothLeScanner?.startScan(null, scanSettings, scanCallback)
    bluetoothEventCallbacks.forEach { it.onScanStarted() }
    stopScanAfterDelay()
}

    private fun stopScanAfterDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            stopScan()
        }, 5000)
    }

    fun stopScan() {
        val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        scanCallback?.let { callback ->
            bluetoothLeScanner?.stopScan(callback)
        }
        scanCallback = null
        bluetoothEventCallbacks.forEach { it.onScanFinished() }
    }




    fun disconnect() {
        val gatt = bleDevice?.let { deviceGattMap[it] }
        if (gatt != null) {
            gatt.disconnect()
            gatt.close()
            deviceGattMap.remove(bleDevice)
            bluetoothEventCallbacks.forEach { callback ->
                callback.onDisconnected(false, bleDevice)
            }
        }
    }

    fun isConnected(): Boolean {
        if (bleDevice == null) return false
        return bluetoothAdapter.isEnabled
    }

    fun addBleInfoCallback(callback: BluetoothEventCallback) {
        if (!bluetoothEventCallbacks.contains(callback)) bluetoothEventCallbacks.add(callback)
    }

    fun removeBleInfoCallback(callback: BluetoothEventCallback) {
        bluetoothEventCallbacks.remove(callback)
    }

    private fun createScanCallback(): ScanCallback {
        return object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.device?.let { bluetoothDevice ->
                    // Log the discovered device information
                    Log.d("BLE_SCAN", "Discovered device: ${bluetoothDevice.name} - ${bluetoothDevice.address}")
                    bluetoothEventCallbacks.forEach { it.onScanning(bluetoothDevice) }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e("BLE_SCAN", "Scan failed with error: $errorCode")
                bluetoothEventCallbacks.forEach { it.onScanFailed(errorCode) }
            }
        }
    }


//    private fun createGattCallback(): BluetoothGattCallback {
//        return object : BluetoothGattCallback() {
//            override fun onConnectionStateChange(
//                gatt: BluetoothGatt?,
//                status: Int,
//                newState: Int
//            ) {
//                if (newState == BluetoothProfile.STATE_CONNECTED) {
//                    val macAddress = SharedPreferencesUtils.getString(this@BluetoothManagerClass.context , "LastConnectedDevice" ?: "", "")
//                    if (!macAddress.isNullOrEmpty()) {
//                        bluetoothEventCallbacks.forEach {
//                            it.onConnectDeviceSuccess(bleDevice)
//                        }
//                    }
//                    gatt?.discoverServices()
//
//                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                    disconnect()
//                }
//            }
//
//            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
//                if (status == BluetoothGatt.GATT_SUCCESS) {
//                    val service = deviceGattMap[bleDevice]?.getService(UUID_SERVICE)
//                    characteristicNotify = service?.getCharacteristic(UUID_NOTIFY)
//                    characteristicWrite = service?.getCharacteristic(UUID_WRITE)
//                    if (characteristicNotify != null) {
//                        enableNotification(gatt!!)
//                    } else {
//                        disconnect()
//                    }
//                    SharedPreferencesUtils.putString(this@BluetoothManagerClass.context , "LastConnectedDevice" , bleDevice!!.address)
//                    bluetoothEventCallbacks.forEach {
//
//                        it.onConnectSuccess(bleDevice)
//                    }
//
//                } else {
//                    disconnect()
//                }
//            }
//
//            override fun onDescriptorWrite(
//                gatt: BluetoothGatt?,
//                descriptor: BluetoothGattDescriptor?,
//                status: Int
//            ) {
//                if (status == BluetoothGatt.GATT_SUCCESS && descriptor?.uuid == UUID_DESCRIPTOR) {}
//            }
//
//            @Deprecated("Deprecated in Java")
//            override fun onCharacteristicChanged(
//                gatt: BluetoothGatt?,
//                characteristic: BluetoothGattCharacteristic?
//            ) {
//                characteristic?.value?.let { value ->
//                    val data = characteristic.value // This is the data received from the peripheral device
//                    Log.d("LastConnectedDevice", "Received data: ${data.contentToString()}")
//                    bluetoothEventCallbacks.forEach { it.onNotify(value) }
//                    FormatCommand.format(value)
//                }
//            }
//        }
//    }

    private fun write(data: ByteArray) {
        if (characteristicWrite != null) {
            deviceGattMap.values.forEach { gatt ->
                characteristicWrite?.value = data
                gatt.writeCharacteristic(characteristicWrite)
            }
        }
    }

//    private fun enableNotification(gatt: BluetoothGatt) {
//        if (bleDevice!!.isConnected()) {
//            if (characteristicNotify != null) {
//                gatt.setCharacteristicNotification(characteristicNotify , true)
//                val descriptor = characteristicNotify!!.getDescriptor(UUID_DESCRIPTOR)
//                if (descriptor != null) {
//                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//                    gatt.writeDescriptor(descriptor)
//                }
//            }
//        }
//    }

//    private fun disableNotification(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
//        if (bleDevice!!.isConnected()) {
//            if (characteristicNotify != null) {
//                gatt.setCharacteristicNotification(characteristic, false)
//                val descriptor = characteristic.getDescriptor(UUID_DESCRIPTOR)
//                if (descriptor != null) {
//                    descriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
//                    gatt.writeDescriptor(descriptor)
//                    bluetoothEventCallbacks.forEach { it.onDisconnected(false, bleDevice) }
//                }
//            }
//        }
//    }

    fun sendPassword(pwd: String) {
        password = pwd
        val data = "Pass#$pwd\r\n"
        write(data.toByteArray())
    }

    fun sendRenameCommand(name: String) {
        val data = "Rename#$name\r\n"
        write(data.toByteArray())

    }

    fun sendChangePasswordCommand(newPassword: String) {
        val data = "Repwd#$newPassword\r\n"
        write(data.toByteArray())

    }

    fun writeData(data: String) {
        write((data + "\r\n").toByteArray())
    }
}