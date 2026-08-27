package com.skippydream.strati.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = InkLight,
    onPrimary = OnInkLight,
    primaryContainer = InkContainerLight,
    onPrimaryContainer = OnInkContainerLight,
    secondaryContainer = SlateContainerLight,
    onSecondaryContainer = OnSlateContainerLight,
    tertiary = AmberLight,
    onTertiary = OnAmberLight,
    tertiaryContainer = AmberContainerLight,
    onTertiaryContainer = OnAmberContainerLight,
    background = PaperLight,
    onBackground = OnPaperLight,
    surface = PaperLight,
    onSurface = OnPaperLight,
    surfaceVariant = VariantLight,
    onSurfaceVariant = OnVariantLight,
    surfaceContainerLowest = CardLight,
    surfaceContainerLow = CardLight,
    surfaceContainer = CardHighLight,
    surfaceContainerHigh = CardHighLight,
    surfaceContainerHighest = CardHighLight,
    outline = OutlineLight,
)

private val DarkColors = darkColorScheme(
    primary = InkDark,
    onPrimary = OnInkDark,
    primaryContainer = InkContainerDark,
    onPrimaryContainer = OnInkContainerDark,
    secondaryContainer = SlateContainerDark,
    onSecondaryContainer = OnSlateContainerDark,
    tertiary = AmberDark,
    onTertiary = OnAmberDark,
    tertiaryContainer = AmberContainerDark,
    onTertiaryContainer = OnAmberContainerDark,
    background = NightDark,
    onBackground = OnNightDark,
    surface = NightDark,
    onSurface = OnNightDark,
    surfaceVariant = VariantDark,
    onSurfaceVariant = OnVariantDark,
    surfaceContainerLowest = CardDark,
    surfaceContainerLow = CardDark,
    surfaceContainer = CardDark,
    surfaceContainerHigh = CardHighDark,
    surfaceContainerHighest = CardHighDark,
    outline = OutlineDark,
)

// Niente Material You: l'app ha una palette propria.
@Composable
fun StratiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
