package com.mileus.watchdog.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * A geographical location representation used within the Mileus SDK.
 * Contains geographical coordinates, postal address (two lines are preferred to a one-line address),
 * and accuracy.
 */
@Parcelize
data class Location(
    val addressLine1: String,
    val addressLine2: String?,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f
) : Parcelable
