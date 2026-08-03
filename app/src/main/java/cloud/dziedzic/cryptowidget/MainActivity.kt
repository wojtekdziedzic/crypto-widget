package cloud.dziedzic.cryptowidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cloud.dziedzic.cryptowidget.data.CachedPrice
import cloud.dziedzic.cryptowidget.data.Coin
import cloud.dziedzic.cryptowidget.data.Currency
import cloud.dziedzic.cryptowidget.data.PriceRepository
import cloud.dziedzic.cryptowidget.ui.BrandAccent
import cloud.dziedzic.cryptowidget.ui.BrandCard
import cloud.dziedzic.cryptowidget.ui.BrandDanger
import cloud.dziedzic.cryptowidget.ui.BrandTextDim
import cloud.dziedzic.cryptowidget.ui.CryptoTheme
import cloud.dziedzic.cryptowidget.ui.Formatters
import cloud.dziedzic.cryptowidget.ui.PriceChart
import cloud.dziedzic.cryptowidget.widget.CryptoWidgetReceiver
import cloud.dziedzic.cryptowidget.work.RefreshScheduler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CryptoTheme {
                MainScreen(onAddWidget = ::requestWidgetPin)
            }
        }
    }

    private fun requestWidgetPin() {
        val manager = getSystemService(AppWidgetManager::class.java)
        if (manager.isRequestPinAppWidgetSupported) {
            // The system fills EXTRA_APPWIDGET_ID into this intent and fires it
            // after the widget is pinned, so the config screen opens right away.
            var flags = PendingIntent.FLAG_UPDATE_CURRENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags = flags or PendingIntent.FLAG_MUTABLE
            }
            val configCallback = PendingIntent.getActivity(
                this,
                0,
                Intent(this, cloud.dziedzic.cryptowidget.widget.WidgetConfigActivity::class.java),
                flags,
            )
            manager.requestPinAppWidget(
                ComponentName(this, CryptoWidgetReceiver::class.java),
                null,
                configCallback,
            )
        }
    }
}

@Composable
private fun MainScreen(onAddWidget: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { PriceRepository.get(context) }
    val scope = rememberCoroutineScope()

    var coin by rememberSaveable { mutableStateOf(Coin.DEFAULT) }
    var currency by rememberSaveable { mutableStateOf(Currency.DEFAULT) }
    var chart by remember { mutableStateOf<List<Float>>(emptyList()) }
    var chartLoaded by remember { mutableStateOf(false) }
    var selectionRestored by rememberSaveable { mutableStateOf(false) }

    val cached by remember(coin, currency) { repository.cache.quoteFlow(coin, currency) }
        .collectAsStateWithLifecycle(initialValue = CachedPrice())

    // Restore the last viewed pair once per process, then persist every change.
    LaunchedEffect(Unit) {
        if (!selectionRestored) {
            val selection = repository.configStore.appSelection()
            coin = selection.coin
            currency = selection.currency
            selectionRestored = true
        }
    }

    LaunchedEffect(coin, currency, selectionRestored) {
        if (!selectionRestored) return@LaunchedEffect
        repository.configStore.saveAppSelection(
            cloud.dziedzic.cryptowidget.data.WidgetConfig(coin, currency),
        )
        chartLoaded = false
        repository.refreshPair(coin, currency)
        chart = repository.fetchChart(coin, currency)
        chartLoaded = true
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            // 1. Coin + currency selection
            SegmentedRow(
                options = Coin.entries.map { it.symbol },
                selectedIndex = Coin.entries.indexOf(coin),
                onSelect = { coin = Coin.entries[it] },
            )
            Spacer(Modifier.height(12.dp))
            SegmentedRow(
                options = Currency.entries.map { it.name },
                selectedIndex = Currency.entries.indexOf(currency),
                onSelect = { currency = Currency.entries[it] },
            )

            // 2. Chart
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.chart_7d),
                style = MaterialTheme.typography.labelLarge,
                color = BrandTextDim,
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BrandCard),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    chart.size >= 2 -> PriceChart(chart, Modifier.padding(8.dp))
                    chartLoaded -> Text(
                        text = stringResource(R.string.chart_unavailable),
                        color = BrandTextDim,
                    )
                    else -> Text(text = "…", color = BrandTextDim)
                }
            }

            // 3. Current quote
            Spacer(Modifier.height(24.dp))
            Text(
                text = cached.price?.let { Formatters.price(it, currency) }
                    ?: stringResource(R.string.no_data),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
            )
            cached.change24hPercent?.let { change ->
                Text(
                    text = Formatters.change(change),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (change < 0) BrandDanger else BrandAccent,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = cached.updatedAtEpochMillis?.let { millis ->
                    stringResource(R.string.updated_at, formatTimestamp(millis))
                } ?: stringResource(R.string.never_updated),
                style = MaterialTheme.typography.bodyMedium,
                color = BrandTextDim,
            )

            // 4. Actions + hint
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = {
                    scope.launch { repository.refreshPair(coin, currency) }
                    RefreshScheduler.refreshNow(context)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.refresh_now))
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onAddWidget, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.add_widget))
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.add_widget_hint),
                style = MaterialTheme.typography.bodySmall,
                color = BrandTextDim,
            )
        }
    }
}

@Composable
private fun SegmentedRow(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BrandCard),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) BrandAccent else BrandCard)
                    .clickable { onSelect(index) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

private fun formatTimestamp(epochMillis: Long): String =
    SimpleDateFormat("HH:mm, d MMMM", Locale("pl", "PL")).format(Date(epochMillis))
