package com.mileus.watchdog.service

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.google.android.gms.location.*
import com.mileus.sdk.R
import com.mileus.watchdog.MileusWatchdog
import com.mileus.watchdog.await
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.coroutines.resumeWithException

class LocationUpdatesService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 5436
        private const val INTERVAL_MS = 30000L

        private const val URL_DEVELOPMENT = "https://mileus.spacek.now.sh/"
        private const val URL_STAGING = "https://api-stage.mileus.com/"
        private const val URL_PRODUCTION = "https://api.mileus.com/"

        private const val POST_LOCATION_PATH = "watchdog/v1/search/location-tracking"
        private val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType();
        private const val RESPONSE_CODE_PROCEED = 202
    }

    private val supervisorJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + supervisorJob)

    private lateinit var okHttpClient: OkHttpClient
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val baseUrl: String
        get() = when (MileusWatchdog.environment) {
            MileusWatchdog.ENV_PRODUCTION -> URL_PRODUCTION
            MileusWatchdog.ENV_STAGING -> URL_STAGING
            else -> URL_DEVELOPMENT
        }

    override fun onCreate() {
        super.onCreate()

        okHttpClient = OkHttpClient()
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                super.onLocationResult(result)
                result ?: return

                onLocationUpdate(result.lastLocation)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        MileusWatchdog.assertInitialized()

        val notificationInfo = MileusWatchdog.foregroundServiceNotificationInfo
        if (notificationInfo == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = NotificationCompat.Builder(this, notificationInfo.channelId)
            .setContentTitle(getString(R.string.notification_service_location_title))
            .setContentText(getString(R.string.notification_service_location_text))
            .setSmallIcon(notificationInfo.iconRes)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        startLocationRequest()

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()

        scope.cancel()
    }

    private fun startLocationRequest() {
        val request = LocationRequest.create().apply {
            interval = INTERVAL_MS
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        try {
            locationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        } catch (_: SecurityException) {
        }
    }

    private fun onLocationUpdate(location: Location) {
        scope.launch {
            val json = """
                {
                    "location": {
                        "coordinates": {
                            "lat": ${location.latitude},
                            "lon": ${location.longitude}
                        },
                        "accuracy": ${location.accuracy}
                    }
                }
            """.trimIndent()

            val url = baseUrl.toUri()
                .buildUpon()
                .appendEncodedPath(POST_LOCATION_PATH)
                .build()
                .toString()

            val request = Request.Builder()
                .url(url)
                .post(json.toRequestBody(MEDIA_TYPE_JSON))
                .addHeader("Authorization", "Bearer ${MileusWatchdog.accessToken}")
                .build()

            try {
                okHttpClient.newCall(request).await().let {
                    if (it.code != RESPONSE_CODE_PROCEED) {
                        stop()
                    }
                }
            } catch (e: IOException) {
                stop()
            }
        }
    }

    private fun stop() {
        locationClient.removeLocationUpdates(locationCallback)
        stopSelf()
    }
}
