package com.xrpwidget.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response shape of CoinGecko simple/price:
 * { "ripple": { "pln": 4.06, "pln_24h_change": 0.8 } }
 */
@Serializable
data class SimplePriceResponse(
    @SerialName("ripple") val ripple: RippleQuote? = null,
)

@Serializable
data class RippleQuote(
    @SerialName("pln") val pln: Double,
    @SerialName("pln_24h_change") val pln24hChange: Double? = null,
)
