package com.mileus.watchdog.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.webkit.JavascriptInterface
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.mileus.watchdog.MileusWatchdog
import com.mileus.watchdog.screen

class WatchdogActivity : MileusActivity() {

    companion object {
        private const val REQUEST_CODE_SETTINGS_BG_LOCATION = 2504
    }

    override val screenOriginal: Screen
        get() = intent.extras?.screen ?: throw IllegalStateException(
            "Missing the screen argument. Did you start the activity manually?"
        )

    private lateinit var requestPermission: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { onRequestBackgroundLocationResult(it) }

        throwIfNotGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        if (MileusWatchdog.foregroundServiceNotificationInfo == null) {
            val msg =
                "Cannot start activity, foreground service notification info has not been set up."
            throw IllegalStateException(msg)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SETTINGS_BG_LOCATION) { // not expecting an OK result
            onRequestBackgroundLocationSettingsResult()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    @JavascriptInterface
    fun verifyBackgroundLocationPermission() {
        var permissionsGranted = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsGranted = permissionsGranted && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
        runOnUiThread {
            webview?.evaluateJavascript(
                """
                window.onVerifyBackgroundLocationPermissionResult({ 
                    granted: $permissionsGranted
                })
            """.trimIndent(),
                null
            )
        }
    }

    private fun onRequestBackgroundLocationResult(result: Boolean) {
        var shouldShowRationale = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            shouldShowRationale = shouldShowRationale &&
                    shouldShowRequestPermissionRationale(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
        }
        if (!result && !shouldShowRationale) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivityForResult(intent, REQUEST_CODE_SETTINGS_BG_LOCATION)
        } else {
            notifyRequestBackgroundLocationResult(result)
        }
    }

    private fun onRequestBackgroundLocationSettingsResult() {
        var permissionsGranted = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsGranted = permissionsGranted && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }

        notifyRequestBackgroundLocationResult(permissionsGranted)
    }

    @JavascriptInterface
    fun requestBackgroundLocationPermission() {
        var permissionsGranted = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsGranted = permissionsGranted && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }

        if (permissionsGranted) {
            runOnUiThread {
                notifyRequestBackgroundLocationResult(true)
            }
        } else {
            // at this point we can safely assume the OS version is >= Q
            requestPermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    @JavascriptInterface
    fun startLocationScanning() {
        MileusWatchdog.onSearchStartingSoon(this)
    }

    private fun notifyRequestBackgroundLocationResult(result: Boolean) {
        webview?.evaluateJavascript(
            "window.onRequestBackgroundLocationPermissionResult({ granted: $result })",
            null
        )
    }
}