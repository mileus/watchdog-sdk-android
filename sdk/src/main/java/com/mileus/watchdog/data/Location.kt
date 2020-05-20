package com.mileus.watchdog.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Location(
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f
) : Parcelable
