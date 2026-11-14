package com.sortify.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val SortifyGreen = Color(0xFF1ED76A)
private val DarkScheme = darkColorScheme(
    primary = SortifyGreen, onPrimary = Color(0xFF00210D),
    primaryContainer = Color(0xFF164B31), onPrimaryContainer = Color(0xFFD0FFDD),
    background = Color(0xFF00160D), onBackground = Color(0xFFF5FAF6),
    surface = Color(0xFF091F16), onSurface = Color(0xFFF5FAF6),
    surfaceVariant = Color(0xFF20352A), onSurfaceVariant = Color(0xFFB8C8BE),
    secondaryContainer = Color(0xFF2A3D32), onSecondaryContainer = Color(0xFFE1F2E7),
    outline = Color(0xFF728A7C), outlineVariant = Color(0xFF344D3F),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    inverseSurface = Color(0xFFDCECE0), inverseOnSurface = Color(0xFF102C1B), inversePrimary = Color(0xFF007B36),
    secondary = Color(0xFFA1D8B6), onSecondary = Color(0xFF073A20), surfaceTint = SortifyGreen,
    surfaceContainerLowest = Color(0xFF001108), surfaceContainerLow = Color(0xFF0D2419),
    surfaceContainer = Color(0xFF10291D), surfaceContainerHigh = Color(0xFF1B3427), surfaceContainerHighest = Color(0xFF233E30),
)
private val LightScheme = lightColorScheme(
    primary = Color(0xFF007B36), onPrimary = Color.White,
    primaryContainer = Color(0xFFB1F6C6), onPrimaryContainer = Color(0xFF00210D),
    background = Color(0xFFF3FAF4), onBackground = Color(0xFF0F2116),
    surface = Color(0xFFF7FFF8), onSurface = Color(0xFF0F2116),
    surfaceVariant = Color(0xFFDFEDE1), onSurfaceVariant = Color(0xFF405A48),
    secondaryContainer = Color(0xFFD7EADB), onSecondaryContainer = Color(0xFF102C1B),
    outline = Color(0xFF647A69), outlineVariant = Color(0xFFB7CCBD),
    secondary = Color(0xFF3C674C), onSecondary = Color.White, surfaceTint = Color(0xFF007B36),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF0F8F0),
    surfaceContainer = Color(0xFFEAF3EB), surfaceContainerHigh = Color(0xFFE4EEE5), surfaceContainerHighest = Color(0xFFDEEADE),
)

val SortifyTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 48.sp, lineHeight = 52.sp, letterSpacing = (-1.4).sp),
    displaySmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 38.sp, lineHeight = 43.sp, letterSpacing = (-0.9).sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.6).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 27.sp, lineHeight = 33.sp, letterSpacing = (-0.5).sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 27.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
)

@Composable
fun SortifyTheme(preference: String, content: @Composable () -> Unit) {
    val dark = preference == "Dark" || (preference == "System" && isSystemInDarkTheme())
    MaterialTheme(colorScheme = if (dark) DarkScheme else LightScheme, typography = SortifyTypography, content = content)
}
