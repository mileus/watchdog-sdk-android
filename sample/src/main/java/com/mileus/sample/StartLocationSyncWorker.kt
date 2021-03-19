package com.mileus.sample

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.mileus.watchdog.MileusWatchdog
import kotlinx.android.synthetic.main.activity_main.*

class StartLocationSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        const val WORK_NAME = "StartLocationSyncWorker"
    }

    override fun doWork(): Result {
        if (!MileusWatchdog.isInitialized) {
            context.getSharedPreferences(MainActivity.PREF_SAMPLE_APP, Context.MODE_PRIVATE)
                .apply {
                    val token = getString(MainActivity.KEY_TOKEN, "") ?: ""
                    val partner = getString(MainActivity.KEY_PARTNER_NAME, "demo") ?: "demo"
                    val env = getString(MainActivity.KEY_ENV, MileusWatchdog.ENV_STAGING)
                        ?: MileusWatchdog.ENV_STAGING
                    MileusWatchdog.init(token, partner, env)
                }
        }

        MileusWatchdog.onSearchStartingSoon(context)
        return Result.success()
    }
}
