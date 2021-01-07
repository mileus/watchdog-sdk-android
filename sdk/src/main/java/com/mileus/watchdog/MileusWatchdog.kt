package com.mileus.watchdog

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.mileus.watchdog.data.Location
import com.mileus.watchdog.ui.MileusMarketValidationActivity
import com.mileus.watchdog.ui.MileusWatchdogActivity
import com.mileus.watchdog.ui.MileusWatchdogSchedulingActivity

object MileusWatchdog {

    const val ENV_PRODUCTION = "production"
    const val ENV_STAGING = "staging"
    const val ENV_DEVELOPMENT = "development"

    const val CURRENT_ORIGIN_EXTRA = "CURRENT_ORIGIN_EXTRA"
    const val CURRENT_DESTINATION_EXTRA = "CURRENT_DESTINATION_EXTRA"
    const val SEARCH_TYPE = "SEARCH_TYPE"
    const val SEARCH_TYPE_ORIGIN = "SEARCH_TYPE_ORIGIN"
    const val SEARCH_TYPE_DESTINATION = "SEARCH_TYPE_DESTINATION"

    lateinit var partnerName: String
        private set

    lateinit var environment: String
        private set

    var originSearchActivityIntent: Intent? = null
    var destinationSearchActivityIntent: Intent? = null

    var taxiRideActivityIntent: Intent? = null

    fun init(partnerName: String, environment: String = ENV_PRODUCTION) {
        MileusWatchdog.partnerName = partnerName
        if (environment !in arrayOf(
                ENV_PRODUCTION,
                ENV_STAGING,
                ENV_DEVELOPMENT
            )
        ) {
            throw IllegalArgumentException("Invalid environment.")
        }
        MileusWatchdog.environment = environment
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
        accessToken: String,
        origin: Location? = null,
        destination: Location? = null
    ) {
        context.startActivity(
            createWatchdogActivityIntent(context, accessToken, origin, destination)
        )
    }

    fun createWatchdogActivityIntent(
        context: Context,
        accessToken: String,
        origin: Location? = null,
        destination: Location? = null
    ) = Intent(context, MileusWatchdogActivity::class.java).updateExtras {
        token = accessToken
        this.origin = origin
        this.destination = destination
    }

    fun startWatchdogSchedulingActivity(
        context: Context,
        accessToken: String,
        home: Location? = null
    ) {
        context.startActivity(createWatchdogSchedulingActivityIntent(context, accessToken, home))
    }

    fun createWatchdogSchedulingActivityIntent(
        context: Context,
        accessToken: String,
        home: Location? = null
    ) = Intent(context, MileusWatchdogSchedulingActivity::class.java).updateExtras {
        token = accessToken
        this.home = home
    }

    fun startMarketValidationActivity(
        context: Context,
        accessToken: String,
        origin: Location? = null,
        destination: Location? = null
    ) {
        context.startActivity(
            createMarketValidationActivityIntent(context, accessToken, origin, destination)
        )
    }

    fun createMarketValidationActivityIntent(
        context: Context,
        accessToken: String,
        origin: Location? = null,
        destination: Location? = null
    ) = Intent(context, MileusMarketValidationActivity::class.java).updateExtras {
        token = accessToken
        this.origin = origin
        this.destination = destination
    }
}
