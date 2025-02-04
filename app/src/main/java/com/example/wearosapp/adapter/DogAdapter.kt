package com.example.wearosapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wearosapp.R
import com.example.wearosapp.model.Dog2

class DogAdapter : RecyclerView.Adapter<DogAdapter.DogViewHolder>() {

    private var dogs = listOf<Dog2>()
    private var onDogSelectedListener: ((Dog2) -> Unit)? = null

    fun setDogs(newDogs: List<Dog2>) {
        dogs = newDogs
        notifyDataSetChanged()
    }

    fun setOnDogSelectedListener(listener: (Dog2) -> Unit) {
        onDogSelectedListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dog, parent, false)
        return DogViewHolder(view)
    }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        holder.bind(dogs[position])
    }

    override fun getItemCount() = dogs.size

    inner class DogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dogImage: ImageView = itemView.findViewById(R.id.dogImage)
        private val dogName: TextView = itemView.findViewById(R.id.dogName)
        private val beepProgress: ProgressBar = itemView.findViewById(R.id.beepProgress)
        private val beepText: TextView = itemView.findViewById(R.id.beepText)
        private val selectedCheck: CheckBox = itemView.findViewById(R.id.selectedCheck)

        fun bind(dog: Dog2) {
            dogImage.setImageResource(dog.imageResId)
            dogName.text = dog.name
            beepProgress.progress = dog.beepProgress
            beepText.text = dog.beepSound
            selectedCheck.isChecked = dog.isSelected

            selectedCheck.setOnCheckedChangeListener { _, isChecked ->
                dog.isSelected = isChecked
                onDogSelectedListener?.invoke(dog)
            }
        }
    }
}