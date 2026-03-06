package com.sawalif.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ===== الألوان الأساسية لتطبيق سوالف =====
val Gold = Color(0xFFF4B942)
val GoldPrimary = Color(0xFFF4B942)
val Purple = Color(0xFF7B2FBE)
val DarkBg = Color(0xFF0A0A0F)
val BackgroundDark = Color(0xFF0A0A0F)
val CardBg = Color(0xFF0F0F1A)
val SurfaceDark = Color(0xFF0F0F1A)
val BorderColor = Color(0xFF1A1A2E)
val TextPrimary = Color(0xFFF0EDE8)
val TextWhite = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF888888)
val TextGray = Color(0xFF8A8A9A)
val TextDim = Color(0xFF555555)
val ErrorRed = Color(0xFFE74C3C)
val SuccessGreen = Color(0xFF2ECC71)

val AvatarPalette = listOf(
    Color(0xFFE8A87C),
    Color(0xFF7EB8D4),
    Color(0xFFA8D5A2),
    Color(0xFFD4A5C9),
    Color(0xFFF4B942),
    Color(0xFF7BA7D4),
    Color(0xFF7B2FBE),
    Color(0xFFE74C3C)
)

private val AppColorScheme = darkColorScheme(
    primary = GoldPrimary,
    secondary = Purple,
    background = DarkBg,
    surface = CardBg,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRed
)

@Composable
fun SawalifTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        content = content
    )
}
