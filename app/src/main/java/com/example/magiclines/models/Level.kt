package com.example.magiclines.models

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class Level(
    var numLevel: Int,
    var resourceId: Int,
    var isComplete: Boolean,
    var category: Int,
    var star: Int? = null
) : Parcelable {

    override fun describeContents(): Int {
        return 0
    }

}