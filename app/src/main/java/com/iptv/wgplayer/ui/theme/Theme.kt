package com.iptv.wgplayer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Cohesive Dark Gold, Pure Black & Dark Gray Palette
val BgBlack = Color(0xFF08080A)
val SurfaceDarkGray = Color(0xFF131317)
val CardDarkGray = Color(0xFF1C1C22)
val ElevatedDarkGray = Color(0xFF24242C)
val BorderDarkGray = Color(0xFF2E2E37)

val GoldDark = Color(0xFFC59B27)
val GoldLight = Color(0xFFE2BE57)
val GoldMuted = Color(0xFF7D621B)

val StatusGreen = Color(0xFF10B981)
val StatusRed = Color(0xFFDC2626)

val TextPrimary = Color(0xFFF5F2EA)
val TextMuted = Color(0xFF9F9FA8)

private val VipGoldDarkColorScheme = darkColorScheme(
    primary = GoldDark,
    secondary = GoldLight,
    tertiary = StatusGreen,
    background = BgBlack,
    surface = SurfaceDarkGray,
    surfaceVariant = CardDarkGray,
    outline = BorderDarkGray,
    onPrimary = BgBlack,
    onSecondary = BgBlack,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextMuted,
    error = StatusRed
)

@Composable
fun VipPlayerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VipGoldDarkColorScheme,
        content = content
    )
}
