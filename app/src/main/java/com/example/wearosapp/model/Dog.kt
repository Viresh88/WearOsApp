package com.example.wearosapp.model

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.wearosapp.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlin.random.Random


@Entity(tableName = "Dog_table")
data class Dog(
    var imei: String? ,
    var name: String? ,
    var color: Int = 0 ,
    var ledLight: Int = 0 ,
    var collarVersion: Int = 0 ,
    var power: Int = 0 ,
    var status: Int = 0 ,
    var angle: Float = 0F ,
    var latitude: Double = 0.0 ,
    var longitude: Double = 0.0 ,
    var time: Long = System.currentTimeMillis() ,
    var isSelected: Boolean = false ,
    var isOnline: Boolean = false ,
    var fenceId: Long = 0 ,
    var levelSanction: Int = 0,
    var received: Int = 0,

    @PrimaryKey(autoGenerate = true)
    var id: Long = imei.hashCode().toLong()
) : Parcelable {
    @Transient
    var map = HashMap<String, Boolean>()

    constructor(parcel: Parcel) : this(
        parcel.readString() ,
        parcel.readString() ,
        parcel.readInt() ,
        parcel.readInt() ,
        parcel.readInt() ,
        parcel.readInt() ,
        parcel.readInt() ,
        parcel.readFloat() ,
        parcel.readDouble() ,
        parcel.readDouble() ,
        parcel.readLong() ,
        parcel.readByte() != 0.toByte() ,
        parcel.readByte() != 0.toByte() ,
        parcel.readLong(),
        parcel.readInt()
    )

    fun getDogIconBitmap(context: Context): Bitmap? {
        val resourceId = if (isOnline) {
            when (status) {
                0 -> R.drawable.dog_sittings
                1 -> R.drawable.dog_standing
                2 -> R.drawable.dog_moving
                3 -> R.drawable.dog_pointing
                else -> R.drawable.dog_pointing
            }
        } else {
            null
        }


        val drawable = resourceId?.let { ContextCompat.getDrawable(context, it) }

        val grayColor = Color.GRAY
        val tintColor = if (color == 0) grayColor else color

        drawable?.let {
            DrawableCompat.setTintList(it, ColorStateList.valueOf(tintColor))
            return drawable.toBitmap()
        }


        return null
    }

    fun getDogIconBitmapWithStatus(context: Context): Pair<Bitmap?, Int> {
        // Determine the resource ID based on online status and dog status
        val resourceId = if (isOnline) {
            when (status) {
                0 -> R.drawable.dog_sittings
                1 -> R.drawable.dog_standing
                2 -> R.drawable.dog_moving
                3 -> R.drawable.dog_pointing
                else -> R.drawable.dog_pointing
            }
        } else {
            null
        }

        val drawable = resourceId?.let { ContextCompat.getDrawable(context, it) }

        val grayColor = Color.GRAY
        val tintColor = if (color == 0) grayColor else color

        drawable?.let {
            DrawableCompat.setTintList(it, ColorStateList.valueOf(tintColor))
            val bitmap = drawable.toBitmap()

            // Return both the bitmap and the status
            return Pair(bitmap, status)
        }

        // Return null for bitmap and the status if drawable is null
        return Pair(null, status)
    }


    fun getDominantColor(context: Context): Int {
        val imeiHashCode = imei?.hashCode() ?: 0
        val random = Random(imeiHashCode)
        val colorIndex = random.nextInt(dogsColors.size)
        val dogIconBitmap = getDogIconBitmap(context) ?: return dogsColors[colorIndex]

        val palette = Palette.from(dogIconBitmap).generate()
        val dominantSwatch = palette.dominantSwatch

        return dominantSwatch?.rgb ?: dogsColors[colorIndex]
    }

    fun getDogIcon(context: Context): BitmapDescriptor? {
        val resourceId = if (isOnline) {
            when (status) {
                0 -> R.drawable.dog_sittings
                1 -> R.drawable.dog_standing
                2 -> R.drawable.dog_moving
                3 -> R.drawable.dog_pointing
                else -> R.drawable.dog_pointing
            }
        } else {
            null
        }

        if (resourceId == null) {
            return null
        }

        val drawable = resourceId.let { ContextCompat.getDrawable(context, it) }

        val grayColor = Color.GRAY
        val tintColor = if (color == 0) grayColor else color

        drawable?.let {
            DrawableCompat.setTintList(it, ColorStateList.valueOf(tintColor))
        }

        return drawable?.toBitmap()?.let { BitmapDescriptorFactory.fromBitmap(it) }

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



    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(imei)
        parcel.writeString(name)
        parcel.writeInt(color)
        parcel.writeInt(ledLight)
        parcel.writeInt(collarVersion)
        parcel.writeInt(power)
        parcel.writeInt(status)
        parcel.writeFloat(angle)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeLong(time)
        parcel.writeByte(if (isSelected) 1 else 0)
        parcel.writeByte(if (isOnline) 1 else 0)
        parcel.writeLong(fenceId)
        parcel.writeInt(levelSanction)
        parcel.writeLong(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Dog> {

        @Transient
        val dogsColors = arrayOf(
            Color.rgb(10, 84, 162), Color.rgb(68, 42, 110), Color.rgb(242, 170, 8), Color.rgb(105, 209, 59),  Color.rgb(202, 0, 22),
            Color.rgb(14, 106, 58), Color.rgb(216, 0, 101), Color.rgb(23, 181, 201), Color.rgb(92, 64, 43), Color.rgb(167, 137, 110),
            Color.rgb(117, 44, 55), Color.rgb(188, 134, 10), Color.rgb(101, 96, 25), Color.rgb(154, 142, 36), Color.rgb(179, 88, 77),
            Color.rgb(217, 147, 158), Color.rgb(145, 119, 180), Color.rgb(106, 143, 184), Color.rgb(102, 46, 111), Color.rgb(0, 123, 75)
        )
        override fun createFromParcel(parcel: Parcel): Dog {
            return Dog(parcel)
        }

        override fun newArray(size: Int): Array<Dog?> {
            return arrayOfNulls(size)
        }
    }
}
