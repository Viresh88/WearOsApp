package com.example.wearosapp.bluetooth

import android.os.SystemClock
import com.example.wearosapp.model.Dog
import kotlin.concurrent.thread

object WriteCommand {

    fun sendPassword(password: String) {
        thread {
            SystemClock.sleep(100)
            BluetoothManagerClass.sendPassword(password)
        }
    }

    fun renameBle() {

    }

    fun pet(dogs :  ArrayList<Dog>, cmdType: String, level: String = "00") {
        val command = StringBuffer()
        command.append(KeyCommand.PET)
        command.append(cmdType)
        command.append(level)
        dogs.forEach {
            command.append(if (it.isSelected) "1" else "0")
        }
        BluetoothManagerClass.writeData(command.toString())
    }

    fun getAllTrail() {
        BluetoothManagerClass.writeData(KeyCommand.TRAIL)
    }
}