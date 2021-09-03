package com.mileus.watchdog.ui

import android.net.Uri
import com.mileus.watchdog.MileusWatchdog
import com.mileus.watchdog.oneTimeSearchStringKeys

class OneTimeSearchActivity : MileusActivity() {

    override val initialScreen = Screen.ONE_TIME_SEARCH

    private lateinit var keyExplanationDialog: String

    override fun fetchIntentExtras() {
        super.fetchIntentExtras()

        intent.extras?.oneTimeSearchStringKeys?.let {
            keyExplanationDialog = it.keyMatchIntroBanner
        } ?: throw MileusWatchdog.InvalidStateException("Missing required string keys.")
    }

    override fun Uri.Builder.modifyUrl() {
        appendQueryParameter("str_key_explanation_dialog", keyExplanationDialog)
    }
}
