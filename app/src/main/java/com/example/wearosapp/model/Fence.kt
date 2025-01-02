package com.example.wearosapp.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "fence_table")
data class Fence(
    @PrimaryKey(autoGenerate = true)
    var fenceId: Long = System.currentTimeMillis(),
    var name: String="",
    var isOpenFence: Boolean = false,
    var isOpenVibration : Boolean = false ,
    var color : Int = 0
) :Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong() ,
        parcel.readString()!! ,
        parcel.readByte() != 0.toByte() ,
        parcel.readByte() != 0.toByte() ,
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(fenceId)
        parcel.writeString(name)
        parcel.writeByte(if (isOpenFence) 1 else 0)
        parcel.writeByte(if (isOpenVibration) 1 else 0)
        parcel.writeInt(color)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Fence> {
        override fun createFromParcel(parcel: Parcel): Fence {
            return Fence(parcel)
        }

        override fun newArray(size: Int): Array<Fence?> {
            return arrayOfNulls(size)
        }
    }
}
