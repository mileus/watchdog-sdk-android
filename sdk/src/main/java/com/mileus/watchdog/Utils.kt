package com.mileus.watchdog

import android.content.Intent
import android.os.Bundle
import com.mileus.watchdog.data.Location
import com.mileus.watchdog.data.OneTimeSearchStringKeys
import com.mileus.watchdog.ui.MileusActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.resume

// internal activity state:

internal var Bundle.toolbarText: String?
    get() = getString("toolbar")
    set(value) {
        putString("toolbar", value)
    }

internal var Bundle.isInfoIconVisible: Boolean
    get() = getBoolean("infoicon", false)
    set(value) {
        putBoolean("infoicon", value)
    }


// global Mileus SDK state:

internal var Bundle.token: String
    get() = getString(BundleKeys.TOKEN) ?: ""
    set(value) {
        putString(BundleKeys.TOKEN, value)
    }

internal var Bundle.partnerName: String
    get() = getString(BundleKeys.PARTNER_NAME) ?: ""
    set(value) {
        putString(BundleKeys.PARTNER_NAME, value)
    }

internal var Bundle.environment: String
    get() = getString(BundleKeys.ENVIRONMENT) ?: ""
    set(value) {
        putString(BundleKeys.ENVIRONMENT, value)
    }

// activity arguments:

internal var Bundle.location: Location?
    get() = getParcelable(BundleKeys.LOCATION)
    set(value) {
        putParcelable(BundleKeys.LOCATION, value)
    }

internal var Bundle.screen: MileusActivity.Screen?
    get() = MileusActivity.Screen.valueOf(getString(BundleKeys.SCREEN, ""))
    set(value) {
        putString(BundleKeys.SCREEN, value.toString())
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

internal var Bundle.oneTimeSearchStringKeys: OneTimeSearchStringKeys?
    get() = getParcelable(BundleKeys.ONE_TIME_SEARCH_STRING_KEYS)
    set(value) {
        putParcelable(BundleKeys.ONE_TIME_SEARCH_STRING_KEYS, value)
    }

internal object BundleKeys {
    const val TOKEN = "TOKEN"
    const val PARTNER_NAME = "PARTNER_NAME"
    const val ENVIRONMENT = "ENVIRONMENT"
    const val LOCATION = "LOCATION"
    const val SCREEN = "screen"
    const val ORIGIN = "ORIGIN"
    const val DESTINATION = "DESTINATION"
    const val HOME = "HOME"
    const val ONE_TIME_SEARCH_STRING_KEYS = "ONE_TIME_SEARCH_STRING_KEYS"
}

internal fun Intent.updateExtras(block: Bundle.() -> Unit) = apply {
    val extras = extras ?: Bundle()
    extras.block()
    putExtras(extras)
}

internal suspend fun Call.await(): Response = suspendCancellableCoroutine { continuation ->
    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            if (!continuation.isCancelled) {
                continuation.resumeWithException(e)
            }
        }

        override fun onResponse(call: Call, response: Response) {
            continuation.resume(response)
        }
    })

    continuation.invokeOnCancellation {
        try {
            cancel()
        } catch (_: Throwable) {
        }
    }
}
