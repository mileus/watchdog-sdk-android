package com.mileus.watchdog.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Wraps all text keys required by One Time Search
 */
@Parcelize
data class OneTimeSearchStringKeys(
    val keyMatchIntroBanner: String
) : Parcelable
