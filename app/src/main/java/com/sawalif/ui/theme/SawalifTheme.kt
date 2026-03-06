package com.sawalif.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val GoldPrimary = Color(0xFFF4B942)
val BackgroundDark = Color(0xFF0A0A0F)
val SurfaceDark = Color(0xFF0F0F1A)
val TextWhite = Color(0xFFFFFFFF)
val TextGray = Color(0xFF8A8A9A)

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = Color.Black,
    onBackground = TextWhite,
    onSurface = TextWhite,
    secondary = TextGray,
    onSecondary = TextWhite
)

@Composable
fun SawalifTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
