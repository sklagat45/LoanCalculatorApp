package com.srklagat.loancalculatorapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = LightGreen,
    onPrimaryContainer = DarkGreen,
    secondary = AccentGreen,
    onSecondary = Color.White,
    secondaryContainer = ButtonGreen,
    onSecondaryContainer = DarkGreen,
    tertiary = BrightGreen,
    background = SurfaceLight,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    error = ErrorRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = AccentGreen,
    onPrimary = DarkGreen,
    primaryContainer = PrimaryGreen,
    onPrimaryContainer = Color.White,
    secondary = ButtonGreen,
    onSecondary = DarkGreen,
    secondaryContainer = PrimaryGreen,
    onSecondaryContainer = Color.White,
    tertiary = BrightGreen,
    background = DarkBackground,
    onBackground = TextOnDarkSurface,
    surface = DarkSurface,
    onSurface = TextOnDarkSurface,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF424242),
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun LoanCalculatorAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}