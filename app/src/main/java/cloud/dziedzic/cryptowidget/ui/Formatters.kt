package cloud.dziedzic.cryptowidget.ui

import cloud.dziedzic.cryptowidget.data.Currency
import java.util.Locale
import kotlin.math.abs

object Formatters {

    private val POLISH = Locale("pl", "PL")

    /**
     * "zł4,06" / "$1,08" for small prices, "zł250 123" for large ones
     * (grouping instead of decimals so BTC still fits on the widget).
     */
    fun price(value: Double, currency: Currency): String {
        val number = when {
            value >= 1000 -> String.format(POLISH, "%,.0f", value)
            value >= 1 -> String.format(POLISH, "%.2f", value)
            else -> String.format(POLISH, "%.4f", value)
        }
        return currency.prefix + number
    }

    /** "0,8% ▲" for gains, "1,2% ▼" for losses. */
    fun change(change24hPercent: Double): String {
        val arrow = if (change24hPercent < 0) "▼" else "▲"
        return String.format(POLISH, "%.1f%% %s", abs(change24hPercent), arrow)
    }
}
