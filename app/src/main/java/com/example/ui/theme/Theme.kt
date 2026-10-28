package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = OceanBlueLight,
    onPrimary = DeepAbyssNavy,
    primaryContainer = OceanBlueDark,
    onPrimaryContainer = LightSeafoam,
    secondary = SeafoamGreen,
    onSecondary = DeepAbyssNavy,
    secondaryContainer = TropicalTeal,
    onSecondaryContainer = Color.White,
    tertiary = GoldenSand,
    onTertiary = DeepAbyssNavy,
    background = DeepAbyssNavy,
    onBackground = TextLight,
    surface = OceanSurfaceDark,
    onSurface = TextLight,
    surfaceVariant = SurfaceCardDark,
    onSurfaceVariant = TextMutedLight,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = OceanBluePrimary,
    onPrimary = Color.White,
    primaryContainer = LightSeafoam,
    onPrimaryContainer = OceanBlueDark,
    secondary = TropicalTeal,
    onSecondary = Color.White,
    secondaryContainer = LightSand,
    onSecondaryContainer = WarmSandDark,
    tertiary = GoldenSand,
    onTertiary = Color.White,
    background = Color(0xFFF0F9FF),
    onBackground = TextDark,
    surface = Color.White,
    onSurface = TextDark,
    surfaceVariant = SurfaceCardLight,
    onSurfaceVariant = TextMuted,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // We prefer our vibrant ocean theme by default, but allow dynamic if wanted
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

