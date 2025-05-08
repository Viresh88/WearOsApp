package com.example.wearosapp.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.wearosapp.R
import com.example.wearosapp.databinding.ItemBluetoothBinding
import com.example.wearosapp.model.Device

class BluetoothAdapterDevice(
    private var data: MutableList<Device> ,
    private val itemClicked: (position: Int) -> Unit ,
    private val parameterClicked: (position: Int) -> Unit
) : RecyclerView.Adapter<BluetoothAdapterDevice.ViewHolder>() {
    private var isScrolling = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBluetoothDeviceBinding = ItemBluetoothBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(itemBluetoothDeviceBinding, itemClicked, parameterClicked)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = data[position]
        holder.itemDevice(item)

        holder.binding.main.isClickable = true
        holder.binding.main.isFocusable = true
        holder.binding.imageViewBluetooth.setOnCheckedChangeListener(null)
        //holder.binding.imageViewBluetooth.isChecked = item.isChecked

        holder.binding.imageViewBluetooth.setOnCheckedChangeListener { _, isChecked ->
            itemClicked(position)
        }





        holder.binding.bluetoothSettingsIcon.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                parameterClicked(position)
                return@setOnTouchListener true
            }
            false
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setScrolling(scrolling: Boolean) {
        isScrolling = scrolling
    }

    @SuppressLint("ClickableViewAccessibility")
    class ViewHolder(
        val binding: ItemBluetoothBinding,
        private val itemClicked: (position: Int) -> Unit,
        private val parameterClicked: (position: Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var connectStatus = ""
        private var color = ""






        fun itemDevice(result: Device) {
            when (result.bluetoothStatus) {
                itemView.context.getString(R.string.connect_fail) -> {
                    connectStatus = itemView.context.getString(R.string.connect_fail)
                    color = "#FF0000"
                }

                itemView.context.getString(R.string.connecting) -> {
                    connectStatus = itemView.context.getString(R.string.connecting)
                    color = "#FFA500"
                }

                itemView.context.getString(R.string.connected) -> {
                    connectStatus = itemView.context.getString(R.string.connected)
                    color = "#008000"
                }

                itemView.context.getString(R.string.disconnect) -> {
                    connectStatus = itemView.context.getString(R.string.disconnect)
                    color = "#000000"
                }
            }

            binding.deviceName.text = result.name ?: "Unnamed"
            binding.connectStatus.text = connectStatus

            if (color.isNotEmpty()) {
                //binding.imageViewBluetooth.setColorFilter(Color.parseColor(color))
                binding.connectStatus.setTextColor(Color.parseColor(color))
            }
        }


    }
}
