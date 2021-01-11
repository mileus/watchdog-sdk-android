package com.mileus.watchdog.ui

import android.content.Intent
import android.webkit.JavascriptInterface
import com.mileus.sdk.R
import com.mileus.watchdog.MileusWatchdog

class MileusWatchdogActivity : MileusActivity() {

    companion object {
        const val REQUEST_CODE_ORIGIN_SEARCH = 2501
        const val REQUEST_CODE_DESTINATION_SEARCH = 2502
        const val REQUEST_CODE_HOME_SEARCH = 2503
        const val SEARCH_TYPE_ORIGIN = "origin"
        const val SEARCH_TYPE_DESTINATION = "destination"
        const val SEARCH_TYPE_HOME = "home"
    }

    override val mode = "watchdog"

    override val toolbarText: String
        get() = resources.getString(R.string.market_validation_title)

    private val taxiRideActivityIntent: Intent?
        get() = MileusWatchdog.taxiRideActivityIntent

    @JavascriptInterface
    fun openTaxiRideScreen() = startActivity(taxiRideActivityIntent)

    @JavascriptInterface
    fun openTaxiRideScreenAndFinish() {
        finish()
        startActivity(taxiRideActivityIntent)
    }
}
