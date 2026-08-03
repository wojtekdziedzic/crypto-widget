package cloud.dziedzic.cryptowidget.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.widgetConfigDataStore by preferencesDataStore(name = "widget_config")

/** Per-appWidgetId coin/currency selection; XRP/PLN when never configured. */
class WidgetConfigStore(private val context: Context) {

    private fun coinKey(appWidgetId: Int) = stringPreferencesKey("coin_$appWidgetId")
    private fun currencyKey(appWidgetId: Int) = stringPreferencesKey("currency_$appWidgetId")

    suspend fun configFor(appWidgetId: Int): WidgetConfig {
        val prefs = context.widgetConfigDataStore.data.first()
        return WidgetConfig(
            coin = Coin.from(prefs[coinKey(appWidgetId)]),
            currency = Currency.from(prefs[currencyKey(appWidgetId)]),
        )
    }

    suspend fun save(appWidgetId: Int, config: WidgetConfig) {
        context.widgetConfigDataStore.edit { prefs ->
            prefs[coinKey(appWidgetId)] = config.coin.name
            prefs[currencyKey(appWidgetId)] = config.currency.name
        }
    }

    suspend fun remove(appWidgetIds: IntArray) {
        context.widgetConfigDataStore.edit { prefs ->
            for (id in appWidgetIds) {
                prefs.remove(coinKey(id))
                prefs.remove(currencyKey(id))
            }
        }
    }

    /** Last coin/currency viewed in the app (not tied to any widget). */
    suspend fun appSelection(): WidgetConfig {
        val prefs = context.widgetConfigDataStore.data.first()
        return WidgetConfig(
            coin = Coin.from(prefs[stringPreferencesKey("coin_app")]),
            currency = Currency.from(prefs[stringPreferencesKey("currency_app")]),
        )
    }

    suspend fun saveAppSelection(config: WidgetConfig) {
        context.widgetConfigDataStore.edit { prefs ->
            prefs[stringPreferencesKey("coin_app")] = config.coin.name
            prefs[stringPreferencesKey("currency_app")] = config.currency.name
        }
    }

    /** Distinct coin/currency pairs of all configured widgets; default pair when none. */
    suspend fun activePairs(): Set<Pair<Coin, Currency>> {
        val prefs = context.widgetConfigDataStore.data.first()
        val ids = prefs.asMap().keys
            .mapNotNull { it.name.substringAfter("coin_", "").toIntOrNull() }
        val pairs = ids.map { id ->
            Coin.from(prefs[coinKey(id)]) to Currency.from(prefs[currencyKey(id)])
        }.toSet()
        return pairs.ifEmpty { setOf(Coin.DEFAULT to Currency.DEFAULT) }
    }
}
