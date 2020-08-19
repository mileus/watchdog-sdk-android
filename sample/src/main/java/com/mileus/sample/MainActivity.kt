package com.mileus.sample

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import com.mileus.watchdog.MileusWatchdog
import com.mileus.watchdog.data.Location
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val KEY_TOKEN = "KEY_TOKEN"
        private const val KEY_PARTNER_NAME = "KEY_PARTNER_NAME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getPreferences(Context.MODE_PRIVATE).apply {
            main_token.setText(getString(KEY_TOKEN, ""))
            main_partner_name.setText(getString(KEY_PARTNER_NAME, "demo"))
        }

        main_env.apply {
            adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                listOf(
                    MileusWatchdog.ENV_DEVELOPMENT,
                    MileusWatchdog.ENV_STAGING,
                    MileusWatchdog.ENV_PRODUCTION
                )
            )
            setSelection(1)
        }

        main_open_watchdog_activity.setOnClickListener {
            handleButtonClick { token, origin, destination ->
                MileusWatchdog.startWatchdogActivity(this, token, origin, destination)
            }
        }

        main_open_market_validation_activity.setOnClickListener {
            handleButtonClick { token, origin, destination ->
                MileusWatchdog.startMarketValidationActivity(this, token, origin, destination)
            }
        }
    }

    private fun handleButtonClick(
        callback: (token: String, origin: Location, destination: Location) -> Unit
    ) {

        val token = main_token.text.toString()
        val partnerName = main_partner_name.text.toString()
        getPreferences(Context.MODE_PRIVATE).edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_PARTNER_NAME, partnerName)
            apply()
        }

        try {
            val originLocation = Location(
                main_origin_address.text.toString(),
                main_origin_latitude.text.toString().replace(',', '.').toDouble(),
                main_origin_longitude.text.toString().replace(',', '.').toDouble()
            )

            val destinationLocation = Location(
                main_destination_address.text.toString(),
                main_destination_latitude.text.toString().replace(',', '.').toDouble(),
                main_destination_longitude.text.toString().replace(',', '.').toDouble()
            )

            // this would typically be only called in your Application implementation,
            // we are calling it here because we want to test different "partner names" in this sample app
            MileusWatchdog.init(partnerName, main_env.selectedItem.toString())

            callback(
                token,
                originLocation,
                destinationLocation
            )
        } catch (e: NumberFormatException) {
            Toast.makeText(this, R.string.number_format_error, Toast.LENGTH_LONG).show()
        }
    }
}
