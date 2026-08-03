package cloud.dziedzic.cryptowidget.data

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class MarketChartResponse(
    /** [[epochMillis, price], ...] */
    val prices: List<List<Double>> = emptyList(),
)

interface CoinGeckoApi {

    /**
     * Returns coinGeckoId -> { "pln": 4.06, "pln_24h_change": 0.8, "usd": ..., ... }.
     * Both ids and vs_currencies accept comma-separated lists, so all active
     * widget pairs are fetched in a single request.
     */
    @GET("api/v3/simple/price")
    suspend fun getSimplePrice(
        @Query("ids") ids: String,
        @Query("vs_currencies") vsCurrencies: String,
        @Query("include_24hr_change") include24hChange: Boolean = true,
    ): Map<String, Map<String, Double>>

    @GET("api/v3/coins/{id}/market_chart")
    suspend fun getMarketChart(
        @Path("id") coinGeckoId: String,
        @Query("vs_currency") vsCurrency: String,
        @Query("days") days: Int,
    ): MarketChartResponse
}
