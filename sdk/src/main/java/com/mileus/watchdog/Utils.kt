package com.mileus.watchdog

import android.content.Intent
import android.os.Bundle
import com.mileus.watchdog.data.Location

internal var Bundle.token: String
    get() = getString(BundleKeys.TOKEN) ?: ""
    set(value) {
        putString(BundleKeys.TOKEN, value)
    }

internal var Bundle.location: Location?
    get() = getParcelable(BundleKeys.LOCATION)
    set(value) {
        putParcelable(BundleKeys.LOCATION, value)
    }

internal var Bundle.origin: Location?
    get() = getParcelable(BundleKeys.ORIGIN)
    set(value) {
        putParcelable(BundleKeys.ORIGIN, value)
    }

/**
 * The current origin location. Will be passed to any location search activity.
 */
var Bundle.currentOrigin: Location?
    get() = getParcelable(MileusWatchdog.CURRENT_ORIGIN_EXTRA)
    internal set(value) {
        putParcelable(MileusWatchdog.CURRENT_ORIGIN_EXTRA, value)
    }

internal var Bundle.destination: Location?
    get() = getParcelable(BundleKeys.DESTINATION)
    set(value) {
        putParcelable(BundleKeys.DESTINATION, value)
    }

/**
 * The current destination location. Will be passed to any location search activity.
 */
var Bundle.currentDestination: Location?
    get() = getParcelable(MileusWatchdog.CURRENT_DESTINATION_EXTRA)
    internal set(value) {
        putParcelable(MileusWatchdog.CURRENT_DESTINATION_EXTRA, value)
    }

internal var Bundle.home: Location?
    get() = getParcelable(BundleKeys.HOME)
    set(value) {
        putParcelable(BundleKeys.HOME, value)
    }

/**
 * The current home location. Will be passed to any location search activity.
 */
var Bundle.currentHome: Location?
    get() = getParcelable(MileusWatchdog.CURRENT_HOME_EXTRA)
    internal set(value) {
        putParcelable(MileusWatchdog.CURRENT_HOME_EXTRA, value)
    }

/**
 * The search type, will be passed to any location search activity. Can be
 * [MileusWatchdog.SEARCH_TYPE_ORIGIN], [MileusWatchdog.SEARCH_TYPE_DESTINATION]
 * or [MileusWatchdog.SEARCH_TYPE_HOME]
 */
var Bundle.searchType: String?
    get() = getString(MileusWatchdog.SEARCH_TYPE)
    set(value) {
        putString(MileusWatchdog.SEARCH_TYPE, value)
    }

internal object BundleKeys {
    const val TOKEN = "TOKEN"
    const val LOCATION = "LOCATION"
    const val ORIGIN = "ORIGIN"
    const val DESTINATION = "DESTINATION"
    const val HOME = "HOME"
}

internal fun Intent.updateExtras(block: Bundle.() -> Unit) = apply {
    val extras = extras ?: Bundle()
    extras.block()
    putExtras(extras)
}
