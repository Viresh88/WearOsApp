package com.example.wearosapp.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = [ForeignKey(
    entity = Fence::class ,
    parentColumns = arrayOf("fenceId") ,
    childColumns = arrayOf("fk_fenceId"),
    onUpdate = ForeignKey.CASCADE
)] , tableName = "position_table")
data class Position(

    var latitude: Double = 0.0,
    var longitude: Double = 0.0,


    @ColumnInfo(name = "fk_fenceId" , index = true)
    var fk_fenceId : Long = 0,

    var altitude: Double = 0.00,

    @PrimaryKey(autoGenerate = true)
    var positionId: Long = System.currentTimeMillis()) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble() ,
        parcel.readDouble() ,
        parcel.readLong() ,
        parcel.readDouble() ,
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeLong(fk_fenceId)
        parcel.writeDouble(altitude)
        parcel.writeLong(positionId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Position> {
        override fun createFromParcel(parcel: Parcel): Position {
            return Position(parcel)
        }

        override fun newArray(size: Int): Array<Position?> {
            return arrayOfNulls(size)
        }
    }
}

