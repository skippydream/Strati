package com.skippydream.strati.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.skippydream.strati.R

val Fraunces = FontFamily(Font(R.font.fraunces, FontWeight.SemiBold))

// Senza questo il glifo si appoggia in alto nella riga e il testo sembra scentrato
// dentro le card.
private val CenteredLines = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private val NoFontPadding = PlatformTextStyle(includeFontPadding = false)

val Typography = Typography(
    // Titolo dell'app: l'unico posto in cui compare Fraunces.
    displaySmall = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        lineHeightStyle = CenteredLines,
        platformStyle = NoFontPadding,
    ),
    headlineLarge = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        lineHeightStyle = CenteredLines,
        platformStyle = NoFontPadding,
    ),
    // La domanda: deve leggersi a voce da un tavolo.
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        lineHeightStyle = CenteredLines,
        platformStyle = NoFontPadding,
    ),
    titleLarge = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        lineHeightStyle = CenteredLines,
        platformStyle = NoFontPadding,
    ),
    titleMedium = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        lineHeightStyle = CenteredLines,
        platformStyle = NoFontPadding,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        lineHeightStyle = CenteredLines,
        platformStyle = NoFontPadding,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        lineHeightStyle = CenteredLines,
        platformStyle = NoFontPadding,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        lineHeightStyle = CenteredLines,
        platformStyle = NoFontPadding,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        lineHeightStyle = CenteredLines,
        platformStyle = NoFontPadding,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        lineHeightStyle = CenteredLines,
        platformStyle = NoFontPadding,
    ),
)
