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

private val DarkColorScheme = darkColorScheme(
    primary = SoothingDarkPrimary,
    secondary = SoothingDarkSecondary,
    tertiary = SoothingDarkAccent,
    background = SoothingDarkBg,
    surface = SoothingDarkSurface,
    onPrimary = SoothingDarkBg,
    onSecondary = Color.White,
    onBackground = SoothingDarkOnBackground,
    onSurface = SoothingDarkOnBackground,
    surfaceVariant = SoothingDarkSecondary,
    onSurfaceVariant = SoothingDarkOnBackground
)

private val LightColorScheme = lightColorScheme(
    primary = SoothingLightPrimary,
    secondary = SoothingLightSecondary,
    tertiary = SoothingLightAccent,
    background = SoothingLightBg,
    surface = SoothingLightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = SoothingLightOnBackground,
    onSurface = SoothingLightOnBackground,
    surfaceVariant = SoothingLightBg,
    onSurfaceVariant = SoothingLightOnBackground
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep dynamicColor false to preserve the intentional calming blue design
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
