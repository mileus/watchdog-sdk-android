package com.mileus.watchdog.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.webkit.JavascriptInterface
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.mileus.watchdog.MileusWatchdog

/**
 * The activity for the Mileus Watchdog scheduling screen. Use
 * [com.mileus.watchdog.MileusWatchdog.startWatchdogSchedulingActivity] to start this activity
 */
class MileusWatchdogSchedulingActivity : MileusActivity() {

    companion object {
        private const val REQUEST_CODE_SETTINGS_BG_LOCATION = 2504
    }

    override val mode = "watchdog_scheduling"

    override val defaultToolbarText: String
        get() = ""

    private lateinit var requestPermission: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        finishIfNotGranted(Manifest.permission.ACCESS_FINE_LOCATION)

        requestPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { onRequestBackgroundLocationResult(it) }

        if (MileusWatchdog.foregroundServiceNotificationInfo == null) {
            val msg =
                "Cannot start activity, foreground service notification info has not been set up."
            Log.e(this::class.simpleName, msg)
            finish()
        }

        super.onCreate(savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SETTINGS_BG_LOCATION) {
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
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
        webview?.evaluateJavascript(
            """
                window.onVerifyBackgroundLocationPermissionResult({ 
                    granted: "$permissionsGranted"
                })
            """.trimIndent(),
            null
        )
    }

    @JavascriptInterface
    fun requestBackgroundLocationPermission() {
        var permissionsGranted = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsGranted = permissionsGranted && ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }

        if (permissionsGranted) {
            webview?.evaluateJavascript(
                "window.onRequestBackgroundLocationPermissionResult({ granted: \"true\" })",
                null
            )
        } else {
            // at this point we can safely assume the OS version is >= Q
            requestPermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
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
            webview?.evaluateJavascript(
                "window.onRequestBackgroundLocationPermissionResult({ granted: \"$result\" })",
                null
            )
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

        webview?.evaluateJavascript(
            """
                window.onRequestBackgroundLocationPermissionResult({ 
                    granted: "$permissionsGranted" 
                })
            """.trimIndent(),
            null
        )
    }
}
