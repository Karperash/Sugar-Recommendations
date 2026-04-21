package com.gk.diaguide.ui.theme

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
    primary = PrimaryLight,
    onPrimary = Color(0xFF0D1B26),
    secondary = SecondaryLight,
    onSecondary = Color(0xFF0D1B26),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE3E8EC),
    surface = Color(0xFF1C252E),
    onSurface = Color(0xFFE3E8EC),
    surfaceVariant = Color(0xFF2A3540),
    onSurfaceVariant = Color(0xFFB8C5CE),
    error = Error,
    onError = Color.White,
)

@Composable
fun DiaGuideTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val context = LocalContext.current
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> if (darkTheme) DarkScheme else LightScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
