package com.example.wearosapp.bluetooth

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import com.example.wearosapp.inteface.bluetooth.OnFormatData
import com.example.wearosapp.model.Dog
import com.example.wearosapp.model.DogTrajectory
import com.example.wearosapp.viewmodel.DogViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

object FormatCommand {
    private var timeZone = TimeZone.getDefault().getOffset(System.currentTimeMillis())
    private var dataBytes = ArrayList<Byte>()
    private var isR = false
    private var isN = false
    var onFormatData : OnFormatData? = null
    private var isTrailStart = false
    private var deviceVer: String? = null
    private var viewModel: DogViewModel? = null
    private var dogs = ArrayList<Dog>()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var appContext: Context

    fun setViewModel(viewModel: DogViewModel) {
        FormatCommand.viewModel = viewModel

    }

    fun format(byteArray: ByteArray) {
        synchronized(FormatCommand::class.java) {
            for (index in byteArray.indices) {
                val it = byteArray[index]
                if (it.toInt() == '\r'.code) isR = true
                if (it.toInt() == '\n'.code) isN = true
                dataBytes.add(it)

                if (isR) {
                    if (dataBytes[0].toInt() == 10) dataBytes.removeAt(0)
                    val data = String(dataBytes.toByteArray())

                    formatData(data.trim())
                    onFormatData?.onData(data.trim())
                    clean()
                    if (index != byteArray.size - 1) {
                        format(
                            byteArray.copyOfRange(index + 1, byteArray.size),
                        )
                        return
                    }
                }
            }
        }
    }


    private fun formatData(data: String) {
        val dataList = ArrayList<String>()
        val indexOf = data.indexOf("#")
        if (data.contains("Pass#0" , true)) {
            onFormatData?.onCorrectPassword()
        }


        if (data.contains("Pass#1" , true)) {
            onFormatData?.onIncorrectPassword()
        }
        if (data.contains("Trail start" , true)) {
            isTrailStart = true
            onFormatData?.onTrailDataStart()
        }
        if (data.contains("Trail end" , true)) {

            isTrailStart = false
            onFormatData?.onTrailDataEnd()
        }



        if (indexOf == -1) {
            return
        }

        val head = data.substring(0 , indexOf)
        dataList.add(head)

        if (data.contains("Rename#" , true)) {
            onFormatData?.onRename(dataList)
        }

        when (head) {
            KeyCommand.VER -> {
                val ver = data.substring(indexOf + 1 , data.length)
                deviceVer = ver
                val logMessage = "Command: VER, Version: $ver"
                //writeLog(logMessage)
            }

            KeyCommand.F04 -> {

                val split = data.split("#")
                if (split.size >= 2) {
                    val dogData = split[1]
                    val logMessage = "Command: F04, Version: $dogData"
                   // writeLog(logMessage)
                    if (dogData.contains(",")) {
                        val dogSplit = dogData.split(",")
                        val dogImei = dogSplit[0].trim()
                        val dogName = dogSplit[1].trim()
                        val color = dogSplit.getOrNull(2)?.toIntOrNull() ?: 0
                        val ledLight = dogSplit.getOrNull(3)?.toIntOrNull() ?: 0
                        val collarVersion = dogSplit.getOrNull(4)?.toIntOrNull() ?: 0
                        val existingDog = dogs.find { it.imei == dogImei }
                        if (existingDog != null) {
                            existingDog.name = dogName
                            existingDog.color = color
                            existingDog.ledLight = ledLight
                            existingDog.collarVersion = collarVersion
                            viewModel?.updateDog(existingDog)
                            onFormatData?.onDogData(dogs)

                        } else {
                            val newDog = Dog(dogImei , dogName , color , ledLight , collarVersion)
                            dogs.add(newDog)
                            viewModel?.insertNewDog(newDog)
                            onFormatData?.onDogData(dogs)

                        }
                    }
                }
            }

            KeyCommand.F03 -> {
                val split = data.split("#")
                val logMessage = "Command: F03, Version: $data"
                //writeLog(logMessage)
                if (split.size >= 2) {
                    val dogImei = split[1]
                    val dog = dogs.singleOrNull { it.imei == dogImei }
                    if (dog != null) {
                        dogs.remove(dog)
                        viewModel?.deleteDog(dog.id)
                        onFormatData?.onDogData(dogs)
                    }
                }
            }

            KeyCommand.F02 -> {


                val split = data.split("#")
                val logMessage = "Command: F03, Version: $data"
                writeLog(logMessage)
                if (split.size >= 5) {
                    val dogData = split[1]
                    val dogsList = dogData.split("|")
                    for (dogInfo in dogsList) {
                        if (dogInfo.contains(",")) {
                            val dogSplit = dogInfo.split(",")
                            if (dogSplit.size >= 5) {
                                val dogImei = split[0]
                                val dogName = split[1]
                                val color = split[2].toIntOrNull() ?: 0
                                val ledLight = split[3].toIntOrNull() ?: 0
                                val collarVersion = split[4].toIntOrNull() ?: 0

                                val existingDog = dogs.find { it.imei == dogImei }
                                if (existingDog != null) {
                                    viewModel?.updateDog(existingDog)
                                } else {
                                    val newDog = Dog(dogImei , dogName , color , ledLight , collarVersion)
                                    dogs.add(newDog)
                                    viewModel?.insertNewDog(newDog)
                                }
                            }
                        } else {
                            val dogSplit = dogInfo.split("|")
                            if (dogSplit.size >= 3) {
                                val dogImei = dogSplit[0]
                                val dogName = dogSplit[1]

                                val existingDog = dogs.find { it.imei == dogImei }
                                if (existingDog != null) {
                                    viewModel?.updateDog(existingDog)
                                } else {
                                    val newDog = Dog(dogImei , dogName)
                                    dogs.add(newDog)
                                    viewModel?.insertNewDog(newDog)
                                }
                            }
                        }
                    }
                }
                onFormatData?.onDogData(dogs)
            }

            KeyCommand.F01 -> {
                val logMessage = "Command: F01, Version: $data"
               // writeLog(logMessage)
                dataList.add(data.substring(indexOf + 1 , indexOf + 2))
                val split = data.split("|")
                if (split.size >= 2) {
                    for (index in 1 until split.size) {
                        dataList.add(split[index])
                    }
                }

                dogs.clear()
                dogs.addAll(dataToDogs(dataList))
                onFormatData?.onDogData(dogs)
                viewModel?.insertNewDogs(dogs)

            }

            KeyCommand.GPS -> {
                val logMessage = "Command: GPS, Version: $data"
               // writeLog(logMessage)
                dataList.add(data.substring(indexOf + 1 , indexOf + 16))
                val split = data.split(",")
                if (split.size < 7) return

                var latitude = split[1]
                val latitudeDirection = split[2]

                if (latitudeDirection.equals("S" , true)) {
                    latitude = (-latitude.toDouble()).toString()
                }

                var longitude = split[3]

                val longitudeDirection = split[4]
                if (longitudeDirection.equals("W" , true)) {
                    longitude = (-longitude.toDouble()).toString()
                }

                val altitude = split[5]

                val power = split[6]
                val status = if (split.size == 9) {
                    split[7]
                } else {
                    "00"
                }

                val timeString = if (split.size >= 9) {
                    split[8]
                } else {
                    split[7]
                }

                val deviceTimeMillis = getDeviceTimeMillis(timeString)

                dataList.add(latitude)
                dataList.add(latitudeDirection)
                dataList.add(longitude)
                dataList.add(longitudeDirection)
                dataList.add(altitude)
                dataList.add(power)
                dataList.add(status)
                dataList.add(deviceTimeMillis.toString())

                val dogTrajectory = dataToDogTrajectory(dataList)

                viewModel!!.updateDogTrajectory(dogTrajectory,dogs)
            }
        }
    }

    private fun updateDogTrajectory(dogTrajectory: DogTrajectory) {
        handler.post {
            val dogToUpdate = dogs.singleOrNull { it.imei == dogTrajectory.imei }
            if (dogToUpdate != null) {
                dogToUpdate.latitude = dogTrajectory.latitude
                dogToUpdate.longitude = dogTrajectory.longitude
                dogToUpdate.power = dogTrajectory.batteryPower
                dogToUpdate.status = dogTrajectory.status
                dogToUpdate.time = dogTrajectory.dateTime
            }
            onFormatData?.onDogData(dogs)
            onFormatData?.onGpsData(dogTrajectory)
            viewModel?.createNewTrajectory(dogTrajectory)
            viewModel?.updateAllDogs(dogs)
        }
    }


    fun convertUtcToLocal(utcTime: String, utcFormat: String, localFormat: String): String {
        val dateFormatUtc = SimpleDateFormat(utcFormat, Locale.getDefault())
        dateFormatUtc.timeZone = TimeZone.getTimeZone("UTC")
        val utcDate = dateFormatUtc.parse(utcTime)

        val dateFormatLocal = SimpleDateFormat(localFormat, Locale.getDefault())
        dateFormatLocal.timeZone = TimeZone.getDefault() // Use the device's default time zone
        return dateFormatLocal.format(utcDate)
    }

    @Synchronized
    private fun dataToDogs(dataToDogList: ArrayList<String>): ArrayList<Dog> {
        val dogs = ArrayList<Dog>()

        if (dataToDogList[0] == KeyCommand.F01 && dataToDogList.size >= 3) {
            for (index in 3 until dataToDogList.size) {
                val split = dataToDogList[index].split(",")

                if (split.size >= 5) {
                    val dogImei = split[0]
                    val dogName = split[1]
                    val colorVersion = split[2].toIntOrNull() ?: 0
                    val color = getColorForIndex(colorVersion) ?: Color.BLACK
                    val ledLight = split[3].toIntOrNull() ?: 0
                    val collarVersion = split[4].toIntOrNull() ?: 0

                    dogs.add(Dog(dogImei , dogName , color , ledLight , collarVersion))
                } else if (split.size >= 2) {
                    val dogImei = split[0]
                    val dogName = split[1]
                    val color = generateRandomColor()
                    dogs.add(Dog(dogImei , dogName,color))
                }
            }
        }

        return dogs
    }

    private fun getColorForIndex(colorIndex: Int): Int? {
        return when (colorIndex) {
            0 -> Color.rgb(0, 123, 75)
            1 -> Color.rgb(120, 214, 75)
            2 -> Color.rgb(0, 193, 212)
            3 -> Color.rgb(0, 105, 177)
            4 -> Color.rgb(86, 61, 130)
            5 -> Color.rgb(227, 28, 121)
            6 -> Color.rgb(214, 0, 28)
            7 -> Color.rgb(246, 183, 0)
            else -> null
        }
    }

    fun generateRandomColor(): Int {
        val random = java.util.Random()

        // Generate random values for red, green, and blue components
        val red = random.nextInt(256)
        val green = random.nextInt(256)
        val blue = random.nextInt(256)

        // Combine the components to create the RGB color
        return Color.rgb(red, green, blue)
    }


    @SuppressLint("SimpleDateFormat")
    private fun getDeviceTimeMillis(timeString: String): Long? {
        val format = SimpleDateFormat("yyyyMMddHHmmss")
        val phoneTimeMillis = System.currentTimeMillis()
        var deviceTimeMillis : Long?

        try {
            deviceTimeMillis = format.parse(timeString)?.time ?: phoneTimeMillis
            if (deviceTimeMillis < (format.parse("20200101000000")?.time ?: 0)) {
                deviceTimeMillis = phoneTimeMillis
            } else {
                if (deviceVer != null) {
                    deviceTimeMillis += timeZone
                } else {
                    if (abs(timeZone - (phoneTimeMillis - deviceTimeMillis)) < 300 * 1000) {
                        deviceTimeMillis += timeZone
                    }
                }
            }

            return deviceTimeMillis
        } catch (e: ParseException) {
            e.printStackTrace()
            return null
        }
    }

    private fun dataToDogTrajectory(data: ArrayList<String>): DogTrajectory {
        val imei = data[1]
        val latitude = data[2].toDouble()
        val longitude = data[4].toDouble()
        val altitude = data[6].toDouble()
        val power = data[7].toInt()
        val status = data[8].toInt()
        val time = data[9].toLong()

        return DogTrajectory(imei, latitude, longitude, altitude, power, status, time)
    }

    private fun clean() {
        dataBytes.clear()
        isR = false
        isN = false
    }

    fun writeLog(logMessage: String) {
        val logFile = File(appContext.filesDir, "device_commands_log.txt")

        try {
            val outputStream = FileOutputStream(logFile, true) // 'true' to append to the file
            outputStream.write("$logMessage\n".toByteArray())
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun setContext(context: Context) {
        appContext = context
    }

}