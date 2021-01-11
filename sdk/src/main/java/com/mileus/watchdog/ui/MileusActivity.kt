package com.mileus.watchdog.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.mileus.sdk.R
import com.mileus.watchdog.*
import com.mileus.watchdog.data.Location
import kotlinx.android.synthetic.main.activity_mileus_watchdog.*

abstract class MileusActivity : AppCompatActivity() {

    companion object {
        const val URL_DEVELOPMENT = "https://mileus.spacek.now.sh/"
        const val URL_STAGING = "https://watchdog-web-stage.mileus.com/"
        const val URL_PRODUCTION = "https://watchdog-web.mileus.com/"
    }

    protected var origin: Location? = null
    protected var destination: Location? = null
    protected var home: Location? = null
    protected lateinit var token: String
    protected var partnerName = MileusWatchdog.partnerName
    protected var environment = MileusWatchdog.environment

    protected val baseUrl: String
        get() = when (environment) {
            MileusWatchdog.ENV_PRODUCTION -> URL_PRODUCTION
            MileusWatchdog.ENV_STAGING -> URL_STAGING
            else -> URL_DEVELOPMENT
        }

    private val originSearchIntent: Intent?
        get() = MileusWatchdog.originSearchActivityIntent?.forSearchActivity(
            MileusWatchdog.SEARCH_TYPE_ORIGIN
        )

    private val destinationSearchIntent: Intent?
        get() = MileusWatchdog.destinationSearchActivityIntent?.forSearchActivity(
            MileusWatchdog.SEARCH_TYPE_DESTINATION
        )

    private val homeSearchIntent: Intent?
        get() = MileusWatchdog.homeSearchActivityIntent?.forSearchActivity(
            MileusWatchdog.SEARCH_TYPE_HOME
        )

    protected abstract val mode: String

    protected val language: String
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                Resources.getSystem().configuration.locales[0].language
            else
                Resources.getSystem().configuration.locale.language
        }

    protected var webview: WebView? = null

    protected abstract val toolbarText: String

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            webview?.post { webview?.setNetworkAvailable(true) }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            webview?.post { webview?.setNetworkAvailable(false) }
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.MileusTheme)
        setContentView(R.layout.activity_mileus_watchdog)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        mileus_toolbar.setNavigationOnClickListener { finish() }
        mileus_toolbar.title = toolbarText

        origin = intent.extras?.origin
        destination = intent.extras?.destination
        home = intent.extras?.home
        token = intent.extras?.token ?: throw IllegalStateException("Missing access token.")
        partnerName = MileusWatchdog.partnerName

        savedInstanceState?.let {
            it.origin?.let { origin = it }
            it.destination?.let { destination = it }
            it.home?.let { home = it }
        }

        val progressBar = mileus_progress
        webview = mileus_webview
        val errorLayout = mileus_error
        webview?.apply {
            addJavascriptInterface(this@MileusActivity, "MileusNative")
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
                        webview?.visibility = View.VISIBLE
                    }
                    progressBar.visibility = View.GONE
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame != true) {
                        return
                    }
                    webview?.visibility = View.INVISIBLE
                    errorLayout.visibility = View.VISIBLE
                }
            }
        }

        mileus_error_retry.setOnClickListener {
            loadWeb()
        }

        if (savedInstanceState == null) {
            loadWeb()
        } else {
            webview?.restoreState(savedInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webview?.saveState(outState)

        outState.let {
            it.origin = origin
            it.destination = destination
            it.home = home
        }
    }

    override fun onResume() {
        super.onResume()
        (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
            .registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback)
    }

    override fun onPause() {
        super.onPause()
        (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
            .unregisterNetworkCallback(networkCallback)
    }

    override fun onDestroy() {
        webview = null
        super.onDestroy()
    }

    override fun onBackPressed() {
        webview?.evaluateJavascript("window.handleBackEvent()") {
            if (it != "true") {
                finish()
            }
        }
    }

    private fun loadWeb() {
        mileus_progress.visibility = View.VISIBLE
        mileus_error.visibility = View.GONE
        webview?.loadUrl(buildUrl())
    }

    @JavascriptInterface
    fun openSearchScreen(searchType: String) {
        when (searchType) {
            MileusWatchdogActivity.SEARCH_TYPE_ORIGIN ->
                startActivityForResult(
                    originSearchIntent,
                    MileusWatchdogActivity.REQUEST_CODE_ORIGIN_SEARCH
                )
            MileusWatchdogActivity.SEARCH_TYPE_DESTINATION ->
                startActivityForResult(
                    destinationSearchIntent,
                    MileusWatchdogActivity.REQUEST_CODE_DESTINATION_SEARCH
                )
            MileusWatchdogActivity.SEARCH_TYPE_HOME ->
                startActivityForResult(
                    homeSearchIntent,
                    MileusWatchdogActivity.REQUEST_CODE_HOME_SEARCH
                )
        }
    }

    protected fun updateLocationsInJs() {
        origin?.let {
            webview?.evaluateJavascript(
                """
                    window.setOrigin({
                        lat: ${it.latitude},
                        lon: ${it.longitude},
                        address_line_1: '${it.addressLine1.sanitize()}',
                        address_line_2: '${it.addressLine2?.sanitize() ?: ""}',
                        accuracy: ${it.accuracy}
                    });
                """.trimIndent(),
                null
            )
        }
        destination?.let {
            webview?.evaluateJavascript(
                """
                    window.setDestination({
                        lat: ${it.latitude},
                        lon: ${it.longitude},
                        address_line_1: '${it.addressLine1.sanitize()}',
                        address_line_2: '${it.addressLine2?.sanitize() ?: ""}',
                        accuracy: ${it.accuracy}
                    });
                """.trimIndent(),
                null
            )
        }
        home?.let {
            webview?.evaluateJavascript(
                """
                    window.setHome({
                        lat: ${it.latitude},
                        lon: ${it.longitude},
                        address_line_1: '${it.addressLine1.sanitize()}',
                        address_line_2: '${it.addressLine2?.sanitize() ?: ""}',
                        accuracy: ${it.accuracy}
                    });
                """.trimIndent(),
            null)
        }
    }

    private fun buildUrl() = Uri.parse(baseUrl)
        .buildUpon()
        .appendQueryParameter("partner_name", partnerName)
        .appendQueryParameter("environment", environment)
        .appendQueryParameter("access_token", token)
        .appendQueryParameter("language", language)
        .appendQueryParameter("platform", "android")
        .appendQueryParameter("screen", mode).run {
            origin?.let {
                appendQueryParameter("origin_lat", it.latitude.toString())
                appendQueryParameter("origin_lon", it.longitude.toString())
                appendQueryParameter("origin_address_line_1", it.addressLine1)
                it.addressLine2?.let { line2 ->
                    appendQueryParameter("origin_address_line_2", line2)
                }
            }
            destination?.let {
                appendQueryParameter("destination_lat", it.latitude.toString())
                appendQueryParameter("destination_lon", it.longitude.toString())
                appendQueryParameter("destination_address_line_1", it.addressLine1)
                it.addressLine2?.let { line2 ->
                    appendQueryParameter("destination_address_line_2", line2)
                }
            }
            home?.let {
                appendQueryParameter("home_lat", it.latitude.toString())
                appendQueryParameter("home_lon", it.longitude.toString())
                appendQueryParameter("home_address_line_1", it.addressLine1)
                it.addressLine2?.let { line2 ->
                    appendQueryParameter("home_address_line_2", line2)
                }
            }
            build().toString()
        }

    private fun Intent.forSearchActivity(searchType: String) = updateExtras {
        currentOrigin = this@MileusActivity.origin
        currentDestination = this@MileusActivity.destination
        currentHome = this@MileusActivity.home
        this.searchType = searchType
    }

    protected fun String.sanitize() = replace("\\", "\\\\").replace("'", "\\'")
}
