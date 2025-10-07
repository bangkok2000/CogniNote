package com.cogninote.app.presentation.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Notesnook-inspired color palette
val NotesnookPurple = Color(0xFF8B5CF6)
val NotesnookPurpleVariant = Color(0xFF7C3AED)
val NotesnookPurpleDark = Color(0xFF6D28D9)
val NotesnookPurpleLight = Color(0xFFA78BFA)

val NotesnookBackground = Color(0xFFFAFAFA)
val NotesnookSurface = Color(0xFFFFFFFF)
val NotesnookSurfaceVariant = Color(0xFFF3F4F6)
val NotesnookOutline = Color(0xFFE5E7EB)

val NotesnookDarkBackground = Color(0xFF0F0F0F)
val NotesnookDarkSurface = Color(0xFF1A1A1A)
val NotesnookDarkSurfaceVariant = Color(0xFF2D2D2D)
val NotesnookDarkOutline = Color(0xFF404040)

val NotesnookGray50 = Color(0xFFF9FAFB)
val NotesnookGray100 = Color(0xFFF3F4F6)
val NotesnookGray200 = Color(0xFFE5E7EB)
val NotesnookGray300 = Color(0xFFD1D5DB)
val NotesnookGray400 = Color(0xFF9CA3AF)
val NotesnookGray500 = Color(0xFF6B7280)
val NotesnookGray600 = Color(0xFF4B5563)
val NotesnookGray700 = Color(0xFF374151)
val NotesnookGray800 = Color(0xFF1F2937)
val NotesnookGray900 = Color(0xFF111827)

// Notesnook Light Theme
val NotesnookLightColorScheme = lightColorScheme(
    primary = NotesnookPurple,
    onPrimary = Color.White,
    primaryContainer = NotesnookPurpleLight.copy(alpha = 0.1f),
    onPrimaryContainer = NotesnookPurpleDark,
    
    secondary = NotesnookGray600,
    onSecondary = Color.White,
    secondaryContainer = NotesnookGray100,
    onSecondaryContainer = NotesnookGray800,
    
    tertiary = NotesnookPurpleVariant,
    onTertiary = Color.White,
    tertiaryContainer = NotesnookPurpleLight.copy(alpha = 0.15f),
    onTertiaryContainer = NotesnookPurpleDark,
    
    error = Color(0xFFEF4444),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFFDC2626),
    
    background = NotesnookBackground,
    onBackground = NotesnookGray900,
    
    surface = NotesnookSurface,
    onSurface = NotesnookGray900,
    surfaceVariant = NotesnookSurfaceVariant,
    onSurfaceVariant = NotesnookGray700,
    
    surfaceTint = NotesnookPurple,
    surfaceBright = Color.White,
    surfaceContainer = NotesnookGray50,
    surfaceContainerHigh = NotesnookGray100,
    surfaceContainerHighest = NotesnookGray200,
    surfaceContainerLow = NotesnookSurface,
    surfaceContainerLowest = Color.White,
    surfaceDim = NotesnookGray100,
    
    outline = NotesnookOutline,
    outlineVariant = NotesnookGray200,
    
    inverseSurface = NotesnookGray800,
    inverseOnSurface = NotesnookGray100,
    inversePrimary = NotesnookPurpleLight,
    
    scrim = Color.Black.copy(alpha = 0.5f)
)

// Notesnook Dark Theme
val NotesnookDarkColorScheme = darkColorScheme(
    primary = NotesnookPurpleLight,
    onPrimary = NotesnookGray900,
    primaryContainer = NotesnookPurpleDark,
    onPrimaryContainer = NotesnookPurpleLight,
    
    secondary = NotesnookGray400,
    onSecondary = NotesnookGray900,
    secondaryContainer = NotesnookGray700,
    onSecondaryContainer = NotesnookGray200,
    
    tertiary = NotesnookPurpleLight,
    onTertiary = NotesnookGray900,
    tertiaryContainer = NotesnookPurpleVariant,
    onTertiaryContainer = NotesnookPurpleLight,
    
    error = Color(0xFFF87171),
    onError = NotesnookGray900,
    errorContainer = Color(0xFFDC2626),
    onErrorContainer = Color(0xFFFECACA),
    
    background = NotesnookDarkBackground,
    onBackground = NotesnookGray100,
    
    surface = NotesnookDarkSurface,
    onSurface = NotesnookGray100,
    surfaceVariant = NotesnookDarkSurfaceVariant,
    onSurfaceVariant = NotesnookGray300,
    
    surfaceTint = NotesnookPurpleLight,
    surfaceBright = NotesnookGray700,
    surfaceContainer = NotesnookGray800,
    surfaceContainerHigh = NotesnookGray700,
    surfaceContainerHighest = NotesnookGray600,
    surfaceContainerLow = NotesnookDarkSurface,
    surfaceContainerLowest = Color.Black,
    surfaceDim = NotesnookDarkBackground,
    
    outline = NotesnookDarkOutline,
    outlineVariant = NotesnookGray700,
    
    inverseSurface = NotesnookGray100,
    inverseOnSurface = NotesnookGray800,
    inversePrimary = NotesnookPurple,
    
    scrim = Color.Black.copy(alpha = 0.7f)
)