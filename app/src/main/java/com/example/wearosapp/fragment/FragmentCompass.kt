package com.example.wearosapp.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.hardware.GeomagneticField
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.wearosapp.R
import com.example.wearosapp.adapter.DogAdapter
import com.example.wearosapp.base.BaseFragment
import com.example.wearosapp.bluetooth.BluetoothManagerClass
import com.example.wearosapp.databinding.FragmentCompassBinding
import com.example.wearosapp.helper.SensorHelper
import com.example.wearosapp.injection.Injection
import com.example.wearosapp.injection.ViewModelFactory
import com.example.wearosapp.model.Dog
import com.example.wearosapp.ui.utils.SharedPreferencesUtils
import com.example.wearosapp.viewmodel.DogViewModel
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@SuppressLint("NotifyDataSetChanged")
class FragmentCompass : BaseFragment<FragmentCompassBinding>() {

    private var viewModel: DogViewModel? = null
    private var modelFactory: ViewModelFactory? = null

    private var adapterDog: DogAdapter? = null
    private val dogs = ArrayList<Dog>()
    private var sensorUtils: SensorHelper? = null

    // Location properties replacing MapsFragment usage
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var currentLocation: Location? = null
    private var locationCallback: LocationCallback? = null
    private val REQUEST_LOCATION_PERMISSION = 1001

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentCompassBinding {
        return FragmentCompassBinding.inflate(inflater, container, false)
    }

    override fun create(savedInstanceState: Bundle?) {
        configureViewModel()
        initLocationClient() // Initialize fused location provider
        val macAddress = activity?.let { SharedPreferencesUtils.getString(it, "LastConnectedDevice", "") }
        if (macAddress != null) {
            viewModel?.getStatusByMacAddress(macAddress) { status ->
                if (status != null && status) {
                    initRecyclerViewCompass()
                    getDogInData()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.device_is_not_connected),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                currentLocation = result.lastLocation
                Log.d("FragmentCompass", "Received location: ${currentLocation?.latitude}, ${currentLocation?.longitude}")
                notifyDogPointer()
            }
        }
    }

    private fun configureViewModel() {
        modelFactory = Injection.provideViewModelFactory(requireActivity())
        viewModel = ViewModelProvider(this, modelFactory!!)[DogViewModel::class.java]
    }

    // Initialize fused location provider client, request, and callback
    private fun initLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        createLocationRequest()
        createLocationCallback()
        startLocationUpdates()
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000 // 10 seconds
            fastestInterval = 5000 // 5 seconds
            priority = PRIORITY_HIGH_ACCURACY
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    // Handle runtime permission results
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDogInData() {
        if (BluetoothManagerClass.isConnected()) {
            viewModel?.getAllDogs()?.observe(viewLifecycleOwner) { dogs ->
                // Optionally, you can filter dogs here (e.g., check for valid coordinates)
//                val filteredDogs = dogs.filter { it.isOnline && it.latitude != 0.0 && it.longitude != 0.0 }

                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) {
                        this@FragmentCompass.dogs.clear()
                        this@FragmentCompass.dogs.addAll(dogs)
                        notifyDogPointer()
                    }
                }
            }
        }
    }

    private fun updateDogSelectionInDatabase(dogs: MutableList<Dog>) {
        activity?.runOnUiThread {
            viewModel?.updateAllDogs(dogs)
        }
    }

    private fun initRecyclerViewCompass() {
        adapterDog = DogAdapter(dogs) { selectedDog ->
            handleDogSelection(selectedDog)
        }
        binding.recyclerviewDogCompass.apply {
            adapter = adapterDog
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            isNestedScrollingEnabled = false
        }
        (binding.recyclerviewDogCompass.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
    }

    @SuppressLint("SuspiciousIndentation")
    private fun handleDogSelection(selectedDog: Dog) {
        Log.d("handleDogSelection", "handleDogSelection")
        dogs.forEach { it.isSelected = false }
        selectedDog.isSelected = true
        CoroutineScope(Dispatchers.IO).launch {
            updateDogSelectionInDatabase(dogs)
            withContext(Dispatchers.Main) {
                notifyDogPointer()
            }
        }
    }

    override fun onDogData(dogs: List<Dog>) {
        if (!isAdded) {
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            val temp = ArrayList<Dog>()
            dogs.forEach { dog ->
                val existingDog = this@FragmentCompass.dogs.find { it.imei == dog.imei }
                if (existingDog != null) {
                    dog.isSelected = existingDog.isSelected
                    dog.levelSanction = existingDog.levelSanction
                }
                temp.add(dog)
            }
            withContext(Dispatchers.Main) {
                if (isAdded) {
                    notifyDogPointer()
                }
            }
        }
    }

    private fun notifyDogPointer() {
        if (dogs.isNotEmpty() && currentLocation != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val tempDogPointer = mutableListOf<Dog>()
                currentLocation?.let { position ->
                    dogs.forEach { dog ->
                        if (dog.latitude != 0.0 && dog.longitude != 0.0) {
                            val dogCopy = dog.copy()
                            // Calculate the angle between the user's current location and the dog's location
                            val angle = getAngle(
                                position.latitude,
                                position.longitude,
                                dog.latitude,
                                dog.longitude
                            ).toFloat()
                            dogCopy.angle = angle
                            tempDogPointer.add(dogCopy)
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    binding.dogCompassView.setDogPointer(tempDogPointer)
                    binding.dogCompassView.invalidate()  // Trigger redraw.
                    // **Update the adapter currentLocation:**
                    adapterDog?.currentLocation = currentLocation
                    adapterDog?.notifyDataSetChanged()
                }
            }
        }
    }



    private fun getAngle(
        userLat: Double,
        userLon: Double,
        dogLat: Double,
        dogLon: Double
    ): Double {
        val userLatRad = Math.toRadians(userLat)
        val userLonRad = Math.toRadians(userLon)
        val dogLatRad = Math.toRadians(dogLat)
        val dogLonRad = Math.toRadians(dogLon)

        val deltaLon = dogLonRad - userLonRad
        val y = sin(deltaLon) * cos(dogLatRad)
        val x = cos(userLatRad) * sin(dogLatRad) - sin(userLatRad) * cos(dogLatRad) * cos(deltaLon)
        var bearing = Math.toDegrees(atan2(y, x))
        bearing = (bearing + 360) % 360
        return bearing
    }


    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        if (sensorUtils == null) {
            sensorUtils = activity?.let { SensorHelper(it) }
        }
        sensorUtils?.register(object : SensorHelper.OnSensorListener {
            override fun onAngle(angle: Float) {
                val adjustedAngle = calculateTrueNorthAzimuth(angle)
                binding.dogCompassView.currentAngle = adjustedAngle
            }
        })
        // Restart location updates
        startLocationUpdates()
    }

    /**
     * Adjust the magnetic sensor's reading to get the true north azimuth.
     * Uses the currentLocation for magnetic declination calculations.
     */
    private fun calculateTrueNorthAzimuth(magneticNorthAzimuth: Float): Float {
        currentLocation?.let { userLocation ->
            val magneticDeclination = getMagneticDeclination(userLocation)
            val trueNorthAzimuth = magneticNorthAzimuth + magneticDeclination
            return (trueNorthAzimuth + 360) % 360
        }
        return magneticNorthAzimuth
    }

    private fun getMagneticDeclination(userLocation: Location): Float {
        val geoMagneticField = GeomagneticField(
            userLocation.latitude.toFloat(),
            userLocation.longitude.toFloat(),
            userLocation.altitude.toFloat(),
            System.currentTimeMillis()
        )
        return geoMagneticField.declination
    }

    override fun onPause() {
        super.onPause()
        sensorUtils?.unregister()
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
    }
}
