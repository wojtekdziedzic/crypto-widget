package com.xrpwidget.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.priceDataStore by preferencesDataStore(name = "price_cache")

/** Last successfully fetched price; all fields null until the first fetch succeeds. */
data class CachedPrice(
    val pricePln: Double? = null,
    val change24hPercent: Double? = null,
    val updatedAtEpochMillis: Long? = null,
)

class PriceCache(private val context: Context) {

    private object Keys {
        val PRICE = doublePreferencesKey("price_pln")
        val CHANGE = doublePreferencesKey("change_24h_percent")
        val UPDATED_AT = longPreferencesKey("updated_at_epoch_millis")
    }

    val cachedPrice: Flow<CachedPrice> = context.priceDataStore.data.map { prefs ->
        CachedPrice(
            pricePln = prefs[Keys.PRICE],
            change24hPercent = prefs[Keys.CHANGE],
            updatedAtEpochMillis = prefs[Keys.UPDATED_AT],
        )
    }

    suspend fun read(): CachedPrice = cachedPrice.first()

    suspend fun save(pricePln: Double, change24hPercent: Double?, updatedAtEpochMillis: Long) {
        context.priceDataStore.edit { prefs ->
            prefs[Keys.PRICE] = pricePln
            if (change24hPercent != null) {
                prefs[Keys.CHANGE] = change24hPercent
            } else {
                prefs.remove(Keys.CHANGE)
            }
            prefs[Keys.UPDATED_AT] = updatedAtEpochMillis
        }
    }
}
