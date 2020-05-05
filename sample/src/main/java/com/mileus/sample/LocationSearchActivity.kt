package com.mileus.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mileus.sdk.Mileus
import com.mileus.sdk.Mileus.returnLocationAndFinishActivity
import com.mileus.sdk.currentDestination
import com.mileus.sdk.currentOrigin
import com.mileus.sdk.data.Location
import com.mileus.sdk.searchType
import kotlinx.android.synthetic.main.activity_location_search.*

class LocationSearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_search)

        val extras = intent.extras ?: throw IllegalStateException("Arguments cannot be null")
        when (extras.searchType) {
            Mileus.SEARCH_TYPE_ORIGIN -> {
                location_search_address.setText(extras.currentOrigin?.address)
                location_search_latitude.setText(extras.currentOrigin?.latitude?.toString())
                location_search_longitude.setText(extras.currentOrigin?.longitude?.toString())
            }
            Mileus.SEARCH_TYPE_DESTINATION -> {
                location_search_address.setText(extras.currentDestination?.address)
                location_search_latitude.setText(extras.currentDestination?.latitude?.toString())
                location_search_longitude.setText(extras.currentDestination?.longitude?.toString())
            }
        }

        location_search_done.setOnClickListener {
            returnLocationAndFinishActivity(
                Location(
                    location_search_address.text.toString(),
                    location_search_latitude.text.toString().toDouble(),
                    location_search_longitude.text.toString().toDouble()
                )
            )
        }
    }
}
