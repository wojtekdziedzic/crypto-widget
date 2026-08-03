package com.xrpwidget.ui

import java.util.Locale
import kotlin.math.abs

object Formatters {

    private val POLISH = Locale("pl", "PL")

    /** Formats the price as "zł4,06" (comma decimal separator, 2 decimal places). */
    fun price(pricePln: Double): String =
        "zł" + String.format(POLISH, "%.2f", pricePln)

    /** Formats the 24h change as "0,8% ▲" for gains and "1,2% ▼" for losses. */
    fun change(change24hPercent: Double): String {
        val arrow = if (change24hPercent < 0) "▼" else "▲"
        return String.format(POLISH, "%.1f%% %s", abs(change24hPercent), arrow)
    }
}
