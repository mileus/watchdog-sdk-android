package com.mileus.watchdog.ui

import android.os.Bundle
import android.webkit.JavascriptInterface
import com.mileus.sdk.R

class MileusMarketValidationActivity : MileusActivity() {

    override val mode = "market_validation"

    override val toolbarText: String
        get() = resources.getString(R.string.market_validation_title)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webview?.addJavascriptInterface(this, "MileusNative")
    }

    @JavascriptInterface
    fun finishMarketValidation() = finish()
}
