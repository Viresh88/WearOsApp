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

            val (bitmap, position) = dog.getDogIconBitmapWithStatus(context)
            if (bitmap != null) {
                binding.imageViewDog.setImageBitmap(bitmap)
            } else {
                println("No bitmap available for position: $position")
            }

            if (position != 0 && position != 3) {
                binding.textviewSpeed.visibility = View.VISIBLE
                val speed = calculateDogSpeed(dog)
                binding.textviewSpeed.text = speed
            } else {
                binding.textviewSpeed.visibility = View.GONE
            }

            showOnlineDogInfo(dog)
        }

        @SuppressLint("SetTextI18n")
        private fun showOnlineDogInfo(dog: Dog) {
            binding.textviewDogName.text = dog.name
            binding.textviewDogPower.text = "${dog.power}%"
            binding.textviewTime.text = getCurrentFormattedTime()

            // Calculate and display the distance using the currentLocation property.
            val distance = calculateDistance(dog)
            binding.textviewDistance.text = distance
        }

        fun getCurrentFormattedTime(): String {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            return dateFormat.format(calendar.time)
        }

        private fun calculateDogSpeed(dog: Dog): String {
            val currentTime = System.currentTimeMillis()
            // Ensure previous location values are valid.
            if (dog.latitude != 0.0 && dog.longitude != 0.0) {
                val currentDogLocation = Location("Dog").apply {
                    latitude = dog.latitude
                    longitude = dog.longitude
                }
                val previousDogLocation = Location("PreviousDog").apply {
                    latitude = dog.latitude
                    longitude = dog.latitude
                }

                val results = FloatArray(1)
                Location.distanceBetween(
                    previousDogLocation.latitude, previousDogLocation.longitude,
                    currentDogLocation.latitude, currentDogLocation.longitude,
                    results
                )
                val distance = results[0]
                val timeDifference = (currentTime - dog.time) / 1000.0 // in seconds

                return if (timeDifference > 0) {
                    val speedInMetersPerSecond = distance / timeDifference
                    val speedInKmh = speedInMetersPerSecond * 3.6
                    String.format("%.2f km/h", speedInKmh)
                } else {
                    "Speed unavailable"
                }
            }
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
                return ""
            }
        }
    }
}
