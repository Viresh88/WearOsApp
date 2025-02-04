package com.example.wearosapp.fragment

import android.hardware.SensorManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.test.isSelected
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.WearableRecyclerView
import com.example.wearosapp.R
import com.example.wearosapp.adapter.DogAdapter
import com.example.wearosapp.model.Dog2
import com.example.wearosapp.view.CustomCompassView
import com.example.wearosapp.view.DogCompassView


class FragmentCompass : Fragment() {
    private lateinit var compassView: DogCompassView
    private lateinit var tvDegree: TextView
    private lateinit var dogAdapter: DogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_compass, container, false)
        compassView = view.findViewById(R.id.dogCompassView)
        tvDegree = view.findViewById(R.id.rotationTextView)
        val recyclerView = view.findViewById<WearableRecyclerView>(R.id.recyclerview_dog_compass)

        dogAdapter = DogAdapter().apply {
            setOnDogSelectedListener { dog ->
                // Update compass view when dog selection changes
                val selectedDogs = getDemoDogs().filter { it.isSelected }

            }

        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = dogAdapter
            isEdgeItemsCenteringEnabled = true
        }
        dogAdapter.setDogs(getDemoDogs())


        return view


    }

    private fun getDemoDogs() = listOf(
        Dog2(
            id = 1,
            name = "Granger Doggie",
            imageResId = R.drawable.dog,
            beepProgress = 85,
            beepSound = "Beep Sound"
        ),
        Dog2(
            id = 2,
            name = "Max",
            imageResId = R.drawable.dog,
            beepProgress = 85,
            beepSound = "Beep Sound"
        ),
        Dog2(
            id = 3,
            name = "Luna",
            imageResId = R.drawable.dog,
            beepProgress = 92,
            beepSound = "Beep Sound"
        )
    )
}