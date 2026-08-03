package com.xrpwidget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.background
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
    // Below this height the full three-line layout does not fit.
    val compact = LocalSize.current.height < 130.dp
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_card_bg))
            .clickable(actionRunCallback<RefreshAction>())
            .padding(if (compact) 10.dp else 12.dp),
    ) {
        IconsRow(iconSize = if (compact) 18.dp else 20.dp)
        Spacer(modifier = GlanceModifier.defaultWeight())
        if (!compact) {
            Text(
                text = LocalContext.current.getString(R.string.xrp_symbol),
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BLACK),
            )
        }
        Text(
            text = cached.pricePln?.let(Formatters::price)
                ?: LocalContext.current.getString(R.string.no_data),
            style = TextStyle(
                fontSize = if (compact) 26.sp else 32.sp,
                fontWeight = FontWeight.Bold,
                color = BLACK,
            ),
        )
        val change = cached.change24hPercent
        if (change != null) {
            Text(
                text = Formatters.change(change),
                style = TextStyle(
                    fontSize = if (compact) 16.sp else 22.sp,
                    color = if (change < 0) CHANGE_NEGATIVE else CHANGE_POSITIVE,
                ),
            )
        }
        Spacer(modifier = GlanceModifier.defaultWeight())
    }
}

@Composable
private fun IconsRow(iconSize: androidx.compose.ui.unit.Dp) {
    val context = LocalContext.current
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        Image(
            provider = ImageProvider(R.drawable.ic_xrp),
            contentDescription = context.getString(R.string.cd_xrp_logo),
            modifier = GlanceModifier.size(iconSize),
        )
        Spacer(modifier = GlanceModifier.defaultWeight())
        Image(
            provider = ImageProvider(R.drawable.ic_refresh),
            contentDescription = context.getString(R.string.cd_refresh),
            modifier = GlanceModifier
                .size(iconSize)
                .clickable(actionRunCallback<RefreshAction>()),
        )
    }
}
