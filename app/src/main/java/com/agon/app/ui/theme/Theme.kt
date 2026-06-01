package com.agon.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = IndigoPrimaryDark,
    secondary = TealSecondaryDark,
    tertiary = AmberAccentDark,
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimaryLight,
    secondary = TealSecondaryLight,
    tertiary = AmberAccentLight,
    background = LightBg,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
)

@Composable
fun AgonAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
