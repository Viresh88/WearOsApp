package com.example.wearosapp.fragment

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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import kotlin.properties.Delegates

class FragmentBluetooth : BaseFragment<FragmentBluetoothBinding>() {

    private lateinit var permissionHelper: PermissionHelper
    private var scanResultAdapter: BluetoothAdapterDevice? = null
    private var currentPassword: String? = null
    private var viewModel: DogViewModel? = null
    private var modelFactory: ViewModelFactory? = null
    private var bluetoothAdapterDevice: BluetoothAdapterDevice? = null
    // Our scanned devices list (legacy Device model)
    private var scanResults = mutableListOf<Device>()
    private lateinit var dialogBinding: DialogueSettingsBluetoothBinding
    private var selectedPosition by Delegates.notNull<Int>()
    private var bluetoothText: String = ""
    private val handler = Handler(Looper.getMainLooper())
    private var _binding: FragmentBluetoothBinding? = null
    override val binding: FragmentBluetoothBinding
        get() = _binding!!

    // Bluetooth system adapter
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = requireActivity().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    // Flag to track scanning state
    private var isScanning = false
        set(value) {
            field = value
            activity?.runOnUiThread {
                binding.analyseBottom.text = if (value) getString(R.string.loading) else getString(R.string.analyze)
            }
        }

    private lateinit var activatedBluetooth: String

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBluetoothBinding {
        _binding = FragmentBluetoothBinding.inflate(inflater, container, false)
        return binding
    }

    override fun create(savedInstanceState: Bundle?) {
        permissionHelper = PermissionHelper(requireActivity())
        binding.analyseBottom.setOnClickListener {
            if (bluetoothAdapter.isEnabled) onScanButtonClick()
            else promptEnableBluetooth() }
        configureAdapterBle()


        // For demonstration, add demo devices (replace with actual scan results later)


        viewModel?.getAllDevices()?.observe(viewLifecycleOwner) { devices ->
            scanResults.clear()
            devices?.let { scanResults.addAll(it) }
            bluetoothAdapterDevice?.setDevices(scanResults)
            bluetoothAdapterDevice?.notifyDataSetChanged()
        }

        // --- Set Up RecyclerView with the new BluetoothAdapterDevice ---
        setupRecyclerView()

        // Other initializations (ViewModel, Bluetooth start, etc.)
        configureViewModel()
        updateCollarInData()
        getBleStarted()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // For navigation: clicking the search button also navigates to FragmentCompass.
//        val btn = view.findViewById<AppCompatButton>(R.id.analyse_bottom)
//        btn.setOnClickListener {
//            val compassFragment = FragmentCompass()
//            parentFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, compassFragment)
//                .addToBackStack(null)
//                .commit()
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBluetoothBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("bluetoothText", bluetoothText)
        super.onSaveInstanceState(outState)
    }

    // --- Advanced Functions ---
    private fun configureViewModel() {
        val modelFactory = Injection.provideViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, modelFactory)[DogViewModel::class.java]

        // Observe LiveData from ViewModel to update the list
        viewModel?.getAllDevices()?.observe(viewLifecycleOwner) { devices ->
            scanResults.clear()
            devices?.let { scanResults.addAll(it) }
            bluetoothAdapterDevice?.setDevices(scanResults)
            bluetoothAdapterDevice?.notifyDataSetChanged()
        }
    }
    private fun updateCollarInData() {
        viewModel?.getAllDevices()?.observe(viewLifecycleOwner) { data ->
            scanResults.clear()
            scanResults.addAll(data)
            bluetoothAdapterDevice?.notifyDataSetChanged()
        }
    }

    private fun configureAdapterBle() {
        if (!activity?.packageManager?.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)!!) {
            Toast.makeText(requireContext(), getString(R.string.blue_not_supported), Toast.LENGTH_SHORT).show()
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
                    getBleStarted()
                }
            } else {
                permissionHelper.checkPermissions()
            }
        } else {
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
            } else {
                getBleStarted()
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

    // --- RecyclerView Integration ---
    private fun setupRecyclerView() {
        scanResultAdapter = BluetoothAdapterDevice(scanResults,
            itemClicked = { position ->
                connectToDevice(position)
                selectedPosition = position
            },
            parameterClicked = { position ->
                showOptionsDialogueChoice(position)
            }
        )
        binding.recyclerViewBluetooth.apply {
            adapter = scanResultAdapter
            layoutManager = LinearLayoutManager(
                requireContext() ,
                RecyclerView.VERTICAL ,
                false
            )
            isNestedScrollingEnabled = false
        }

        val animator = binding.recyclerViewBluetooth.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    // --- Scanning & Device Handling ---
//    private fun getDemoDevices() = listOf(
//        Device(
//            name = "Device 1",
//            address = "00:11:22:33:44:55",
//            status = false,
//            bluetoothStatus = getString(R.string.disconnect)
//        ),
//        Device(
//            name = "Device 2",
//            address = "AA:BB:CC:DD:EE:FF",
//            status = false,
//            bluetoothStatus = getString(R.string.disconnect)
//        ),
//        Device(
//            name = "Device 3",
//            address = "11:22:33:44:55:66",
//            status = false,
//            bluetoothStatus = getString(R.string.disconnect)
//        )
//    )

    private fun connectToDevice(position: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            val device = scanResults.getOrNull(position)
            device?.let {
                val newStatus = !it.status
                it.status = newStatus
                if (newStatus) {
                    it.address?.let { address ->
                        //BluetoothManagerClass.connect(address)
                    }
                    delay(1000)
                    it.status = false
                } else {
                    it.address?.let {
                        BluetoothManagerClass.disconnect()
                    }
                }
            }
            bluetoothAdapterDevice?.notifyDataSetChanged()
        }
    }

    private fun colorScan() {
        if (isScanning) {
            binding.analyseBottom.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
        } else {
            binding.analyseBottom.setTextColor(ContextCompat.getColor(requireContext(), R.color.light_orange))
        }
    }

    private fun bluetoothText(status: String?) {
        binding.activatedBluetooth.text = status
    }

    private fun onScanButtonClick() {
        isScanning = !isScanning
        colorScan()
        if (isScanning) {
            // Call the BLE scan from BluetoothManagerClass
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
        Log.d("FragmentBluetooth", "onScanning called with: ${bluetoothDevice.name} - ${bluetoothDevice.address}")
        requireActivity().runOnUiThread {
            val index = scanResults.indexOfFirst { it.address == bluetoothDevice.address }
            val newDevice = Device(
                name = bluetoothDevice.name ?: "Unknown Device",
                address = bluetoothDevice.address,
                status = false,
                bluetoothStatus = getString(R.string.disconnect)
            )
            if (index != -1) {
                scanResults[index] = newDevice
                bluetoothAdapterDevice?.notifyItemChanged(index)
            } else {
                scanResults.add(newDevice)
                bluetoothAdapterDevice?.notifyItemInserted(scanResults.size - 1)
            }
            bluetoothAdapterDevice?.notifyDataSetChanged()
        }
    }

    override fun onScanStarted() {
        requireActivity().runOnUiThread {
            binding.progressBarBluetooth.isVisible = true
            isScanning = true
            colorScan()
            bluetoothAdapterDevice?.notifyDataSetChanged()
        }
    }

    override fun onScanFinished() {
        requireActivity().runOnUiThread {
            binding.progressBarBluetooth.isVisible = false
            isScanning = false
            colorScan()
            bluetoothAdapterDevice?.notifyDataSetChanged()
        }
    }

    override fun onStartConnect(mac: String) {
        handler.post {
            requireActivity().runOnUiThread {
                val bluetoothStatus = getString(R.string.connecting)
                updateBleListStatus(mac, bluetoothStatus, false)
                bluetoothText(bluetoothStatus)
                binding.progressBarBluetooth.isVisible = false
                bluetoothAdapterDevice?.notifyDataSetChanged()
            }
        }
    }

    // --- Connection Callbacks & Dialogs (Remaining methods unchanged) ---
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
            bluetoothAdapterDevice?.notifyDataSetChanged()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onConnectDeviceSuccess(bleDevice: BluetoothDevice?) {
        requireActivity().runOnUiThread {
            val bluetoothStatus = activity?.applicationContext?.getString(R.string.connected)
            updateBleListStatus(bleDevice?.address, bluetoothStatus, true)
            Toast.makeText(requireContext(), getString(R.string.connected), Toast.LENGTH_SHORT).show()
            returnToMainScreen()
            viewModel?.createNewDevice(
                Device(bleDevice?.name, bleDevice?.address, true, bluetoothStatus, "Connected")
            )
            bluetoothAdapterDevice?.notifyDataSetChanged()
        }
    }

    override fun onPasswordIncorrect() {
        requireActivity().runOnUiThread {
            showInputDialog { passwordEditText ->
                if (passwordEditText.isNotBlank()) {
                    currentPassword = passwordEditText
                    CoroutineScope(Dispatchers.IO).launch {
                        connectToDevice(selectedPosition)
                    }
                }
            }
            bluetoothAdapterDevice?.notifyDataSetChanged()
            Toast.makeText(requireContext(), getString(R.string.psw_error), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConnectFail(bleDevice: BluetoothDevice?) {
        requireActivity().runOnUiThread {
            val bluetoothStatus = activity?.applicationContext?.getString(R.string.connect_fail)
            updateBleListStatus(bleDevice?.address, bluetoothStatus, false)
            bluetoothText(bluetoothStatus)
            Toast.makeText(requireContext(), getString(R.string.connect_fail), Toast.LENGTH_SHORT).show()
            bluetoothAdapterDevice?.notifyDataSetChanged()
        }
    }

    override fun onDisconnected(isActiveDisConnected: Boolean, device: BluetoothDevice?) {
        super.onDisconnected(isActiveDisConnected, device)
        requireActivity().runOnUiThread {
            val bluetoothStatus = activity?.applicationContext?.getString(R.string.disconnect)
            updateBleListStatus(device?.address, bluetoothStatus, false)
            bluetoothText(bluetoothStatus)
            Toast.makeText(requireContext(), getString(R.string.disconnect), Toast.LENGTH_SHORT).show()
            bluetoothAdapterDevice?.notifyDataSetChanged()
        }
    }

    private fun showInputDialog(onConfirm: (String) -> Unit) {
        val bindingInput = DialogInputPasswordBinding.inflate(layoutInflater)
        AlertDialog.Builder(requireContext())
            .setView(bindingInput.root)
            .setPositiveButton(R.string.confirm) { _, _ ->
                val passwordEditText = bindingInput.pwdEditText.text.toString()
                onConfirm(passwordEditText)
            }
            .show()
            .window?.setBackgroundDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.bottom_style_dialog)
            )
    }

    private fun getViewDialogues(): AlertDialog {
        dialogBinding = DialogueSettingsBluetoothBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext())
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
            dialogBinding.textViewBluetoothTitle.text = bluetoothDevice.name

            dialogBinding.textViewDeleteBluetooth.setOnClickListener {
                deleteDeviceInDialogue(position)
                dialogBuilder.dismiss()
            }

            dialogBinding.textViewConnected.setOnClickListener {
                if (bluetoothDevice.status) {
                    Toast.makeText(requireContext(), getString(R.string.already_connect), Toast.LENGTH_SHORT).show()
                } else {
                    bluetoothDevice.address?.let { address ->
                        //BluetoothManagerClass.connect(address)
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
        scanResults.forEach { device ->
            if (mac == null || device.address == mac) {
                device.status = status
                device.bluetoothStatus = bleStatus
                device.lastConnected = bleStatus
                viewModel?.updateDevice(device)
            }
        }
        bluetoothAdapterDevice?.notifyDataSetChanged()
    }

    private fun deleteDeviceFromDatabase(device: Device?) {
        SharedPreferencesUtils.putString(requireActivity(), "LastConnectedDevice", "")
        viewModel?.deleteDevice(device?.id!!)
    }

    private fun deleteDeviceInDialogue(position: Int) {
        if (position in scanResults.indices) {
            val deviceToDelete = scanResults[position]
            scanResults.removeAt(position)
            bluetoothAdapterDevice?.notifyItemRemoved(position)
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
    private fun getBleStarted() {
        if (!bluetoothAdapter.isEnabled) {
            binding.textBluetooth.text = getString(R.string.text_bluetooth)
            binding.textBluetooth.textSize = 6F
        } else {
            binding.textBluetooth.text = getString(R.string.bluetooth_pairing_requested)
            binding.textBluetooth.textSize = 6F
        }
    }
    override fun onResume() {
        super.onResume()
        BluetoothManagerClass.addBleInfoCallback(this)
        stopScan()
        promptEnableBluetooth()
    }
    override fun onPause() {
        super.onPause()
        BluetoothManagerClass.removeBleInfoCallback(this)
    }
    companion object {
        const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
    }
}
