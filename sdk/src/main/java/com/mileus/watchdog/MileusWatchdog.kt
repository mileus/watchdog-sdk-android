package com.mileus.watchdog

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.mileus.watchdog.data.Location
import com.mileus.watchdog.data.NotificationInfo
import com.mileus.watchdog.ui.MileusMarketValidationActivity
import com.mileus.watchdog.ui.MileusWatchdogActivity
import com.mileus.watchdog.ui.MileusWatchdogSchedulingActivity

object MileusWatchdog {

    const val ENV_PRODUCTION = "production"
    const val ENV_STAGING = "staging"
    const val ENV_DEVELOPMENT = "development"

    const val CURRENT_ORIGIN_EXTRA = "CURRENT_ORIGIN_EXTRA"
    const val CURRENT_DESTINATION_EXTRA = "CURRENT_DESTINATION_EXTRA"
    const val CURRENT_HOME_EXTRA = "CURRENT_HOME_EXTRA"
    const val SEARCH_TYPE = "SEARCH_TYPE"
    const val SEARCH_TYPE_ORIGIN = "SEARCH_TYPE_ORIGIN"
    const val SEARCH_TYPE_DESTINATION = "SEARCH_TYPE_DESTINATION"
    const val SEARCH_TYPE_HOME = "SEARCH_TYPE_HOME"

    lateinit var accessToken: String
        private set

    lateinit var partnerName: String
        private set

    lateinit var environment: String
        private set

    var isInitialized = false
        private set

    var originSearchActivityIntent: Intent? = null
    var destinationSearchActivityIntent: Intent? = null
    var homeSearchActivityIntent: Intent? = null

    var taxiRideActivityIntent: Intent? = null

    var foregroundServiceNotificationInfo: NotificationInfo? = null

    fun init(accessToken: String, partnerName: String, environment: String = ENV_PRODUCTION) {
        this.accessToken = accessToken
        this.partnerName = partnerName
        if (environment !in arrayOf(
                ENV_PRODUCTION,
                ENV_STAGING,
                ENV_DEVELOPMENT
            )
        ) {
            throw IllegalArgumentException("Invalid environment.")
        }
        this.environment = environment
        isInitialized = true
    }

    fun Activity.returnLocationAndFinishActivity(location: Location) {
        val returnIntent = Intent().updateExtras {
            this.location = location
        }

        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    fun startWatchdogActivity(
        context: Context,
        origin: Location? = null,
        destination: Location? = null
    ) {
        context.startActivity(
            createWatchdogActivityIntent(context, origin, destination)
        )
    }

    fun createWatchdogActivityIntent(
        context: Context,
        origin: Location? = null,
        destination: Location? = null
    ) = Intent(context, MileusWatchdogActivity::class.java).updateExtras {
        assertInitialized()
        token = accessToken
        this.origin = origin
        this.destination = destination
    }

    fun startWatchdogSchedulingActivity(
        context: Context,
        home: Location? = null
    ) {
        context.startActivity(createWatchdogSchedulingActivityIntent(context, home))
    }

    fun createWatchdogSchedulingActivityIntent(
        context: Context,
        home: Location? = null
    ) = Intent(context, MileusWatchdogSchedulingActivity::class.java).updateExtras {
        assertInitialized()
        token = accessToken
        this.home = home
    }

    fun startMarketValidationActivity(
        context: Context,
        origin: Location,
        destination: Location
    ) {
        context.startActivity(
            createMarketValidationActivityIntent(context, origin, destination)
        )
    }

    fun createMarketValidationActivityIntent(
        context: Context,
        origin: Location,
        destination: Location
    ) = Intent(context, MileusMarketValidationActivity::class.java).updateExtras {
        assertInitialized()
        token = accessToken
        this.origin = origin
        this.destination = destination
    }

    fun onSearchStartingSoon() {
        assertInitialized()
    }

    internal fun assertInitialized() {
        if (!isInitialized) {
            throw SdkUninitializedException()
        }
    }

    class SdkUninitializedException : IllegalStateException("Mileus SDK hasn't been initialized.")
}
