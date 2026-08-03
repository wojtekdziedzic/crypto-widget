package com.xrpwidget.data

import retrofit2.http.GET
import retrofit2.http.Query

interface CoinGeckoApi {

    @GET("api/v3/simple/price")
    suspend fun getSimplePrice(
        @Query("ids") ids: String = "ripple",
        @Query("vs_currencies") vsCurrencies: String = "pln",
        @Query("include_24hr_change") include24hChange: Boolean = true,
    ): SimplePriceResponse
}
