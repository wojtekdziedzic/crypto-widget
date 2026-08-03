package com.xrpwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xrpwidget.data.CachedPrice
import com.xrpwidget.data.PriceRepository
import com.xrpwidget.ui.Formatters
import com.xrpwidget.widget.XrpWidgetReceiver
import com.xrpwidget.work.RefreshScheduler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen(
                    onRefresh = { RefreshScheduler.refreshNow(this) },
                    onAddWidget = ::requestWidgetPin,
                )
            }
        }
    }

    private fun requestWidgetPin() {
        val manager = getSystemService(AppWidgetManager::class.java)
        if (manager.isRequestPinAppWidgetSupported) {
            manager.requestPinAppWidget(
                ComponentName(this, XrpWidgetReceiver::class.java),
                null,
                null,
            )
        }
    }
}

@Composable
private fun MainScreen(onRefresh: () -> Unit, onAddWidget: () -> Unit) {
    val context = LocalContext.current
    val cached by PriceRepository.get(context).cache.cachedPrice
        .collectAsStateWithLifecycle(initialValue = CachedPrice())

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.last_price_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = cached.pricePln?.let(Formatters::price)
                    ?: stringResource(R.string.no_data),
                style = MaterialTheme.typography.displayMedium,
            )
            cached.change24hPercent?.let { change ->
                Text(
                    text = Formatters.change(change),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = cached.updatedAtEpochMillis?.let { millis ->
                    stringResource(R.string.updated_at, formatTimestamp(millis))
                } ?: stringResource(R.string.never_updated),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(32.dp))
            Button(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.refresh_now))
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onAddWidget, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.add_widget))
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.add_widget_hint),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun formatTimestamp(epochMillis: Long): String =
    SimpleDateFormat("HH:mm, d MMMM", Locale("pl", "PL")).format(Date(epochMillis))
