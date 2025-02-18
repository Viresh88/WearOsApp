package com.example.wearosapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wearosapp.R
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
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bluetooth, parent, false)
        return BluetoothViewHolder(view)
    }

    override fun onBindViewHolder(holder: BluetoothViewHolder, position: Int) {
        holder.bind(devices[position], position)
    }

    override fun getItemCount(): Int = devices.size

    inner class BluetoothViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bluetoothIcon: ImageView = itemView.findViewById(R.id.image_view_bluetooth)
        private val deviceName: TextView = itemView.findViewById(R.id.deviceName)
        private val connectStatus: TextView = itemView.findViewById(R.id.connectStatus)
        private val settingsIcon: ImageView = itemView.findViewById(R.id.bluetooth_settings_icon)

        fun bind(device: Device, position: Int) {
            val context = itemView.context
            var statusText = ""
            var color = ""

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
                    // Optionally handle any other state
                    statusText = device.bluetoothStatus.toString()
                }
            }

            deviceName.text = device.name ?: "Unnamed"
            connectStatus.text = statusText

            if (color.isNotEmpty()) {
                bluetoothIcon.setColorFilter(Color.parseColor(color))
                connectStatus.setTextColor(Color.parseColor(color))
            }

            itemView.setOnClickListener {
                itemClicked?.invoke(position)
            }
            settingsIcon.setOnClickListener {
                parameterClicked?.invoke(position)
            }
        }
    }
}
