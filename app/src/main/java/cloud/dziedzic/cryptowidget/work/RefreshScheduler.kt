package cloud.dziedzic.cryptowidget.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object RefreshScheduler {

    private const val PERIODIC_WORK_NAME = "crypto_price_periodic_refresh"
    private const val MANUAL_WORK_NAME = "crypto_price_manual_refresh"
    private const val REFRESH_INTERVAL_MINUTES = 30L

    private val networkConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /** Called from the widget receiver's onEnabled; KEEP preserves the running schedule. */
    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<PriceRefreshWorker>(
            REFRESH_INTERVAL_MINUTES, TimeUnit.MINUTES,
        )
            .setConstraints(networkConstraint)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    /** Called from the widget receiver's onDisabled (last widget removed). */
    fun cancelPeriodic(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
    }

    /** Manual refresh; REPLACE collapses rapid repeated taps into one request. */
    fun refreshNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<PriceRefreshWorker>()
            .setConstraints(networkConstraint)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(MANUAL_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }
}
