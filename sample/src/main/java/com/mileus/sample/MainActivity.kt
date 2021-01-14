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
            handleButtonClick { origin, destination, _ ->
                MileusWatchdog.startWatchdogActivity(this, origin, destination)
            }
        }

        main_open_watchdog_scheduling_activity.setOnClickListener {
            handleButtonClick { _, _, home ->
                MileusWatchdog.startWatchdogSchedulingActivity(this, home)
            }
        }

        main_open_market_validation_activity.setOnClickListener {
            handleButtonClick { origin, destination, _ ->
                MileusWatchdog.startMarketValidationActivity(this, origin, destination)
            }
        }
    }

    private fun handleButtonClick(
        callback: (origin: Location, destination: Location, home: Location) -> Unit
    ) {

        val token = main_token.text.toString()
        val partnerName = main_partner_name.text.toString()
        getPreferences(Context.MODE_PRIVATE).edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_PARTNER_NAME, partnerName)
            apply()
        }

        fun String.toCoordinate() = replace(',', '.').toDouble()

        try {
            val originLocation = Location(
                main_origin_address.text.toString(),
                main_origin_address_2.text.toString(),
                main_origin_latitude.text.toString().toCoordinate(),
                main_origin_longitude.text.toString().toCoordinate()
            )

            val destinationLocation = Location(
                main_destination_address.text.toString(),
                main_destination_address_2.text.toString(),
                main_destination_latitude.text.toString().toCoordinate(),
                main_destination_longitude.text.toString().toCoordinate()
            )

            val homeLocation = Location(
                main_home_address.text.toString(),
                main_home_address_2.text.toString(),
                main_home_latitude.text.toString().toCoordinate(),
                main_home_longitude.text.toString().toCoordinate()
            )

            MileusWatchdog.init(token, partnerName, main_env.selectedItem.toString())

            callback(
                originLocation,
                destinationLocation,
                homeLocation
            )
        } catch (e: NumberFormatException) {
            Toast.makeText(this, R.string.number_format_error, Toast.LENGTH_LONG).show()
        }
    }
}
