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

    private val chartCache =
        mutableMapOf<Triple<Coin, Currency, ChartRange>, Pair<Long, List<Float>>>()

    /**
     * Price history for the chart. Served from a short in-memory cache when
     * fresh (rate-limit friendly: rapid switching between selections does not
     * hit the API); on failure returns the stale cached series if present,
     * otherwise an empty list (no crash).
     */
    suspend fun fetchChart(coin: Coin, currency: Currency, range: ChartRange): List<Float> {
        val key = Triple(coin, currency, range)
        val cached = chartCache[key]
        if (cached != null && System.currentTimeMillis() - cached.first < CHART_CACHE_TTL_MILLIS) {
            return cached.second
        }
        return fetchChartRemote(coin, currency, range)
            ?.also { chartCache[key] = System.currentTimeMillis() to it }
            ?: cached?.second
            ?: emptyList()
    }

    private suspend fun fetchChartRemote(
        coin: Coin,
        currency: Currency,
        range: ChartRange,
    ): List<Float>? = try {
        val prices = api.getMarketChart(coin.coinGeckoId, currency.code, range.days).prices
        val windowMillis = range.windowHours?.let { it * 3_600_000.0 }
        val filtered = if (windowMillis != null && prices.isNotEmpty()) {
            val cutoff = (prices.last().getOrNull(0) ?: 0.0) - windowMillis
            prices.filter { (it.getOrNull(0) ?: 0.0) >= cutoff }
        } else {
            prices
        }
        val points = filtered.mapNotNull { it.getOrNull(1)?.toFloat() }
        // Decimate long series (e.g. 90 days hourly = 2160 points) to keep drawing light.
        if (points.size > MAX_CHART_POINTS) {
            val stride = (points.size + MAX_CHART_POINTS - 1) / MAX_CHART_POINTS
            points.filterIndexed { index, _ -> index % stride == 0 }
        } else {
            points
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "Chart fetch failed", e)
        null
    }

    companion object {
        private const val TAG = "PriceRepository"
        private const val BASE_URL = "https://api.coingecko.com/"
        private const val MAX_CHART_POINTS = 400
        private const val CHART_CACHE_TTL_MILLIS = 10 * 60 * 1000L

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
