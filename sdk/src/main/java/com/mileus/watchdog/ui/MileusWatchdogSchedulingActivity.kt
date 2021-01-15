package com.mileus.watchdog.ui

import android.os.Bundle
import android.util.Log
import com.mileus.watchdog.MileusWatchdog

class MileusWatchdogSchedulingActivity : MileusActivity() {

    override val mode = "watchdog_scheduling"

    override val defaultToolbarText: String
        get() = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        finishIfNotGranted(android.Manifest.permission.ACCESS_FINE_LOCATION)

        if (MileusWatchdog.foregroundServiceNotificationInfo == null) {
            val msg = "Cannot start activity, foreground service notification info has not been set up."
            Log.e(this::class.simpleName, msg)
            finish()
        }

        super.onCreate(savedInstanceState)
    }

}
