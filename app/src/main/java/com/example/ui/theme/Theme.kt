package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val NoorDarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = EmeraldDeep,
    primaryContainer = EmeraldElevated,
    onPrimaryContainer = GoldLight,
    secondary = TealHighlight,
    onSecondary = EmeraldDeep,
    secondaryContainer = EmeraldCard,
    onSecondaryContainer = TextCream,
    tertiary = GoldAccent,
    background = EmeraldDeep,
    onBackground = TextCream,
    surface = EmeraldSurface,
    onSurface = TextCream,
    surfaceVariant = EmeraldCard,
    onSurfaceVariant = TextSecondarySage,
    error = CoralError,
    outline = GoldMuted
)

private val NoorLightColorScheme = lightColorScheme(
    primary = Color(0xFF0B4F3F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9EFE8),
    onPrimaryContainer = Color(0xFF07251E),
    secondary = Color(0xFFB5891C),
    onSecondary = Color.White,
    background = MushafParchment,
    onBackground = MushafInkDark,
    surface = Color(0xFFFFFDF7),
    onSurface = MushafInkDark,
    surfaceVariant = Color(0xFFF2EAD7),
    onSurfaceVariant = Color(0xFF4F4634),
    error = Color(0xFFB3261E),
    outline = MushafBorderGold
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NoorDarkColorScheme else NoorLightColorScheme

    // Provide RTL layout direction by default for authentic Arabic Quranic UI
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
