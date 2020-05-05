package com.mileus.sdk.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.mileus.sdk.*
import com.mileus.sdk.data.Location
import kotlinx.android.synthetic.main.activity_mileus.*

class MileusActivity : AppCompatActivity() {

    companion object {
        // "https://watchdog-web-stage.mileus.com/"
        const val URL_STAGING = "https://mileus.spacek.now.sh/"
        const val URL_PRODUCTION = "https://watchdog-web.mileus.com/"
        const val REQUEST_CODE_ORIGIN_SEARCH = 2501
        const val REQUEST_CODE_DESTINATION_SEARCH = 2502
        const val SEARCH_TYPE_ORIGIN = "origin"
        const val SEARCH_TYPE_DESTINATION = "destination"
    }

    private var origin: Location? = null
    private var destination: Location? = null
    private lateinit var token: String
    private var partnerName = Mileus.partnerName
    private var environment = Mileus.environment

    private val baseUrl: String
        get() = when (environment) {
            Mileus.ENV_PRODUCTION -> URL_PRODUCTION
            else -> URL_STAGING
        }

    private val originSearchIntent: Intent?
        get() = Mileus.originSearchActivityIntent?.updateExtras {
            currentOrigin = this@MileusActivity.origin
            currentDestination = this@MileusActivity.destination
            searchType = Mileus.SEARCH_TYPE_ORIGIN
        }

    private val destinationSearchIntent: Intent?
        get() = Mileus.destinationSearchActivityIntent?.updateExtras {
            currentOrigin = this@MileusActivity.origin
            currentDestination = this@MileusActivity.destination
            searchType = Mileus.SEARCH_TYPE_DESTINATION
        }

    private val taxiRideActivityIntent: Intent?
        get() = Mileus.taxiRideActivityIntent

    private var lastLoadedUrl = ""

    @SuppressLint("SetJavaScriptEnabled", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mileus)
        setTheme(R.style.MileusTheme)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        mileus_toolbar.setNavigationOnClickListener { finish() }

        origin = intent.extras?.origin
        destination = intent.extras?.destination
        token = intent.extras?.token ?: throw IllegalStateException("Missing access token.")
        partnerName = Mileus.partnerName

        savedInstanceState?.let {
            it.origin?.let { origin = it }
            it.destination?.let { destination = it }
        }

        val progressBar = mileus_progress
        val webview = mileus_webview
        val errorLayout = mileus_error
        webview.apply {
            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return request?.url?.host?.contains(baseUrl)?.not() ?: false
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (errorLayout.visibility != View.VISIBLE) {
                        webview.visibility = View.VISIBLE
                    }
                    progressBar.visibility = View.GONE
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (request?.url?.toString() != lastLoadedUrl) {
                        return
                    }
                    webview.visibility = View.INVISIBLE
                    errorLayout.visibility = View.VISIBLE
                }
            }

            addJavascriptInterface(this@MileusActivity, "MileusNative")
        }

        mileus_error_retry.setOnClickListener {
            loadWeb()
        }

        if (savedInstanceState == null) {
            loadWeb()
        } else {
            webview.restoreState(savedInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mileus_webview.saveState(outState)

        outState.let {
            it.origin = origin
            it.destination = destination
        }
    }

    private fun loadWeb() {
        mileus_progress.visibility = View.VISIBLE
        mileus_error.visibility = View.GONE
        lastLoadedUrl = buildUrl()
        mileus_webview.loadUrl(lastLoadedUrl)
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
                startActivityForResult(originSearchIntent, REQUEST_CODE_ORIGIN_SEARCH)
            SEARCH_TYPE_DESTINATION ->
                startActivityForResult(destinationSearchIntent, REQUEST_CODE_DESTINATION_SEARCH)
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

    private fun buildUrl() = Uri.parse(baseUrl)
        .buildUpon()
        .appendQueryParameter("partner_name", partnerName)
        .appendQueryParameter("environment", environment)
        .appendQueryParameter("access_token", token)
        .appendQueryParameter("platform", "android").run {
            origin?.let {
                appendQueryParameter("origin_lat", it.latitude.toString())
                appendQueryParameter("origin_lon", it.longitude.toString())
                appendQueryParameter("origin_address", it.address)
            }
            destination?.let {
                appendQueryParameter("destination_lat", it.latitude.toString())
                appendQueryParameter("destination_lon", it.longitude.toString())
                appendQueryParameter("destination_address", it.address)
            }
            build().toString()
        }

    private fun String.sanitize() = replace("\\", "\\\\").replace("'", "\\'")
}
