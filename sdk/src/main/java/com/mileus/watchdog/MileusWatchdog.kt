package com.mileus.watchdog

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.mileus.watchdog.data.Location
import com.mileus.watchdog.data.NotificationInfo
import com.mileus.watchdog.data.OneTimeSearchStringKeys
import com.mileus.watchdog.service.LocationUpdatesService
import com.mileus.watchdog.ui.MarketValidationActivity
import com.mileus.watchdog.ui.MileusActivity
import com.mileus.watchdog.ui.OneTimeSearchActivity
import com.mileus.watchdog.ui.WatchdogActivity

/**
 * Encapsulates all Mileus SDK-related methods and attributes. Make sure to call [init] prior
 * to calling any methods (but you may set/update properties within this object whenever you want).
 */
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

    /**
     * The Mileus access token in use. Can only be set using [init].
     */
    lateinit var accessToken: String
        private set

    /**
     * The partner name used with the SDK. Can only be set using [init].
     */
    lateinit var partnerName: String
        private set

    /**
     * The current environment in use, such as [ENV_PRODUCTION]. Can only be set using [init].
     */
    lateinit var environment: String
        private set

    /**
     * True if the SDK has been initialized (implying that most methods within the SDK
     * are ready for use).
     */
    var isInitialized = false
        private set

    /**
     * The intent used for requesting an origin location. Needs to be an activity that will call
     * [returnLocationAndFinishActivity] after a location is selected. Use [searchType] to find out
     * which location is being requested if you're reusing the same activity for multiple purposes.
     * Use [currentOrigin] to obtain the old origin location.
     * Always set this value within your application class, as the Mileus activities will not
     * preserve it when the process gets killed.
     */
    var originSearchActivityIntent: Intent? = null

    /**
     * The intent used for requesting a destination location. Needs to be an activity that will call
     * [returnLocationAndFinishActivity] after a location is selected. Use [searchType] to find out
     * which location is being requested if you're reusing the same activity for multiple purposes.
     * Use [currentDestination] to obtain the old destination location.
     * Always set this value within your application class, as the Mileus activities will not
     * preserve it when the process gets killed.
     */
    var destinationSearchActivityIntent: Intent? = null

    /**
     * The intent used for requesting a home location. Needs to be an activity that will call
     * [returnLocationAndFinishActivity] after a location is selected. Use [searchType] to find out
     * which location is being requested if you're reusing the same activity for multiple purposes.
     * Use [currentHome] to obtain the old home location.
     * Always set this value within your application class, as the Mileus activities will not
     * preserve it when the process gets killed.
     */
    var homeSearchActivityIntent: Intent? = null

    /**
     * The intent used for opening the last screen of the Mileus flow. Must be an activity that
     * implements that screen.
     * Always set this value within your application class, as the Mileus activities will not
     * preserve it when the process gets killed.
     */
    var taxiRideActivityIntent: Intent? = null

    /**
     * The notification info used to customize the notifications displayed when a foreground service
     * is running.
     * Always set this value within your application class, as the Mileus activities will not
     * preserve it when the process gets killed.
     */
    var foregroundServiceNotificationInfo: NotificationInfo? = null

    /**
     * Call this method before calling any other method within the SDK (or an exception will
     * be thrown). You can still set/update properties of this object prior to calling this method.
     *
     * @param accessToken a valid Mileus Watchdog token that will be used for communicating
     *      with the Mileus backend.
     * @param partnerName unique partner identifier
     * @param environment the environment used with the Mileus backend, such as [ENV_PRODUCTION]
     */
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

    /**
     * Call this from a location search activity to return the location to the Mileus flow
     * and finish the activity.
     *
     * @param location the [Location] that will be passed back to Mileus
     */
    fun Activity.returnLocationAndFinishActivity(location: Location) {
        val returnIntent = Intent().updateExtras {
            this.location = location
        }

        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    /**
     * Shortcut for starting the activity using [createWatchdogActivityIntent].
     */
    fun startWatchdogActivity(
        context: Context,
        origin: Location? = null,
        destination: Location? = null
    ) {
        context.startActivity(
            createWatchdogActivityIntent(context, origin, destination)
        )
    }

    /**
     * Creates an Intent for the watchdog activity.
     *
     * @param context context used for creating the intent
     * @param origin will be used as the origin location if provided
     * @param destination will be used as the destination location if provided
     */
    fun createWatchdogActivityIntent(
        context: Context,
        origin: Location? = null,
        destination: Location? = null
    ) = Intent(context, WatchdogActivity::class.java).updateExtras {
        assertInitialized()
        screen = MileusActivity.Screen.WATCHDOG
        this.origin = origin
        this.destination = destination
    }

    /**
     * Shortcut for starting the activity using [createWatchdogSchedulingActivityIntent].
     */
    fun startWatchdogSchedulingActivity(
        context: Context,
        home: Location? = null
    ) {
        context.startActivity(createWatchdogSchedulingActivityIntent(context, home))
    }

    /**
     * Creates an Intent for the watchdog scheduling activity.
     *
     * @param context context used for creating the intent
     * @param home will be used as the home location if provided
     */
    fun createWatchdogSchedulingActivityIntent(
        context: Context,
        home: Location? = null
    ) = Intent(context, WatchdogActivity::class.java).updateExtras {
        assertInitialized()
        screen = MileusActivity.Screen.SCHEDULING
        this.home = home
    }

    /**
     * Shortcut for starting the activity using [createMarketValidationActivityIntent].
     */
    fun startMarketValidationActivity(
        context: Context,
        origin: Location,
        destination: Location
    ) {
        context.startActivity(
            createMarketValidationActivityIntent(context, origin, destination)
        )
    }

    /**
     * Creates an Intent for the market validation activity.
     *
     * @param context context used for creating the intent
     * @param origin will be used as the origin location
     * @param destination will be used as the destination location
     */
    fun createMarketValidationActivityIntent(
        context: Context,
        origin: Location,
        destination: Location
    ) = Intent(context, MarketValidationActivity::class.java).updateExtras {
        assertInitialized()
        this.origin = origin
        this.destination = destination
    }

    /**
     * Shortcut for starting the activity using [createOneTimeSearchActivityIntent].
     */
    fun startOneTimeSearchActivity(
        context: Context,
        stringsKeys: OneTimeSearchStringKeys
    ) {
        context.startActivity(createOneTimeSearchActivityIntent(context, stringsKeys))
    }

    /**
     * Creates Intent for the one time search screen.
     *
     * @param context context used for creating the intent
     * @param stringsKeys required strings keys for customizable texts
     */
    fun createOneTimeSearchActivityIntent(
        context: Context,
        stringsKeys: OneTimeSearchStringKeys
    ) = Intent(context, OneTimeSearchActivity::class.java).updateExtras {
        assertInitialized()
        oneTimeSearchStringKeys = stringsKeys
    }

    /**
     * Call this method to initialize location searching for a scheduled watchdog.
     */
    fun onSearchStartingSoon(context: Context) {
        assertInitialized()

        val intent = Intent(context, LocationUpdatesService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    internal fun assertInitialized() {
        if (!isInitialized) {
            throw SdkUninitializedException()
        }
    }

    /**
     * Thrown when trying to perform any operation without initializing the SDK first (using [init]).
     */
    class SdkUninitializedException : IllegalStateException("Mileus SDK hasn't been initialized.")

    class InvalidStateException(message: String) : IllegalStateException(message)
}
