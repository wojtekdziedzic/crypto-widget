package com.xrpwidget.work

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.xrpwidget.data.PriceRepository
import com.xrpwidget.widget.XrpPriceWidget

class PriceRefreshWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val refreshResult = PriceRepository.get(applicationContext).refresh()
        // Always redraw so the widget reflects whatever the cache holds,
        // including the "last known value" case after a network failure.
        XrpPriceWidget().updateAll(applicationContext)
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
