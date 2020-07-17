package com.mileus.watchdog.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import com.mileus.sdk.R
import com.mileus.watchdog.*
import kotlinx.android.synthetic.main.activity_mileus_watchdog.*

class MileusWatchdogActivity : MileusActivity() {

    companion object {
        const val REQUEST_CODE_ORIGIN_SEARCH = 2501
        const val REQUEST_CODE_DESTINATION_SEARCH = 2502
        const val SEARCH_TYPE_ORIGIN = "origin"
        const val SEARCH_TYPE_DESTINATION = "destination"
    }

    override val mode = "watchdog"

    override val toolbarText: String
        get() = resources.getString(R.string.market_validation_title)

    private val originSearchIntent: Intent?
        get() = MileusWatchdog.originSearchActivityIntent?.updateExtras {
            currentOrigin = this@MileusWatchdogActivity.origin
            currentDestination = this@MileusWatchdogActivity.destination
            searchType = MileusWatchdog.SEARCH_TYPE_ORIGIN
        }

    private val destinationSearchIntent: Intent?
        get() = MileusWatchdog.destinationSearchActivityIntent?.updateExtras {
            currentOrigin = this@MileusWatchdogActivity.origin
            currentDestination = this@MileusWatchdogActivity.destination
            searchType = MileusWatchdog.SEARCH_TYPE_DESTINATION
        }

    private val taxiRideActivityIntent: Intent?
        get() = MileusWatchdog.taxiRideActivityIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webview?.addJavascriptInterface(this, "MileusNative")
    }

    private fun updateLocationsInJs() {
        origin?.let {
            mileus_webview.evaluateJavascript(
                """
                    window.setOrigin({
                        lat: ${it.latitude},
                        lon: ${it.longitude},
                        address: '${it.address.sanitize()}',
                        accuracy: ${it.accuracy}
                    });
                """.trimIndent(),
                null
            )
        }
        destination?.let {
            mileus_webview.evaluateJavascript(
                """
                    window.setDestination({
                        lat: ${it.latitude},
                        lon: ${it.longitude},
                        address: '${it.address.sanitize()}',
                        accuracy: ${it.accuracy}
                    });
                """.trimIndent(),
                null
            )
        }
    }

    @JavascriptInterface
    fun openSearchScreen(searchType: String) {
        when (searchType) {
            SEARCH_TYPE_ORIGIN ->
                startActivityForResult(
                    originSearchIntent,
                    REQUEST_CODE_ORIGIN_SEARCH
                )
            SEARCH_TYPE_DESTINATION ->
                startActivityForResult(
                    destinationSearchIntent,
                    REQUEST_CODE_DESTINATION_SEARCH
                )
        }
    }

    @JavascriptInterface
    fun openTaxiRideScreen() = startActivity(taxiRideActivityIntent)

    @JavascriptInterface
    fun openTaxiRideScreenAndFinish() {
        finish()
        startActivity(taxiRideActivityIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_ORIGIN_SEARCH -> {
                    origin = data?.extras?.location
                    updateLocationsInJs()
                    return
                }
                REQUEST_CODE_DESTINATION_SEARCH -> {
                    destination = data?.extras?.location
                    updateLocationsInJs()
                    return
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}
