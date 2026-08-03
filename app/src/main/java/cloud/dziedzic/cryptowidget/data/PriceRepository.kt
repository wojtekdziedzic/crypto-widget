package cloud.dziedzic.cryptowidget.data

import android.content.Context
import android.util.Log
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class PriceRepository private constructor(
    private val api: CoinGeckoApi,
    val cache: PriceCache,
    val configStore: WidgetConfigStore,
) {

    /**
     * Fetches quotes for every coin/currency pair used by any widget in one
     * API call and stores them in the cache. On failure the cache is left
     * untouched so widgets keep showing the last known values.
     */
    suspend fun refreshAll(): Result<Unit> = try {
        val pairs = configStore.activePairs()
        val ids = pairs.map { it.first.coinGeckoId }.distinct().joinToString(",")
        val currencies = pairs.map { it.second.code }.distinct().joinToString(",")
        val response = api.getSimplePrice(ids, currencies)
        val updatedAt = System.currentTimeMillis()
        for ((coin, currency) in pairs) {
            val quote = response[coin.coinGeckoId] ?: continue
            val price = quote[currency.code] ?: continue
            cache.save(coin, currency, price, quote["${currency.code}_24h_change"], updatedAt)
        }
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "Price refresh failed, keeping cached values", e)
        Result.failure(e)
    }

    /** Fetches and caches a single pair (used by the app for the viewed selection). */
    suspend fun refreshPair(coin: Coin, currency: Currency): Result<Unit> = try {
        val response = api.getSimplePrice(coin.coinGeckoId, currency.code)
        val quote = response[coin.coinGeckoId] ?: error("Missing ${coin.coinGeckoId} in response")
        val price = quote[currency.code] ?: error("Missing ${currency.code} price")
        cache.save(coin, currency, price, quote["${currency.code}_24h_change"], System.currentTimeMillis())
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "Pair refresh failed", e)
        Result.failure(e)
    }

    /** 7-day price history for the chart; empty list on failure (no crash, no cache). */
    suspend fun fetchChart(coin: Coin, currency: Currency, days: Int = 7): List<Float> = try {
        api.getMarketChart(coin.coinGeckoId, currency.code, days)
            .prices.mapNotNull { it.getOrNull(1)?.toFloat() }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "Chart fetch failed", e)
        emptyList()
    }

    companion object {
        private const val TAG = "PriceRepository"
        private const val BASE_URL = "https://api.coingecko.com/"

        @Volatile
        private var instance: PriceRepository? = null

        fun get(context: Context): PriceRepository =
            instance ?: synchronized(this) {
                instance ?: create(context.applicationContext).also { instance = it }
            }

        private fun create(appContext: Context): PriceRepository {
            val json = Json { ignoreUnknownKeys = true }
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
            return PriceRepository(
                api = retrofit.create(CoinGeckoApi::class.java),
                cache = PriceCache(appContext),
                configStore = WidgetConfigStore(appContext),
            )
        }
    }
}
