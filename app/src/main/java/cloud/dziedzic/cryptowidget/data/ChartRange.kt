package cloud.dziedzic.cryptowidget.data

/**
 * Chart time ranges. CoinGecko granularity is automatic: days=1 gives
 * 5-minute points, 2-90 hourly, >90 daily. Sub-day ranges (6H/1H) reuse
 * the days=1 data filtered client-side; 1H (~12 points) is the floor,
 * minute-scale ranges are not possible with this granularity.
 */
enum class ChartRange(val label: String, val days: Int, val windowHours: Int? = null) {
    Y1("1R", 365),
    M3("3M", 90),
    M1("1M", 30),
    D7("7D", 7),
    D1("1D", 1),
    H6("6H", 1, 6),
    H1("1H", 1, 1),
}
