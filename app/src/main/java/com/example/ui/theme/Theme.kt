package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = Color(0xFF38BDF8),
  onPrimary = Color(0xFF00354E),
  primaryContainer = Color(0xFF0369A1),
  onPrimaryContainer = Color(0xFFE0F2FE),
  secondary = Color(0xFF818CF8),
  onSecondary = Color(0xFF1E1B4B),
  background = Color(0xFF0F172A),
  surface = Color(0xFF0F172A),
  surfaceContainer = Color(0xFF1E293B),
  surfaceContainerHigh = Color(0xFF334155),
  surfaceContainerHighest = Color(0xFF1E293B),
  onBackground = Color(0xFFF8FAFC),
  onSurface = Color(0xFFF8FAFC),
  onSurfaceVariant = Color(0xFF94A3B8),
  outline = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
