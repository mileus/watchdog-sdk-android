package com.mileus.sample

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.mileus.watchdog.MileusWatchdog
import com.mileus.watchdog.data.Address
import com.mileus.watchdog.data.Location
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        const val KEY_TOKEN = "KEY_TOKEN"
        const val KEY_PARTNER_NAME = "KEY_PARTNER_NAME"
        const val KEY_ENV = "KEY_ENV"

        const val PREF_SAMPLE_APP = "mileussampleapp"
    }

    private lateinit var requestPermission: ActivityResultLauncher<String>

    private var afterGranted: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                afterGranted?.invoke()
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
            }
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
        }

        getSharedPreferences(PREF_SAMPLE_APP, Context.MODE_PRIVATE).apply {
            main_token.setText(getString(KEY_TOKEN, ""))
            main_partner_name.setText(getString(KEY_PARTNER_NAME, "demo"))
            main_env.setSelection(
                (main_env.adapter as ArrayAdapter<String>).getPosition(
                    getString(
                        KEY_ENV, MileusWatchdog.ENV_STAGING
                    )
                )
            )
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
            handleButtonClick(false) { origin, destination, _ ->
                if (origin == null || destination == null) {
                    Toast.makeText(this, R.string.number_format_error, Toast.LENGTH_LONG).show()
                } else {
                    MileusWatchdog.startMarketValidationActivity(this, origin, destination)
                }
            }
        }

        main_sync_location.setOnClickListener { onScheduleLocationSyncClick() }
    }

    private fun handleButtonClick(
        requireFineLocation: Boolean = true,
        callback: (origin: Location?, destination: Location?, home: Location?) -> Unit
    ) {

        val permissionFine = !requireFineLocation || ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionFine) {
            afterGranted = {
                handleButtonClick(requireFineLocation, callback)
            }
            requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        fun String.toCoordinate() = replace(',', '.').toDouble()

        val originLocation = runCatching {
            Location(
                main_origin_latitude.text.toString().toCoordinate(),
                main_origin_longitude.text.toString().toCoordinate(),
                Address(
                    main_origin_address.text.toString(),
                    main_origin_address_2.text.toString()
                )
            )
        }.getOrNull()

        val destinationLocation = runCatching {
            Location(
                main_destination_latitude.text.toString().toCoordinate(),
                main_destination_longitude.text.toString().toCoordinate(),
                Address(
                    main_destination_address.text.toString(),
                    main_destination_address_2.text.toString()
                )
            )
        }.getOrNull()

        val homeLocation = runCatching {
            Location(
                main_home_latitude.text.toString().toCoordinate(),
                main_home_longitude.text.toString().toCoordinate(),
                Address(
                    main_home_address.text.toString(),
                    main_home_address_2.text.toString()
                )
            )
        }.getOrNull()

        initSdkFromInputs()

        callback(
            originLocation,
            destinationLocation,
            homeLocation
        )
    }

    private fun initSdkFromInputs() {
        val token = main_token.text.toString()
        val partnerName = main_partner_name.text.toString()
        val env = main_env.selectedItem.toString()
        getSharedPreferences(PREF_SAMPLE_APP, Context.MODE_PRIVATE).edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_PARTNER_NAME, partnerName)
            putString(KEY_ENV, env)
            apply()
        }

        MileusWatchdog.init(token, partnerName, env)
    }

    private fun onScheduleLocationSyncClick() {
        initSdkFromInputs()

        val request = OneTimeWorkRequestBuilder<StartLocationSyncWorker>()
            .setInitialDelay(20, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            StartLocationSyncWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )

        Toast.makeText(this, R.string.location_sync_scheduled, Toast.LENGTH_LONG).show()
    }
}
