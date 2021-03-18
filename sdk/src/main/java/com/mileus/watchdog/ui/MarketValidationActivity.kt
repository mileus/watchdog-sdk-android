package com.mileus.watchdog.ui

import android.webkit.JavascriptInterface

class MarketValidationActivity : MileusActivity() {

    override val initialScreen = Screen.MARKET_VALIDATION

    @JavascriptInterface
    fun finishMarketValidation() = finish()

}