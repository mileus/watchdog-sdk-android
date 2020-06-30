package com.mileus.sample

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.mileus.watchdog.MileusWatchdog
import com.mileus.watchdog.data.Location
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val KEY_TOKEN = "KEY_TOKEN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getPreferences(Context.MODE_PRIVATE).apply {
            main_token.setText(getString(KEY_TOKEN, "").orEmpty())
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
        getPreferences(Context.MODE_PRIVATE).edit().apply {
            putString(KEY_TOKEN, main_token.text.toString())
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
            callback(
                main_token.text.toString(),
                originLocation,
                destinationLocation
            )
        } catch (e: NumberFormatException) {
            Toast.makeText(this, R.string.number_format_error, Toast.LENGTH_LONG).show()
        }
    }
}
