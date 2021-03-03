package com.mileus.watchdog.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * A geographical location representation used within the Mileus SDK.
 * Contains geographical coordinates, postal address, and accuracy.
 */
@Parcelize
data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: Address?,
    val accuracy: Float = 0f
) : Parcelable

/**
 * A Mileus SDK representation of a postal address.
 * Preferably the first line should contain the building number and street name, and the second line
 * should contain anything you write below the street name.
 */
@Parcelize
data class Address(
    val firstLine: String,
    val secondLine: String?
) : Parcelable
