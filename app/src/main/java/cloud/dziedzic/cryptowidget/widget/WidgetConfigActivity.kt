package cloud.dziedzic.cryptowidget.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import cloud.dziedzic.cryptowidget.R
import cloud.dziedzic.cryptowidget.data.Coin
import cloud.dziedzic.cryptowidget.data.Currency
import cloud.dziedzic.cryptowidget.data.WidgetConfig
import cloud.dziedzic.cryptowidget.data.WidgetConfigStore
import cloud.dziedzic.cryptowidget.ui.CryptoTheme
import cloud.dziedzic.cryptowidget.work.RefreshScheduler
import kotlinx.coroutines.launch

/** Shown when a widget is added (APPWIDGET_CONFIGURE); saves per-widget coin/currency. */
class WidgetConfigActivity : ComponentActivity() {

    private val appWidgetId: Int
        get() = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Cancelled by default: backing out keeps the widget on its defaults.
        setResult(RESULT_CANCELED, resultIntent())
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        setContent {
            CryptoTheme {
                ConfigScreen(onSave = ::saveAndFinish)
            }
        }
    }

    private fun resultIntent() =
        Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

    private fun saveAndFinish(config: WidgetConfig) {
        lifecycleScope.launch {
            android.util.Log.d("CryptoWidgetConfig", "save id=$appWidgetId config=$config")
            WidgetConfigStore(this@WidgetConfigActivity).save(appWidgetId, config)
            // Redraw this widget immediately so it shows the chosen coin right away
            // (with a placeholder price until the worker fetches the quote).
            runCatching {
                val glanceId = androidx.glance.appwidget.GlanceAppWidgetManager(this@WidgetConfigActivity)
                    .getGlanceIdBy(appWidgetId)
                CryptoPriceWidget().update(this@WidgetConfigActivity, glanceId)
            }
            // The worker fetches the (possibly new) pair and redraws all widgets.
            RefreshScheduler.refreshNow(this@WidgetConfigActivity)
            setResult(RESULT_OK, resultIntent())
            finish()
        }
    }
}

@Composable
private fun ConfigScreen(onSave: (WidgetConfig) -> Unit) {
    var coin by remember { mutableStateOf(Coin.DEFAULT) }
    var currency by remember { mutableStateOf(Currency.DEFAULT) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
        ) {
            Text(
                text = stringResource(R.string.config_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.config_coin),
                style = MaterialTheme.typography.titleMedium,
            )
            Coin.entries.forEach { option ->
                SelectableRow(
                    label = option.symbol,
                    selected = coin == option,
                    onClick = { coin = option },
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.config_currency),
                style = MaterialTheme.typography.titleMedium,
            )
            Currency.entries.forEach { option ->
                SelectableRow(
                    label = option.name,
                    selected = currency == option,
                    onClick = { currency = option },
                )
            }
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = { onSave(WidgetConfig(coin, currency)) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.config_save))
            }
        }
    }
}

@Composable
private fun SelectableRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
