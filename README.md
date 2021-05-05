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
    implementation 'com.mileus:watchdog-sdk-android:$watchdog_version'
}
```

## Prerequisites
Before launching any of the Watchdog activities (except for the Market Validation activity), the `ACCESS_FINE_LOCATION` permission must be granted. Note that it is a runtime permission, thus must be asked explicitly. If you attempt to run any of the activities without the permission, the activity will crash.
You also need to set up foreground service notifications (see the section “Set up foreground service notifications”) before opening any (non-Market Validation) Watchdog activity.
Lastly, you need to specify search screen intents that open Activities allowing the user to change the locations for home, origin and destination (again, this excludes Market Validation; see “Handle callbacks”).

## Create an instance
First you have to initialise the Mileus SDK. Call this method after you obtain the access token, before you call any of the other methods within the Mileus SDK:
``` kotlin
MileusWatchdog.init(
    accessToken: String, // token from Watchdog auth API
    partnerName: String, // unique partner identifier
    environment: String // MileusWatchdog.ENV_DEVELOPMENT or MileusWatchdog.ENV_STAGING or MileusWatchdog.ENV_PRODUCTION
)
```

## Check whether the SDK has been initialised
You can use this property to check whether the SDK has been initialised, might come handy before you call any of the SDK methods:
``` kotlin
MileusWatchdog.isInitialized
```

## Start Mileus SDK Watchdog screen
This is a universal entry point in Mileus SDK Watchdog. Use it to initialise new search, as well as opening Mileus SDK Watchdog from notification.
``` kotlin
MileusWatchdog.startWatchdogActivity(
    context: Context,
    origin: Location? = null,
    destination: Location? = null
)
```

You can also use Intent or PendingIntent directly. The method above is just a shortcut for creating an intent and starting the Activity.
``` kotlin
MileusWatchdog.createWatchdogActivityIntent(
    context: Context,
    origin: Location? = null,
    destination: Location? = null
)
```

## Start Mileus SDK Watchdog Scheduling screen
This is an entry point for opening the screen for scheduling watchdogs. Use it to open Watchdog Scheduling from your user interface, as well as for opening Watchdog Scheduling from notification. You can optionally pass the home location which will be used as the destination, if your app allows choosing one; otherwise the activity itself will allow the user to choose it. It will only be used as the default value, the user can still pick a different one within the activity.
``` kotlin
MileusWatchdog.startWatchdogSchedulingActivity(
    context: Context,
    homeLocation: Location? = null
)
```

You can also use Intent or PendingIntent directly. The method above is just a shortcut for creating an intent and starting the Activity.
``` kotlin
MileusWatchdog.createWatchdogSchedulingActivityIntent(
    context: Context,
    homeLocation: Location? = null
)
```

## Set up foreground service notifications
In order to use the scheduled watchdog feature, you need to specify the notification modifier for the notification that will be displayed when a foreground service is running (this foreground service is going to be used for scanning the user’s location). Please set the following value before starting any (non-Market Validation) activity (otherwise it will crash). Note that it has to be set before the foreground service is started, so we strongly recommend doing it in your Application implementation (you do not have to initialise the SDK first):
``` kotlin
MileusWatchdog.foregroundServiceNotificationInfo = NotificationInfo.Builder()
    .setChannel(channelId)
    .setIconRes(resId)
    .build()
```

## Start Mileus SDK Market Validation screen
A special entry point for market validation purposes. In this mode no callbacks are called. You only need to initialise the Mileus SDK and use one of the following methods to open the market validation screen.
``` kotlin
MileusWatchdog.startMarketValidationActivity(
    context: Context,
    origin: Location,
    destination: Location
)
```

You can also use Intent or PendingIntent directly. The method above is just a shortcut for creating an intent and starting the Activity.
``` kotlin
MileusWatchdog.createMarketValidationActivityIntent(
    context: Context,
    origin: Location, 
    destination: Location 
)
```

## Style the Toolbar in Mileus screens
For all the Mileus screens, you can adjust the colour of the toolbar, the colour of text and icons in the toolbar, and the font. You can skip these steps if you’re happy with the default styling.
In your app’s styles XML, create a theme called `MileusTheme`. The theme should inherit from `MileusBaseTheme`:
``` xml
<style name="MileusTheme" parent="MileusBaseTheme">
   <item name="mileusToolbarSurface">#0000ff</item>
   <item name="mileusOnSurface">#ffffff</item>
   <item name="mileusLightStatusBar">false</item>
   <item name="mileusTitleFont">monospace</item>
</style>
```
The `mileusOnSurface` attribute applies to both the text and the icons.
`mileusToolbarSurface` will be applied both as the Toolbar background and as the status bar colour.
The attribute `mileusLightStatusBar` is just a shortcut for `android:windowLightStatusBar`, so that you don’t need to create multiple versions of the theme if you support a version of Android that doesn’t support this attribute (note that using the platform version will have no effect).
Finally, use `mileusTitleFont` to change the font of the Toolbar text.

## Handle callbacks
### Search for origin / destination / home
Before opening any Mileus screens except Market Validation, configure Intents to open your location search activities. These should always be set in your Application class, since they don’t survive if your app gets killed:
``` kotlin
MileusWatchdog.originSearchActivityIntent = originSearchIntent
MileusWatchdog.destinationSearchActivityIntent = destinationSearchIntent
MileusWatchdog.homeSearchActivityIntent = homeSearchIntent
```

We’ll call these Intents upon a user's request to change origin or destination with the following extras:

- `MileusWatchdog.CURRENT_ORIGIN_EXTRA` - containing `Location`
- `MileusWatchdog.CURRENT_DESTINATION_EXTRA` - containing `Location`
- `MileusWatchdog.CURRENT_HOME_EXTRA` - containing Location
- `MileusWatchdog.SEARCH_TYPE` - containing one of these string constants: `MileusWatchdog.SEARCH_TYPE_ORIGIN`, `MileusWatchdog.SEARCH_TYPE_DESTINATION` or `MileusWatchdog.SEARCH_TYPE_HOME`

Feel free to add any custom extras, or flags.

In your Activity, call to return new origin / destination / home and finish Activity:

``` kotlin
MileusWatchdog.returnLocationAndFinishActivity(location: Location)
```

### Open taxi ride Activity
We need to open a taxi ride Activity on the last screen of the Mileus flow. Set an intent we can use to start your native Activity, you can add any extras and flags, there are no special Mileus extras. You should always set this in your Application class, since Intents don’t survive if your app gets killed.
``` kotlin
MileusWatchdog.taxiRideActivityIntent = taxiRideIntent
```

## Initiate background location sync
We use a foreground service to initiate the synchronisation of the user's location when the scheduled watchdog is about to start. Call this method to start the service. Make sure that you have set the notification modifier for foreground services before calling it, otherwise your app will crash:
``` kotlin
MileusWatchdog.onSearchStartingSoon(context: Context)
```

## Location Data class
We use a custom `Location` class to store origin, destination and home:
``` kotlin
data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: Address?,
    val accuracy: Float = 0
)

data class Address(
    val firstLine: String, // for example, "My Street 5"
    val secondLine: String? // for example, "Dublin - 350 02"
)
```
