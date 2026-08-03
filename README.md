# xrp-widget

Native Android home screen widget showing the current XRP price in PLN.

**Download: [latest APK release](https://github.com/wojtekdziedzic/xrp-widget/releases/latest)** (Android 8.0+, sideloading required).

## Features

- 2x2 Jetpack Glance widget: price, 24h change (green/red), manual refresh on tap
- Auto-refresh every 30 minutes via WorkManager (network-constrained, battery friendly)
- Last known price cached in DataStore; survives network failures and reboots
- Responsive layout: adapts when the widget is resized smaller
- Data source: [CoinGecko public API](https://www.coingecko.com/) (no API key)

## Tech stack

Kotlin, Jetpack Glance (appwidget), WorkManager, Retrofit 2.11 + kotlinx.serialization,
DataStore Preferences, Jetpack Compose (single-screen app). minSdk 26, targetSdk 35,
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

APK lands in `app/build/outputs/apk/debug/app-debug.apk`.
