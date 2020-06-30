package com.mileus.sample

import android.app.Application
import android.content.Intent
import com.mileus.watchdog.MileusWatchdog

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        MileusWatchdog.init("partnerName", MileusWatchdog.ENV_DEVELOPMENT)

        Intent(this, LocationSearchActivity::class.java).also {
            MileusWatchdog.destinationSearchActivityIntent = it
            MileusWatchdog.originSearchActivityIntent = it
        }

        MileusWatchdog.taxiRideActivityIntent = Intent(this, TaxiRideActivity::class.java)
    }
}
