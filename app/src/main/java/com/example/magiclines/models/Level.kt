package com.example.magiclines.models

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import java.io.Serializable

data class Level(
    var numLevel: Int,
    var resourceName: Int,
    var resourceId: Int,
    var isComplete: Boolean,
    var category: String
) : Parcelable {
    private var star: Int? = null

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readString()!!
    ) {
        star = parcel.readValue(Int::class.java.classLoader) as? Int
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(numLevel)
        parcel.writeInt(resourceName)
        parcel.writeInt(resourceId)
        parcel.writeByte(if (isComplete) 1 else 0)
        parcel.writeValue(star)
        parcel.writeString(category)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Level> {
        override fun createFromParcel(parcel: Parcel): Level {
            return Level(parcel)
        }

        override fun newArray(size: Int): Array<Level?> {
            return arrayOfNulls(size)
        }
    }
    fun getStar(): Int? {
        return star
    }

    // Hàm để đặt giá trị star
    fun setStar(star: Int?) {
        this.star = star
    }
}