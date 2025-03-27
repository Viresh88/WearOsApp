package com.example.wearosapp.fragment

import android.annotation.SuppressLint
import android.hardware.GeomagneticField
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.wearosapp.R
import com.example.wearosapp.adapter.DogAdapter
import com.example.wearosapp.base.BaseFragment
import com.example.wearosapp.bluetooth.BluetoothManagerClass
import com.example.wearosapp.databinding.FragmentCompassBinding
import com.example.wearosapp.injection.Injection
import com.example.wearosapp.injection.ViewModelFactory
import com.example.wearosapp.model.Dog
import com.example.wearosapp.ui.utils.SharedPreferencesUtils
import com.example.wearosapp.viewmodel.DogViewModel
import com.example.wearosapp.fragment.CompassFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("NotifyDataSetChanged")
class CompassFragment : BaseFragment<FragmentCompassBinding>() {
    private var viewModel: DogViewModel? = null
    private var modelFactory: ViewModelFactory? = null

    private var adapterDog: DogAdapter? = null
    private val dogs = ArrayList<Dog>()


    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentCompassBinding {
        return FragmentCompassBinding.inflate(inflater, container, false)
    }

    override fun create(savedInstanceState: Bundle?) {
        configureViewModel()
        val macAddress =
            activity?.let { SharedPreferencesUtils.getString(it, "LastConnectedDevice" ?: "", "") }
        if (macAddress != null) {
            viewModel?.getStatusByMacAddress(macAddress) { status ->

                if (status!= null && status) {
                    initRecyclerViewCompass()
                    getDogInData()
                } else {
                    Toast.makeText(requireContext() , getString(R.string.device_is_not_connected) , Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }


    private fun configureViewModel() {
        modelFactory = Injection.provideViewModelFactory(requireActivity())
        viewModel = ViewModelProvider(this, modelFactory!!)[DogViewModel::class.java]
    }



    private fun getDogInData() {
        if (BluetoothManagerClass.isConnected()) {
            viewModel?.getAllDogs()?.observe(viewLifecycleOwner) { dogs ->
                CoroutineScope(Dispatchers.IO).launch {
                    // Perform database filtering operation on a background thread
//                    val filteredDogs = dogs.filter { it.isOnline && it.latitude != 0.0 }

                    // Update the UI on the main thread after the operation is complete
                    withContext(Dispatchers.Main) {
                        this@CompassFragment.dogs.clear()
                        this@CompassFragment.dogs.addAll(dogs)

                        adapterDog?.notifyDataSetChanged()

                    }

                }
            }
        }

    }

    private fun updateDogSelectionInDatabase(dog: MutableList<Dog>) {
        activity?.runOnUiThread {
            viewModel?.updateAllDogs(dog)
        }
    }

    private fun initRecyclerViewCompass() {
        adapterDog = DogAdapter(dogs) { position ->
            handleDogSelection(position)
        }
        binding.recyclerviewDogCompass.apply {
            adapter = adapterDog
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            isNestedScrollingEnabled = false
        }
        val animator = binding.recyclerviewDogCompass.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun handleDogSelection(selectedDog: Dog) {

        Log.d("handleDogSelection","handleDogSelection");
        dogs.forEach { it.isSelected = false }
        selectedDog.isSelected = true
        CoroutineScope(Dispatchers.IO).launch {
            updateDogSelectionInDatabase(dogs)
            withContext(Dispatchers.Main) {

            }
        }

        adapterDog?.notifyDataSetChanged()
    }

    override fun onDogData(dogs: List<Dog>) {
        if (!isAdded) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val temp = ArrayList<Dog>()

            dogs.forEach { dog ->
                val existingDog = this@CompassFragment.dogs.find { it.imei == dog.imei }
                if (existingDog != null) {
                    dog.isSelected = existingDog.isSelected
                    dog.levelSanction = existingDog.levelSanction
                }
                temp.add(dog)
            }

            withContext(Dispatchers.Main) {
                if (isAdded) {


                }
            }
        }

    }


//    override fun onDogUpdate() {
//        super.onDogUpdate()
//        Toast.makeText(requireContext() , "Please pull down to refresh the page and obtain the most recent dog information" , Toast.LENGTH_SHORT)
//            .show()
//    }

    override fun onResume() {
        super.onResume()

    }


    override fun onPause() {
        super.onPause()

    }
}