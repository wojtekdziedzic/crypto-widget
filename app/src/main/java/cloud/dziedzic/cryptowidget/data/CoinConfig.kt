package cloud.dziedzic.cryptowidget.data

import cloud.dziedzic.cryptowidget.R

enum class Coin(val coinGeckoId: String, val symbol: String, val iconRes: Int) {
    BTC("bitcoin", "BTC", R.drawable.ic_btc),
    ETH("ethereum", "ETH", R.drawable.ic_eth),
    XRP("ripple", "XRP", R.drawable.ic_xrp);

    companion object {
        val DEFAULT = XRP
        fun from(name: String?): Coin = entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}

enum class Currency(val code: String, val prefix: String) {
    PLN("pln", "zł"),
    USD("usd", "$");

    companion object {
        val DEFAULT = PLN
        fun from(name: String?): Currency = entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}

data class WidgetConfig(
    val coin: Coin = Coin.DEFAULT,
    val currency: Currency = Currency.DEFAULT,
)
