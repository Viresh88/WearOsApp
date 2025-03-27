package com.example.wearosapp.adapter

import android.annotation.SuppressLint
import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wearosapp.R
import com.example.wearosapp.databinding.ItemDogBinding
import com.example.wearosapp.model.Dog
import com.example.wearosapp.model.Dog2
import java.text.SimpleDateFormat
import java.util.Calendar


class DogAdapter(
    private var dataDogList: MutableList<Dog>,
    private val handleSave: (Dog) -> Unit
) : RecyclerView.Adapter<DogAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): ViewHolder {
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
                val (bitmap, position) = dog.getDogIconBitmapWithStatus(context)

                if (bitmap != null) {
                    binding.imageViewDog.setImageBitmap(bitmap)
                } else {
                    println("No bitmap available for position: $position")
                }
                if(position != 0 && position !=3){
                    binding.textviewSpeed.visibility = View.VISIBLE

                }else{
                    binding.textviewSpeed.visibility = View.GONE
                }

                showOnlineDogInfo(dog)

        }

        @SuppressLint("SetTextI18n")
        private fun showOnlineDogInfo(dog: Dog) {

            binding.textviewDogName.text = dog.name
            binding.textviewDogPower.text = "${dog.power}%"
            binding.textviewTime.text = getCurrentFormattedTime()

        }
        fun getCurrentFormattedTime(): String {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            return dateFormat.format(calendar.time)
        }

    }
}
