package com.mileus.watchdog.data

/**
 * Contains data that customizes notifications. No notification can be displayed by the SDK without
 * a [NotificationInfo] set for that type of notification. Channel ID and icon resource are both
 * required.
 *
 * @param channelId the channel ID used for displaying the notification; must be an ID
 *      of an existing (created) channel
 * @param iconRes a resource ID for a drawable that can be used as the icon for the notification
 */
class NotificationInfo internal constructor(
    val channelId: String,
    val iconRes: Int
) {

    class Builder {
        lateinit var channelId: String

        private lateinit var iconResInternal: IconRes
        var iconRes: Int
            get() = iconResInternal.iconRes
            set(value) {
                iconResInternal = IconRes(value)
            }

        /**
         * Set the channel ID. Required attribute for the builder.
         * @param channelId the channel ID used for displaying the notification; must be an ID
         *      of an existing (created) channel
         */
        fun setChannelId(channelId: String) = apply {
            this.channelId = channelId
        }

        /**
         * Set the icon resource. Required attribute for the builder.
         * @param iconRes a resource ID for a drawable that can be used as the icon
         *      for the notification
         */
        fun setIconRes(iconRes: Int) = apply {
            this.iconRes = iconRes
        }

        /**
         * Build an instance of [NotificationInfo]. Calling this without previously calling
         * [setChannelId] and [setIconRes] will result in a crash.
         */
        fun build() = NotificationInfo(channelId, iconRes)
    }

    private class IconRes(val iconRes: Int)
}
