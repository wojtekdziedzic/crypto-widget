package cloud.dziedzic.cryptowidget.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import cloud.dziedzic.cryptowidget.data.PriceRepository
import cloud.dziedzic.cryptowidget.ui.CryptoWidgetContent

class CryptoPriceWidget : GlanceAppWidget() {

    // Exact sizing so the content can adapt when the user resizes the widget.
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = PriceRepository.get(context)
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val config = repository.configStore.configFor(appWidgetId)
        android.util.Log.d("CryptoWidgetConfig", "provide id=$appWidgetId config=$config")
        // Snapshot read; PriceRefreshWorker calls updateAll() after every fetch,
        // which re-runs provideGlance with fresh data.
        val cached = repository.cache.read(config.coin, config.currency)
        provideContent {
            CryptoWidgetContent(config, cached)
        }
    }
}
