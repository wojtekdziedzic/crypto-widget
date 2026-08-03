package com.xrpwidget.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import com.xrpwidget.data.PriceRepository
import com.xrpwidget.ui.XrpWidgetContent

class XrpPriceWidget : GlanceAppWidget() {

    // Exact sizing so the content can adapt when the user resizes the widget.
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Read the cached snapshot up front; PriceRefreshWorker calls updateAll()
        // after every fetch, which re-runs provideGlance with fresh data.
        val cached = PriceRepository.get(context).cache.read()
        provideContent {
            XrpWidgetContent(cached)
        }
    }
}
