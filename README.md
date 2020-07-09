# Android SDK for Mileus Watchdog

This library supports an asynchronous background search for journey plans through Mileus, with the base use case of commuting home from the user's current location.

Please see the [Watchdog search docs](https://docs.mileus.com/watchdog-search/) for more details about the functionality, or the [SDK docs](https://docs.mileus.com/watchdog-search/frontend-integration/sdk/android/) for more details about the usage of this SDK.

## Add dependency
Add the JitPack repository to your build file:
```
allprojects {
    repositories {
        ...
        maven { url 'https://www.jitpack.io' }
    }
}
```
Add the dependency:
```
dependencies {
    implementation 'com.mileus:watchdog-sdk-android:1.0.0'
}
```

## Add activity to manifest
To make our Activity work please add it to Manifest too:
```
<application …>
    <activity android:name="com.mileus.watchdog.ui.MileusWatchdogActivity" />
</application>
```

## Create an instance
First you have to create an instance of Mileus SDK. Place this piece of code in your `Application.onCreate`, alternatively you can call `MileusWatchdog.init()` on demand in your favorite dependency injection framework:
``` kotlin
val mileus = MileusWatchdog.init(
    partnerName: String, // unique partner identifier
    environment: String // MileusWatchdog.ENV_STAGING or MileusWatchdog.ENV_PRODUCTION
)
```

## Start Mileus SDK Watchdog screen
This is a universal entry point in Mileus SDK Watchdog. Use it to initialise new search, as well as opening Mileus SDK Watchdog from notification.
``` kotlin
MileusWatchdog.startWatchdogActivity(
    context: Context,
    accessToken: String, // token from Watchdog auth API
    origin: Location? = null,
    destination: Location? = null
)
```

You can also use Intent or PendingIntent directly. The method above is just a shortcut for creating intent and starting Activity.
``` kotlin
MileusWatchdog.createWatchdogActivityIntent(
    context: Context,
    accessToken: String,
    origin: Location? = null, 
    destination: Location? = null 
)
```

## Start Mileus SDK Market Validation screen
Special entry point for market validation purposes. In this mode no callbacks are called. You only need to init Mileus SDK and use one of the following methods to open the market validation screen.
``` kotlin
MileusWatchdog.startMarketValidationActivity(
    context: Context,
    accessToken: String, // token from Watchdog auth API
    origin: Location,
    destination: Location
)
```

You can also use Intent or PendingIntent directly. The method above is just a shortcut for creating intent and starting Activity.
``` kotlin
MileusWatchdog.createMarketValidationActivityIntent(
    context: Context,
    accessToken: String,
    origin: Location, 
    destination: Location 
)
```

## Handle callbacks
Callbacks are relevant only for Mileus SDK Watchdog screen usage, not for Mileus SDK Market Validation screen usage.

### Search for origin or destination
When your app has native screen for origin and destination search, configure Intents to open these activities:
``` kotlin
mileus.originSearchActivityIntent = originSearchIntent
mileus.destinationSearchActivityIntent = destinationSearchIntent
```

We’ll call these Intents upon a user's request to change origin or destination with following extras:

- `MileusWatchdog.CURRENT_ORIGIN_EXTRA` - containing `Location`
- `MileusWatchdog.CURRENT_DESTINATION_EXTRA` - containing `Location`
- `MileusWatchdog.SEARCH_TYPE` - containing `String` `"origin"` or `"destination"`

Feel free to add any custom extras, or flags.

In your Activity call to return new origin / destination and finish Activity:

``` kotlin
MileusWatchdog.returnLocationAndFinishActivity(location: Location)
```

### Open taxi ride Activity
We need to open a taxi ride Activity on the last screen in Mileus flow. Set intent we can use to start your native Activity, you can add any extras and flags, there are no special Mileus extras:
``` kotlin
mileus.taxiRideActivityIntent = taxiRideIntent
```

## Location Data class
We use custom Location class to store origin and destination:
``` kotlin
data class Location(
    val address: String, 
    latitude: Double, 
    longitude: Double, 
    accuracy: Float = 0
)
```
