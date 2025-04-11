package com.example.wearosapp.adapter

import android.annotation.SuppressLint
import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wearosapp.databinding.ItemDogBinding
import com.example.wearosapp.model.Dog
import java.text.SimpleDateFormat
import java.util.Calendar

class DogAdapter(
    private var dataDogList: MutableList<Dog>,
    private val handleSave: (Dog) -> Unit
) : RecyclerView.Adapter<DogAdapter.ViewHolder>() {

    var currentLocation: Location? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemDogBinding =
            ItemDogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemDogBinding, handleSave)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataDogList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return dataDogList.size
    }

    inner class ViewHolder(
        private val binding: ItemDogBinding,
        private val handleSave: (Dog) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("NotifyDataSetChanged")
        fun bind(dog: Dog) {
            val context = itemView.context
            binding.checkboxSelectDog.isChecked = dog.isSelected
            binding.checkboxSelectDog.setOnClickListener {
                dataDogList.forEach { it.isSelected = false }
                dog.isSelected = true
                handleSave(dog)
            }
            binding.itemContainer.setOnClickListener {
                dataDogList.forEach { it.isSelected = false }
                dog.isSelected = true
                handleSave(dog)
            }

            // Hide the whole item if the dog's location is not valid.
            if (dog.latitude == 0.0 && dog.longitude == 0.0) {
                binding.itemContainer.visibility = View.GONE
            } else {
                binding.itemContainer.visibility = View.VISIBLE

                // Set dog image (using your getDogIconBitmapWithStatus implementation)
                val (bitmap, position) = dog.getDogIconBitmapWithStatus(context)
                if (bitmap != null) {
                    binding.imageViewDog.setImageBitmap(bitmap)
                } else {
                    println("No bitmap available for position: $position")
                }

                // Show speed only if position value indicates (for example, if not equal to 0 or 3)
                if (position != 0 && position != 3) {
                    binding.textviewSpeed.visibility = View.VISIBLE
                    val speed = calculateDogSpeed(dog)
                    binding.textviewSpeed.text = speed
                } else {
                    binding.textviewSpeed.visibility = View.GONE
                }

                // Display online dog information (name, power, time, distance)
                showOnlineDogInfo(dog)
            }
        }

        @SuppressLint("SetTextI18n")
        private fun showOnlineDogInfo(dog: Dog) {
            binding.textviewDogName.text = dog.name
            binding.textviewDogPower.text = "${dog.power}%"
            binding.textviewTime.text = getCurrentFormattedTime()

            // Only display the distance if the dog's coordinates are valid.
            if (dog.latitude == 0.0 && dog.longitude == 0.0) {
                binding.itemContainer.visibility = View.GONE
            } else {
                // Calculate and display the distance regardless of selection state.
                val distance = calculateDistance(dog)
                binding.textviewDistance.text = distance
                binding.textviewDistance.visibility = View.VISIBLE
            }
        }

        fun getCurrentFormattedTime(): String {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            return dateFormat.format(calendar.time)
        }

        fun calculateDogSpeed(dog: Dog): String {


            val currentTime = System.currentTimeMillis()

            // Check if current position is available
            if (dog.latitude != 0.0 && dog.longitude!= 0.0) {

                // Create a Location object for the dog's current position
                val dogLocation = Location("Dog")
                dogLocation.latitude = dog.latitude
                dogLocation.longitude = dog.longitude

                // Check if previous position is valid (not 0.0 for both latitude and longitude)
                if (dog.preLatitude != 0.0 && dog.preLongitude != 0.0) {
                    // Create a Location object for the dog's previous position
                    val previousDogLocation = Location("PreviousDog")
                    previousDogLocation.latitude = dog.preLatitude
                    previousDogLocation.longitude = dog.preLongitude

                    // Calculate the distance between the previous and current positions
                    val results = FloatArray(1)
                    Location.distanceBetween(
                        previousDogLocation.latitude, previousDogLocation.longitude,
                        dogLocation.latitude, dogLocation.longitude,
                        results
                    )
                    val distance = results[0]

                    // Calculate the time difference in seconds
                    val timeDifference = (currentTime - dog.time) / 1000.0 // Time difference in seconds

                    // Avoid division by zero if the time difference is too small (i.e., dog hasn't moved)
                    if (timeDifference > 0) {
                        // Calculate the speed in meters per second
                        val speed = distance / timeDifference

                        // Convert speed to kilometers per hour
                        val speedInKmh = speed * 3.6 // Convert meters per second to kilometers per hour
                        return String.format("%.2f km/h", speedInKmh)
                    } else {
                        // If timeDifference is 0 or too small, return "Speed unavailable"
                        return "Speed unavailable"
                    }
                } else {
                    // If previous location is invalid, return "Speed unavailable"
                    return "Speed unavailable"
                }
            }

            // If current position is not available, return "Speed unavailable"
            return "Speed unavailable"
        }


        private fun calculateDistance(dog: Dog): String {
            val currentLocation = this@DogAdapter.currentLocation
            if (currentLocation != null) {
                val dogLocation = Location("Dog").apply {
                    latitude = dog.latitude
                    longitude = dog.longitude
                }
                val results = FloatArray(1)
                Location.distanceBetween(
                    currentLocation.latitude,
                    currentLocation.longitude,
                    dogLocation.latitude,
                    dogLocation.longitude,
                    results
                )
                val distance = results[0]
                return if (distance < 1000) {
                    "${distance.toInt()}m"
                } else {
                    val km = distance / 1000.0
                    String.format("%.2fkm", km)
                }
            } else {
                // Provide a default message when location is unavailable.
                return "Distance unavailable"
            }
        }
    }
}
