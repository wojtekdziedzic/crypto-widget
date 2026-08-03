# crypto-widget

Native Android home screen widget showing crypto prices (BTC / ETH / XRP in PLN or USD).
Built under the [dziedzic.cloud](https://dziedzic.cloud) umbrella.

**Download: [latest APK release](https://github.com/wojtekdziedzic/crypto-widget/releases/latest)** (Android 8.0+, sideloading required).

## Features

- 2x2 Jetpack Glance widget: price, 24h change (green/red), manual refresh on tap
- Per-widget configuration: pick the coin (BTC/ETH/XRP) and currency (PLN/USD) when adding;
  multiple widgets with different pairs can live side by side
- In-app 7-day price chart with coin/currency switcher
- Auto-refresh every 30 minutes via WorkManager (single API call for all widgets)
- Last known prices cached in DataStore; survives network failures and reboots
- Responsive widget layout: adapts when resized smaller
- dziedzic.cloud brand palette (black background, #00E5A0 accent)
- Data source: [CoinGecko public API](https://www.coingecko.com/) (no API key)

## Tech stack

Kotlin, Jetpack Glance (appwidget), WorkManager, Retrofit 2.11 + kotlinx.serialization,
DataStore Preferences, Jetpack Compose. minSdk 26, targetSdk 35,
Gradle Kotlin DSL with a version catalog.

## Build

Requires JDK 17 and the Android SDK (platform 35). Point `local.properties` at your SDK:

```
sdk.dir=/path/to/android-sdk
```

Then:

```
./gradlew :app:assembleDebug
```

Release builds are signed via `keystore.properties` (not in the repo); without it
`assembleRelease` produces an unsigned APK.
