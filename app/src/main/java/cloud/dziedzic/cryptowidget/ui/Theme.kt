package cloud.dziedzic.cryptowidget.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// dziedzic.cloud brand palette
val BrandBackground = Color(0xFF0A0A0F)
val BrandCard = Color(0xFF12121A)
val BrandAccent = Color(0xFF00E5A0)
val BrandDanger = Color(0xFFFF4466)
val BrandText = Color(0xFFE8E8EF)
val BrandTextDim = Color(0xFF8888A0)
val BrandBorder = Color(0xFF2A2A3A)

@Composable
fun CryptoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = BrandAccent,
            onPrimary = BrandBackground,
            background = BrandBackground,
            onBackground = BrandText,
            surface = BrandCard,
            onSurface = BrandText,
            secondaryContainer = BrandCard,
            onSecondaryContainer = BrandAccent,
            outline = BrandBorder,
            error = BrandDanger,
        ),
        content = content,
    )
}
