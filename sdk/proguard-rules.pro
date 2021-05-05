-keep class com.mileus.watchdog.MileusWatchdog {
    public *;
}

-keep class com.mileus.watchdog.data.Location {
    public *;
}

-keep class com.mileus.watchdog.data.Address {
    public *;
}

-keep class com.mileus.watchdog.data.NotificationInfo {
    public *;
}

-keep class com.mileus.watchdog.ui.MileusActivity {
    public *;
    protected *;
}

-keep class com.mileus.watchdog.ui.WatchdogActivity {
    public *;
    protected *;
}

-keep class com.mileus.watchdog.ui.MarketValidationActivity {
    public *;
    protected *;
}

-keep class com.mileus.watchdog.UtilsKt {
    public *;
}
