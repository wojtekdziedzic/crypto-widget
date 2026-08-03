package cloud.dziedzic.cryptowidget.work

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cloud.dziedzic.cryptowidget.data.PriceRepository
import cloud.dziedzic.cryptowidget.widget.CryptoPriceWidget

class PriceRefreshWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val refreshResult = PriceRepository.get(applicationContext).refreshAll()
        // Always redraw so widgets reflect whatever the cache holds,
        // including the "last known value" case after a network failure.
        CryptoPriceWidget().updateAll(applicationContext)
        return when {
            refreshResult.isSuccess -> Result.success()
            runAttemptCount < MAX_RETRIES -> Result.retry()
            else -> Result.failure()
        }
    }

    companion object {
        private const val MAX_RETRIES = 3
    }
}
