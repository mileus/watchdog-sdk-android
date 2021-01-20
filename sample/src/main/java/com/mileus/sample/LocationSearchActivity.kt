package com.mileus.sample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mileus.watchdog.*
import com.mileus.watchdog.MileusWatchdog.returnLocationAndFinishActivity
import com.mileus.watchdog.data.Location
import kotlinx.android.synthetic.main.activity_location_search.*

class LocationSearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_search)

        val extras = intent.extras ?: throw IllegalStateException("Arguments cannot be null")
        when (extras.searchType) {
            MileusWatchdog.SEARCH_TYPE_ORIGIN -> {
                location_search_address.setText(extras.currentOrigin?.addressLine1)
                location_search_address_2.setText(extras.currentOrigin?.addressLine2)
                location_search_latitude.setText(extras.currentOrigin?.latitude?.toString())
                location_search_longitude.setText(extras.currentOrigin?.longitude?.toString())
            }
            MileusWatchdog.SEARCH_TYPE_DESTINATION -> {
                location_search_address.setText(extras.currentDestination?.addressLine1)
                location_search_address_2.setText(extras.currentDestination?.addressLine2)
                location_search_latitude.setText(extras.currentDestination?.latitude?.toString())
                location_search_longitude.setText(extras.currentDestination?.longitude?.toString())
            }
            MileusWatchdog.SEARCH_TYPE_HOME -> {
                location_search_address.setText(extras.currentHome?.addressLine1)
                location_search_address_2.setText(extras.currentHome?.addressLine2)
                location_search_latitude.setText(extras.currentHome?.latitude?.toString())
                location_search_longitude.setText(extras.currentHome?.longitude?.toString())
            }
        }

        location_search_done.setOnClickListener {
            try {
                returnLocationAndFinishActivity(
                    Location(
                        location_search_address.text.toString(),
                        location_search_address_2.text.toString(),
                        location_search_latitude.text.toString().toDouble(),
                        location_search_longitude.text.toString().toDouble()
                    )
                )
            } catch (e: NumberFormatException) {
                Toast.makeText(this, R.string.number_format_error, Toast.LENGTH_LONG).show()
            }
        }
    }
}
