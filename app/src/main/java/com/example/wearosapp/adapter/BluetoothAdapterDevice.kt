package com.example.wearosapp.adapter

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wearosapp.R
import com.example.wearosapp.databinding.ItemBluetoothBinding
import com.example.wearosapp.model.Device

class BluetoothAdapterDevice(
    private var data: MutableList<Device> ,
    private val itemClicked: (position: Int) -> Unit ,
    private val parameterClicked: (position: Int) -> Unit
) : RecyclerView.Adapter<BluetoothAdapterDevice.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBluetoothDeviceBinding = ItemBluetoothBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(itemBluetoothDeviceBinding, itemClicked, parameterClicked)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.itemDevice(item)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(
        private val binding: ItemBluetoothBinding,
        private val itemClicked: (position: Int) -> Unit,
        private val parameterClicked: (position: Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private var connectStatus = ""
        private var color = ""

        init {
            itemView.setOnClickListener(this)
            binding.bluetoothSettingsIcon.setOnClickListener {
                val position = bindingAdapterPosition
                parameterClicked(position)
            }
        }


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
                binding.imageViewBluetooth.setColorFilter(Color.parseColor(color))
                binding.connectStatus.setTextColor(Color.parseColor(color))
            }
        }

        override fun onClick(view: View?) {
            val position = bindingAdapterPosition

            // 1. Visual bounce
            view?.animate()?.scaleX(0.95f)?.scaleY(0.95f)?.setDuration(100)?.withEndAction {
                view.animate().scaleX(1f).scaleY(1f).duration = 100

                // 2. Vibration
                val vibrator = itemView.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50)
                }

                // 3. Trigger BLE connect logic
                itemClicked(position)
            }?.start()

        }
    }
}