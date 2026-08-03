package com.xrpwidget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.xrpwidget.R
import com.xrpwidget.data.CachedPrice
import com.xrpwidget.widget.RefreshAction

private val BLACK = ColorProvider(Color.Black)
private val CHANGE_POSITIVE = ColorProvider(Color(0xFF00C853))
private val CHANGE_NEGATIVE = ColorProvider(Color(0xFFE53935))

@Composable
fun XrpWidgetContent(cached: CachedPrice) {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_card_bg))
            .clickable(actionRunCallback<RefreshAction>())
            .padding(16.dp),
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Image(
                provider = ImageProvider(R.drawable.ic_xrp),
                contentDescription = context.getString(R.string.cd_xrp_logo),
                modifier = GlanceModifier.size(22.dp),
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Image(
                provider = ImageProvider(R.drawable.ic_refresh),
                contentDescription = context.getString(R.string.cd_refresh),
                modifier = GlanceModifier
                    .size(22.dp)
                    .clickable(actionRunCallback<RefreshAction>()),
            )
        }
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = context.getString(R.string.xrp_symbol),
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BLACK),
            )
            Text(
                text = cached.pricePln?.let(Formatters::price)
                    ?: context.getString(R.string.no_data),
                style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = BLACK),
            )
            val change = cached.change24hPercent
            if (change != null) {
                Text(
                    text = Formatters.change(change),
                    style = TextStyle(
                        fontSize = 22.sp,
                        color = if (change < 0) CHANGE_NEGATIVE else CHANGE_POSITIVE,
                    ),
                )
            }
        }
    }
}
