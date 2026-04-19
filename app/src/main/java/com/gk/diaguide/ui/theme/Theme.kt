package com.gk.diaguide.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    secondary = Secondary,
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    error = Error,
)

private val DarkScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    error = Error,
)

@Composable
fun DiaGuideTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightScheme,
        typography = Typography,
        content = content,
    )
}
