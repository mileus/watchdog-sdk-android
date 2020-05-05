package com.mileus.sample

import android.app.Application
import android.content.Intent
import com.mileus.sdk.Mileus

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Mileus.init("partnerName", Mileus.ENV_STAGING)

        Intent(this, LocationSearchActivity::class.java).also {
            Mileus.destinationSearchActivityIntent = it
            Mileus.originSearchActivityIntent = it
        }

        Mileus.taxiRideActivityIntent = Intent(this, TaxiRideActivity::class.java)
    }
}
