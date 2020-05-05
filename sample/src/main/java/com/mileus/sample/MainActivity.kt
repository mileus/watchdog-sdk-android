package com.mileus.sample

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mileus.sdk.Mileus
import com.mileus.sdk.data.Location
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

        main_open_activity.setOnClickListener {
            getPreferences(Context.MODE_PRIVATE).edit().apply {
                putString(KEY_TOKEN, main_token.text.toString())
                apply()
            }

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

            Mileus.startMileusActivity(
                this,
                main_token.text.toString(),
                originLocation,
                destinationLocation
            )
        }
    }
}
