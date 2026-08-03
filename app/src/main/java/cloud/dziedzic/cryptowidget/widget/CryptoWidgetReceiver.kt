package cloud.dziedzic.cryptowidget.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import cloud.dziedzic.cryptowidget.data.WidgetConfigStore
import cloud.dziedzic.cryptowidget.work.RefreshScheduler
import kotlinx.coroutines.runBlocking

class CryptoWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = CryptoPriceWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        RefreshScheduler.schedulePeriodic(context)
        // Fill the widget right away instead of waiting up to 30 minutes.
        RefreshScheduler.refreshNow(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        runBlocking { WidgetConfigStore(context).remove(appWidgetIds) }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        RefreshScheduler.cancelPeriodic(context)
    }
}
