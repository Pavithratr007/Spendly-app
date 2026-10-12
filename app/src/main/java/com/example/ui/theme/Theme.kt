package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = SpendlyPrimaryContainer,
  onPrimary = Color.White,
  primaryContainer = SpendlyPrimary,
  onPrimaryContainer = SpendlyOnPrimaryContainer,
  secondary = SpendlyEmerald,
  onSecondary = Color.White,
  secondaryContainer = SpendlySecondary,
  onSecondaryContainer = Color.White,
  tertiary = SpendlyCoral,
  onTertiary = Color.White,
  background = Color(0xFF0F172A),
  onBackground = Color(0xFFF8FAFC),
  surface = Color(0xFF1E293B),
  onSurface = Color(0xFFF8FAFC),
  surfaceVariant = Color(0xFF334155),
  onSurfaceVariant = Color(0xFFCBD5E1),
  outline = Color(0xFF64748B)
)

private val LightColorScheme = lightColorScheme(
  primary = SpendlyPrimary,
  onPrimary = Color.White,
  primaryContainer = SpendlyPrimaryContainer,
  onPrimaryContainer = SpendlyOnPrimaryContainer,
  secondary = SpendlyDeepEmerald,
  onSecondary = Color.White,
  secondaryContainer = SpendlySecondaryContainer,
  onSecondaryContainer = SpendlySecondary,
  tertiary = SpendlyDeepCoral,
  onTertiary = Color.White,
  background = SpendlySurface,
  onBackground = SpendlyOnSurface,
  surface = Color.White,
  onSurface = SpendlyOnSurface,
  surfaceVariant = SpendlySurfaceContainerLow,
  onSurfaceVariant = SpendlyOnSurfaceVariant,
  outline = SpendlyOutline,
  outlineVariant = SpendlyOutlineVariant
)

@Composable
fun MyApplicationTheme(
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
