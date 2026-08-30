package com.example.bouncer.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    secondary = AccentPurple,
    onSecondary = Color.White,
    tertiary = BouncerAmber,
    background = DarkSurface,
    onBackground = TextPrimary,
    surface = DarkSurfaceVariant,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    error = BouncerRed,
    onError = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    secondary = AccentPurple,
    onSecondary = Color.White,
    tertiary = BouncerAmber,
    error = BouncerRedDark,
    onError = Color.White,
)

@Composable
fun BouncerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Bouncer uses a dark-first design; always dark for v1
    val colorScheme = DarkColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
