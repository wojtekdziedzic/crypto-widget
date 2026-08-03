package cloud.dziedzic.cryptowidget.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.priceDataStore by preferencesDataStore(name = "price_cache")

/** Last successfully fetched quote of one coin/currency pair. */
data class CachedPrice(
    val price: Double? = null,
    val change24hPercent: Double? = null,
    val updatedAtEpochMillis: Long? = null,
)

class PriceCache(private val context: Context) {

    private fun priceKey(coin: Coin, currency: Currency) =
        doublePreferencesKey("price_${coin.coinGeckoId}_${currency.code}")

    private fun changeKey(coin: Coin, currency: Currency) =
        doublePreferencesKey("change_${coin.coinGeckoId}_${currency.code}")

    private fun updatedKey(coin: Coin, currency: Currency) =
        longPreferencesKey("updated_${coin.coinGeckoId}_${currency.code}")

    private fun Preferences.quote(coin: Coin, currency: Currency) = CachedPrice(
        price = this[priceKey(coin, currency)],
        change24hPercent = this[changeKey(coin, currency)],
        updatedAtEpochMillis = this[updatedKey(coin, currency)],
    )

    fun quoteFlow(coin: Coin, currency: Currency): Flow<CachedPrice> =
        context.priceDataStore.data.map { it.quote(coin, currency) }

    suspend fun read(coin: Coin, currency: Currency): CachedPrice =
        context.priceDataStore.data.first().quote(coin, currency)

    fun allQuotesFlow(pairs: Collection<Pair<Coin, Currency>>): Flow<Map<Pair<Coin, Currency>, CachedPrice>> =
        context.priceDataStore.data.map { prefs ->
            pairs.associateWith { (coin, currency) -> prefs.quote(coin, currency) }
        }

    suspend fun save(
        coin: Coin,
        currency: Currency,
        price: Double,
        change24hPercent: Double?,
        updatedAtEpochMillis: Long,
    ) {
        context.priceDataStore.edit { prefs ->
            prefs[priceKey(coin, currency)] = price
            if (change24hPercent != null) {
                prefs[changeKey(coin, currency)] = change24hPercent
            } else {
                prefs.remove(changeKey(coin, currency))
            }
            prefs[updatedKey(coin, currency)] = updatedAtEpochMillis
        }
    }
}
