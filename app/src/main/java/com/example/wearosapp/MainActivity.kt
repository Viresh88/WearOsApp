package com.example.wearosapp

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.wearosapp.bluetooth.BluetoothManagerClass
import com.example.wearosapp.bluetooth.FormatCommand
import com.example.wearosapp.eventbus.Move2MapFragmentEventBus
import com.example.wearosapp.injection.Injection
import com.example.wearosapp.injection.ViewModelFactory
import com.example.wearosapp.model.Device
import com.example.wearosapp.model.Dog
import com.example.wearosapp.ui.utils.SharedPreferencesUtils
import com.example.wearosapp.viewmodel.DogViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private var viewModel: DogViewModel? = null
    private var bluetooth: MenuItem? = null
    private val deviceMutableList = ArrayList<Device>()
    private var dogs = ArrayList<Dog>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

     //   BluetoothManagerClass.disconnect()  // This will call callbacks to mark disconnection.
        // Optionally clear shared preferences:
        //SharedPreferencesUtils.putString(this, "LastConnectedDevice", "")
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        configureViewModel()
        BluetoothManagerClass.initializeBluetooth(this)
        getDevicesFromDatabase()
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        actionBar?.hide()
        enableEdgeToEdge()
    }

    private fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: Observer<T>) {
        observe(owner, object : Observer<T> {
            override fun onChanged(value: T) {
                observer.onChanged(value)
                removeObserver(this)
            }
        })
    }

    private fun getDevicesFromDatabase() {
        viewModel?.getAllDevices()?.observeOnce(this) { devices ->
            deviceMutableList.clear()
            deviceMutableList.addAll(devices.map { it.apply { status = false } })
            updateStatus()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        //unregisterReceiver(broadcastReceiver)
        updateStatus()
        //bindingT = null
    }


    private fun updateStatus() {
        deviceMutableList.forEach { device ->
            device.bluetoothStatus = getString(R.string.disconnect)
            device.status = false
            viewModel?.updateDevice(device)
            BluetoothManagerClass.disconnect()
        }
    }


    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
    @Subscribe
    fun onMoveToCompassEvent(event: Move2MapFragmentEventBus) {
        navigateTo(R.id.nav_compass)
    }


    private fun navigateTo(destinationId: Int) {
        if (navController.currentDestination?.id != destinationId) {
            navController.navigate(destinationId)
        }
    }

    private fun configureViewModel() {
        val modelFactory: ViewModelFactory = Injection.provideViewModelFactory(this)
        viewModel = ViewModelProvider(this, modelFactory)[DogViewModel::class.java]
        BluetoothManagerClass.initializeBluetooth(this)
        viewModel?.let { FormatCommand.setViewModel(it) }
        FormatCommand.setContext(this)
    }
}


