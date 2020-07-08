package com.mileus.watchdog.ui

import android.os.Bundle
import android.webkit.JavascriptInterface

class MileusMarketValidationActivity : MileusActivity() {

    override val mode = "market_validation"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webview?.addJavascriptInterface(this, "MileusNative")
    }

    @JavascriptInterface
    fun finishMarketValidation() = finish()
}
