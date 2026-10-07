package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004E57),
    onPrimaryContainer = Color(0xFF99F6FF),
    secondary = AmberAccent,
    onSecondary = Color(0xFF452200),
    secondaryContainer = Color(0xFF633200),
    onSecondaryContainer = Color(0xFFFFDCC1),
    tertiary = IndigoAccent,
    onTertiary = Color(0xFF1E2878),
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkSurfaceElevated,
    outlineVariant = KeyNumberBorder
)

// A clean dark scheme is provided even in "light" setting to fulfill the clean dark mode UI request
private val CleanDarkColorScheme = DarkColorScheme

@Composable
fun OmniCalcTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Defaulting to our polished clean dark mode theme as requested
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
