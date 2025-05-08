package com.example.wearosapp.fragment

import com.example.wearosapp.R
import com.example.wearosapp.adapter.BluetoothAdapterDevice
import com.example.wearosapp.base.BaseFragment
import com.example.wearosapp.bluetooth.BluetoothManagerClass
import com.example.wearosapp.bluetooth.WriteCommand
import com.example.wearosapp.databinding.DialogInputPasswordBinding
import com.example.wearosapp.databinding.DialogueSettingsBluetoothBinding
import com.example.wearosapp.databinding.FragmentBluetoothBinding
import com.example.wearosapp.eventbus.Move2MapFragmentEventBus
import com.example.wearosapp.helper.PermissionHelper
import com.example.wearosapp.injection.Injection
import com.example.wearosapp.injection.ViewModelFactory
import com.example.wearosapp.model.Device
import com.example.wearosapp.ui.utils.SharedPreferencesUtils
import com.example.wearosapp.viewmodel.DogViewModel
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.wear.widget.WearableLinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import kotlin.properties.Delegates

class FragmentBluetooth : BaseFragment<FragmentBluetoothBinding>() {

    var isScrolling = false
    private lateinit var permissionHelper: PermissionHelper
    private var scanResultAdapter: BluetoothAdapterDevice? = null
    private var currentPassword: String? = null
    private var viewModel: DogViewModel? = null
    private var modelFactory: ViewModelFactory? = null
    private var scanResults = mutableListOf<Device>()
    private lateinit var dialogBinding: DialogueSettingsBluetoothBinding
    private var selectedPosition by Delegates.notNull<Int>()
    private var bluetoothText: String = ""
    private val handler = Handler(Looper.getMainLooper())
    private var isConnecting = false

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            requireActivity().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private var isScanning = false
        set(value) {
            field = value
            activity?.runOnUiThread {
                binding.analyseBottom.text = if (value) getString(R.string.loading) else getString(R.string.analyze)
            }
        }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentBluetoothBinding {
        return FragmentBluetoothBinding.inflate(inflater, container, false)
    }

    override fun create(savedInstanceState: Bundle?) {
        permissionHelper = PermissionHelper(requireActivity())
        binding.analyseBottom.setOnClickListener {
            if (bluetoothAdapter.isEnabled) onScanButtonClick()
            else promptEnableBluetooth() }
        configureAdapterBle()
        setupRecyclerView()
        configureViewModel()
        updateCollarInData()
        if (bluetoothAdapter.isEnabled) {
            onScanButtonClick()
        }
        else {
            promptEnableBluetooth()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("bluetoothText", bluetoothText)
        super.onSaveInstanceState(outState)
    }

    private fun configureViewModel() {
        modelFactory = Injection.provideViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this , modelFactory!!)[DogViewModel::class.java]
    }

    private fun updateCollarInData() {
        viewModel?.getAllDevices()?.observe(viewLifecycleOwner) { data ->
            scanResults.clear()
            scanResults.addAll(data)
            scanResultAdapter?.notifyDataSetChanged()
        }
    }

    private fun configureAdapterBle() {
        if (!activity?.packageManager?.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)!!) {
            Toast.makeText(requireContext() ,
                getString(R.string.blue_not_supported) , Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }
    }

    private fun promptEnableBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (permissionHelper.checkPermissions()) {
                if (!bluetoothAdapter.isEnabled) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
                } else {

                }
            } else {
                permissionHelper.checkPermissions()
            }
        } else {
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
            } else {
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (permissionHelper.onRequestPermissionsResult(requestCode, grantResults)) {
                    promptEnableBluetooth()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    startScan()
                }
            }
        }
    }



    class CustomScrollingLayoutCallback : WearableLinearLayoutManager.LayoutCallback() {

        private val maxShrinkAmount = 0.15f // Max 15% shrink
        private val shrinkEdgeThreshold = 0.2f // Shrink starts when near 20% from top or bottom

        override fun onLayoutFinished(child: View, parent: RecyclerView) {
            val itemCount = parent.adapter?.itemCount ?: 0

            // If there are 1 or 2 items, skip shrink effect
            if (itemCount <= 2) {

                return
            }

            val parentHeight = parent.height
            val childCenterY = (child.top + child.bottom) / 2f

            val topThreshold = parentHeight * shrinkEdgeThreshold
            val bottomThreshold = parentHeight * (1 - shrinkEdgeThreshold)

            val scale = when {
                childCenterY < topThreshold -> {
                    val distanceRatio = 1 - (childCenterY / topThreshold)
                    val scaled = 1f - maxShrinkAmount * distanceRatio
                    scaled.coerceIn(0.85f, 1f)
                }
                childCenterY > bottomThreshold -> {
                    val distanceRatio = (childCenterY - bottomThreshold) / (parentHeight - bottomThreshold)
                    val scaled = 1f - maxShrinkAmount * distanceRatio
                    scaled.coerceIn(0.85f, 1f)
                }
                else -> 1f
            }

            child.scaleX = scale
            child.scaleY = scale
        }

    }










    private fun setupRecyclerView() {


        scanResultAdapter = BluetoothAdapterDevice(scanResults,
            itemClicked = { position ->
                Toast.makeText(requireContext(), "Please wait...", Toast.LENGTH_SHORT).show()
                val macAddress = SharedPreferencesUtils.getString(requireContext() , "LastConnectedDevice" ?: "", "")
                if(macAddress == scanResults.getOrNull(position)!!.address){
                    returnToMainScreen()
                }else{

                    connectToDevice(position)
                }
                selectedPosition = position
            },
            parameterClicked = { position ->
                showOptionsDialogueChoice(position)
            }
        )

        binding.recyclerViewBluetooth.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                isScrolling = newState != RecyclerView.SCROLL_STATE_IDLE
                scanResultAdapter!!.setScrolling(isScrolling) // Pass to adapter
            }
        })


        binding.recyclerViewBluetooth.apply {
            isEdgeItemsCenteringEnabled = true
            adapter = scanResultAdapter
            layoutManager = WearableLinearLayoutManager(requireContext(), CustomScrollingLayoutCallback())

            //binding.recyclerViewBluetooth.layoutManager?.scrollToPosition(0)


        }




//        val animator = binding.recyclerViewBluetooth.itemAnimator
//        if (animator is SimpleItemAnimator) {
//            animator.supportsChangeAnimations = false
//        }
    }

    private fun connectToDevice(position: Int) {
        val device = scanResults.getOrNull(position) ?: return

        // Set connecting UI state

        scanResultAdapter?.notifyItemChanged(position)

        CoroutineScope(Dispatchers.IO).launch {
            val address = device.address ?: return@launch

            BluetoothManagerClass.connect(address) // fire-and-forget

            // Simulate a connection attempt duration (only for UI)
            delay(2000)

            withContext(Dispatchers.Main) {
                device.status = true // assuming it's now connected (fake status)
                scanResultAdapter?.notifyItemChanged(position)
            }
        }
    }



    private fun colorScan() {
        if (isScanning) {
            binding.analyseBottom.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
        } else {
            binding.analyseBottom.setTextColor(ContextCompat.getColor(requireContext(), R.color.light_orange))
        }
    }



    private fun onScanButtonClick() {
        isScanning = !isScanning
        colorScan()
        if (isScanning) {
            BluetoothManagerClass.startScan()
        } else {
            BluetoothManagerClass.stopScan()
        }
    }

    private fun startScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (permissionHelper.checkPermissions()) {
                BluetoothManagerClass.startScan()
            }
        } else {
            BluetoothManagerClass.startScan()
        }
    }

    private fun stopScan() {
        requireActivity().runOnUiThread {
            BluetoothManagerClass.stopScan()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onScanning(bluetoothDevice: BluetoothDevice) {
        requireActivity().runOnUiThread {
            val existingDeviceIndex = scanResults.indexOfFirst { it.address == bluetoothDevice.address }
            val newDevice = Device(bluetoothDevice.name, bluetoothDevice.address, false, getString(R.string.disconnect))
            if (existingDeviceIndex != -1) {
                var name = SharedPreferencesUtils.getString(requireActivity(), "LastConnectedDevice","");
                if(name == bluetoothDevice.address){
                    val newDevice = Device(bluetoothDevice.name, bluetoothDevice.address, true, getString(R.string.connected))
                    scanResults[existingDeviceIndex] = newDevice
                }else{
                    scanResults[existingDeviceIndex] = newDevice
                }

                scanResultAdapter?.notifyItemChanged(existingDeviceIndex)
            } else {
                scanResults.add(newDevice)
                scanResultAdapter?.notifyItemInserted(scanResults.size - 1)
            }

            scanResultAdapter?.notifyDataSetChanged()
        }
    }

    override fun onScanStarted() {
        requireActivity().runOnUiThread {
            binding.progressBarBluetooth.isVisible = true
            isScanning = true
            colorScan()
            scanResultAdapter?.notifyDataSetChanged()
        }
    }

    override fun onScanFinished() {
        requireActivity().runOnUiThread {
            binding.progressBarBluetooth.isVisible = false
            isScanning = false
            colorScan()
            scanResultAdapter?.notifyDataSetChanged()
        }
    }

    override fun onStartConnect(mac: String) {
        handler.post {
            requireActivity().runOnUiThread {
                val bluetoothStatus = getString(R.string.connecting)
                updateBleListStatus(mac , bluetoothStatus , false)
                binding.progressBarBluetooth.isVisible = false
                scanResultAdapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onConnectSuccess(bleDevice: BluetoothDevice?) {
        super.onConnectSuccess(bleDevice)
        requireActivity().runOnUiThread {
            if (currentPassword != null) {
                currentPassword?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        WriteCommand.sendPassword(it)
                    }
                    currentPassword = null
                }
                return@runOnUiThread
            }

            val password = SharedPreferencesUtils.getString(requireContext(), bleDevice?.address ?: "", "")
            if (password.isBlank()) {
                showInputDialog { passwordEditText ->
                    if (passwordEditText.isNotBlank()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            WriteCommand.sendPassword(passwordEditText)
                        }
                    }
                }
            } else {
                val devicePassword = if (password == bleDevice?.address) "1234" else password
                CoroutineScope(Dispatchers.IO).launch {
                    WriteCommand.sendPassword(devicePassword)
                }
            }
            scanResultAdapter?.notifyDataSetChanged()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onConnectDeviceSuccess(bleDevice: BluetoothDevice?) {
        requireActivity().runOnUiThread {
            isConnecting = false
            val bluetoothStatus = activity?.applicationContext?.getString(R.string.connected)
            updateBleListStatus(bleDevice?.address , bluetoothStatus , true)
            Toast.makeText(requireContext() , getString(R.string.connected) , Toast.LENGTH_SHORT)
                .show()
            returnToMainScreen()
            viewModel?.createNewDevice(Device(bleDevice?.name, bleDevice?.address, true, bluetoothStatus,"Connected"))
            scanResultAdapter?.notifyDataSetChanged()
        }
    }

    override fun onPasswordIncorrect() {
        requireActivity().runOnUiThread {
            isConnecting = false
            showInputDialog { passwordEditText ->
                if (passwordEditText.isNotBlank()) {
                    currentPassword = passwordEditText
                    CoroutineScope(Dispatchers.IO).launch {
                        connectToDevice(selectedPosition)
                    }
                }
            }
            scanResultAdapter?.notifyDataSetChanged()
            Toast.makeText(requireContext(), getString(R.string.psw_error), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConnectFail(bleDevice: BluetoothDevice?) {
        requireActivity().runOnUiThread {
            isConnecting = false
            val bluetoothStatus = activity?.applicationContext?.getString(R.string.connect_fail)
            updateBleListStatus(bleDevice?.address , bluetoothStatus , false)
            Toast.makeText(requireContext() , getString(R.string.connect_fail) , Toast.LENGTH_SHORT)
                .show()
            scanResultAdapter?.notifyDataSetChanged()
        }
    }

    override fun onDisconnected(isActiveDisConnected: Boolean , device: BluetoothDevice?) {
        super.onDisconnected(isActiveDisConnected , device)
        requireActivity().runOnUiThread {
            val bluetoothStatus = activity?.applicationContext?.getString(R.string.disconnect)
            updateBleListStatus(device?.address , bluetoothStatus , false)
            Toast.makeText(requireContext() , getString(R.string.disconnect) , Toast.LENGTH_SHORT)
                .show()
            scanResultAdapter?.notifyDataSetChanged()
        }
    }

    private fun showInputDialog(onConfirm: (String) -> Unit) {
        val binding = DialogInputPasswordBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
            .setView(binding.root)
            .create()

        dialog.show()

        dialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.bottom_style_dialog)
        )

        binding.btnConfirm.setOnClickListener {
            val password = binding.pwdEditText.text.toString()
            onConfirm(password)  // Process the password (e.g., connect to device)

            // Dismiss the dialog after confirming
            dialog.dismiss()
        }
    }

    private fun getViewDialogues(): AlertDialog {
        dialogBinding = DialogueSettingsBluetoothBinding.inflate(layoutInflater)
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
        builder.setView(dialogBinding.root)
        return builder.create()
    }

    private fun showOptionsDialogueChoice(position: Int) {
        val device = scanResults.getOrNull(position)
        device?.let { bluetoothDevice ->
            val dialogBuilder = getViewDialogues()
            dialogBuilder.window?.apply {
                setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bottom_style_dialog))
            }

            dialogBuilder.show()
            dialogBinding.textViewBluetoothTitle.text= bluetoothDevice.name

            dialogBinding.textViewDeleteBluetooth.setOnClickListener {
                deleteDeviceInDialogue(position)
                dialogBuilder.dismiss()
            }

            dialogBinding.textViewConnected.setOnClickListener {
                if (bluetoothDevice.status) {
                    Toast.makeText(requireContext(), getString(R.string.already_connect), Toast.LENGTH_SHORT).show()
                } else {
                    bluetoothDevice.address?.let { address ->
                        BluetoothManagerClass.connect(address)
                    }
                }
                dialogBuilder.dismiss()
            }

            dialogBinding.textViewDisconnected.setOnClickListener {
                if (bluetoothDevice.status) {
                    BluetoothManagerClass.disconnect()
                }
                dialogBuilder.dismiss()
            }
            dialogBinding.imageButtonCloseBluetooth.setOnClickListener {
                dialogBuilder.dismiss()
            }
        }
    }

    private fun updateBleListStatus(mac: String?, bleStatus: String?, status: Boolean) {
        scanResults.forEach { device->
            if (mac == null || device.address == mac) {
                device.status = status
                device.bluetoothStatus = bleStatus
                device.lastConnected = bleStatus
                viewModel?.updateDevice(device)
            }
            scanResultAdapter?.notifyDataSetChanged()
        }
    }

    private fun deleteDeviceFromDatabase(device: Device?) {
        SharedPreferencesUtils.putString(requireActivity(), "LastConnectedDevice" ?: "", "")
        viewModel?.deleteDevice(device?.id!!)
    }

    private fun deleteDeviceInDialogue(position: Int) {
        if (position in scanResults.indices) {
            val deviceToDelete = scanResults[position]
            scanResults.removeAt(position)
            scanResultAdapter?.notifyItemRemoved(position)
            CoroutineScope(Dispatchers.IO).launch {
                deleteDeviceFromDatabase(deviceToDelete)
            }
        }
        BluetoothManagerClass.disconnect()
    }

    private fun returnToMainScreen() {
        val event = Move2MapFragmentEventBus.createEvent()
        EventBus.getDefault().post(event)
    }


    override fun onResume() {
        super.onResume()
        // Clear out old BLE device data when the fragment resumes
        scanResults.clear()
        scanResultAdapter?.notifyDataSetChanged()
        stopScan()
        promptEnableBluetooth()
    }

    companion object {
        const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
    }
}
