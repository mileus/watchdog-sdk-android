package com.mileus.sample

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.mileus.watchdog.MileusWatchdog
import com.mileus.watchdog.data.NotificationInfo

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Intent(this, LocationSearchActivity::class.java).also {
            MileusWatchdog.destinationSearchActivityIntent = it
            MileusWatchdog.originSearchActivityIntent = it
            MileusWatchdog.homeSearchActivityIntent = it
        }

        MileusWatchdog.taxiRideActivityIntent = Intent(this, TaxiRideActivity::class.java)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "mileus-sample-location-channel"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        MileusWatchdog.foregroundServiceNotificationInfo = NotificationInfo.Builder()
            .setChannelId(channelId)
            .setIconRes(R.drawable.ic_location_search)
            .build()
    }
}
