package com.mileus.watchdog.ui

import android.annotation.SuppressLint
import android.content.Context
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
    protected lateinit var token: String
    protected var partnerName = MileusWatchdog.partnerName
    protected var environment = MileusWatchdog.environment

    protected val baseUrl: String
        get() = when (environment) {
            MileusWatchdog.ENV_PRODUCTION -> URL_PRODUCTION
            MileusWatchdog.ENV_STAGING -> URL_STAGING
            else -> URL_DEVELOPMENT
        }

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
        token = intent.extras?.token ?: throw IllegalStateException("Missing access token.")
        partnerName = MileusWatchdog.partnerName

        savedInstanceState?.let {
            it.origin?.let { origin = it }
            it.destination?.let { destination = it }
        }

        val progressBar = mileus_progress
        webview = mileus_webview
        val errorLayout = mileus_error
        webview?.apply {
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
        mileus_webview.saveState(outState)

        outState.let {
            it.origin = origin
            it.destination = destination
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

    private fun loadWeb() {
        mileus_progress.visibility = View.VISIBLE
        mileus_error.visibility = View.GONE
        mileus_webview.loadUrl(buildUrl())
    }

    private fun buildUrl() = Uri.parse(baseUrl)
        .buildUpon()
        .appendQueryParameter("partner_name", partnerName)
        .appendQueryParameter("environment", environment)
        .appendQueryParameter("access_token", token)
        .appendQueryParameter("language", language)
        .appendQueryParameter("platform", "android")
        .appendQueryParameter("mode", mode).run {
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

    protected fun String.sanitize() = replace("\\", "\\\\").replace("'", "\\'")
}