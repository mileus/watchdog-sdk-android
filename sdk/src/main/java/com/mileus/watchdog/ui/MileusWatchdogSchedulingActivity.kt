package com.mileus.watchdog.ui

import android.os.Bundle

class MileusWatchdogSchedulingActivity : MileusActivity() {

    override val mode = "watchdog_scheduling"

    override val defaultToolbarText: String
        get() = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        finishIfNotGranted(android.Manifest.permission.ACCESS_FINE_LOCATION)
        super.onCreate(savedInstanceState)
    }

}
