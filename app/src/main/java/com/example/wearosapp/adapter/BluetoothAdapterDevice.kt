package com.example.wearosapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wearosapp.R
import com.example.wearosapp.databinding.ItemBluetoothBinding
import com.example.wearosapp.model.Device

class BluetoothAdapterDevice(
    private var devices: MutableList<Device> = mutableListOf(),
    private val itemClicked: ((Int) -> Unit)? = null,
    private val parameterClicked: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<BluetoothAdapterDevice.BluetoothViewHolder>() {

    fun setDevices(newDevices: List<Device>) {
        devices = newDevices.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothViewHolder {
        val binding = ItemBluetoothBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BluetoothViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BluetoothViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount(): Int = devices.size

    inner class BluetoothViewHolder(private val binding: ItemBluetoothBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var statusText = ""
        private var color = ""

        init {
            // Set click listener on the entire item view.
            binding.root.setOnClickListener {
                itemClicked?.invoke(bindingAdapterPosition)
            }
            // Set click listener for the settings icon.
            binding.bluetoothSettingsIcon.setOnClickListener {
                parameterClicked?.invoke(bindingAdapterPosition)
            }
        }

        fun bind(device: Device) {
            val context = binding.root.context

            when (device.bluetoothStatus) {
                context.getString(R.string.connect_fail) -> {
                    statusText = context.getString(R.string.connect_fail)
                    color = "#FF0000"
                }
                context.getString(R.string.connecting) -> {
                    statusText = context.getString(R.string.connecting)
                    color = "#FFA500"
                }
                context.getString(R.string.connected) -> {
                    statusText = context.getString(R.string.connected)
                    color = "#008000"
                }
                context.getString(R.string.disconnect) -> {
                    statusText = context.getString(R.string.disconnect)
                    color = "#000000"
                }
                else -> {
                    statusText = device.bluetoothStatus ?: ""
                    color = "#000000"
                }
            }

            binding.deviceName.text = device.name ?: "Unnamed"
            binding.connectStatus.text = statusText

            if (color.isNotEmpty()) {
                binding.imageViewBluetooth.setColorFilter(Color.parseColor(color))
                binding.connectStatus.setTextColor(Color.parseColor(color))
            }
        }

    }

}
