package com.mileus.watchdog.data

class NotificationInfo internal constructor(
    val channelId: String,
    val iconRes: Int
) {

    class Builder() {
        lateinit var channelId: String

        private lateinit var iconResInternal: IconRes
        var iconRes: Int
            get() = iconResInternal.iconRes
            set(value) {
                iconResInternal = IconRes(value)
            }

        fun setChannelId(channelId: String) = apply {
            this.channelId = channelId
        }

        fun setIconRes(iconRes: Int) = apply {
            this.iconRes = iconRes
        }

        fun build() = NotificationInfo(channelId, iconRes)
    }

    private class IconRes(val iconRes: Int)
}
