package com.xrpwidget.data

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
) {

    /**
     * Fetches the current price and stores it in the cache.
     * On any failure the cache is left untouched so the widget keeps
     * showing the last known value.
     */
    suspend fun refresh(): Result<CachedPrice> = try {
        val quote = api.getSimplePrice().ripple
            ?: error("Missing 'ripple' node in CoinGecko response")
        val updatedAt = System.currentTimeMillis()
        cache.save(quote.pln, quote.pln24hChange, updatedAt)
        Result.success(CachedPrice(quote.pln, quote.pln24hChange, updatedAt))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "Price refresh failed, keeping cached value", e)
        Result.failure(e)
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
            )
        }
    }
}
