package com.mileus.watchdog.ui

import android.webkit.JavascriptInterface
import com.mileus.sdk.R

/**
 * The activity for the Mileus market validation screen. Use
 * [com.mileus.watchdog.MileusWatchdog.startMarketValidationActivity] to start this activity
 */
class MileusMarketValidationActivity : MileusActivity() {

    override val mode = "market_validation"

    override val defaultToolbarText: String
        get() = resources.getString(R.string.market_validation_title)

    @JavascriptInterface
    fun finishMarketValidation() = finish()
}
