package com.pemmob.gamedex.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    primaryContainer = BlueContainer40,
    onPrimaryContainer = OnBlueContainer40,
    background = SoftBlueBackground,
    surface = LightSurface,
    surfaceVariant = SoftBlueSearch,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    outlineVariant = LightOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    primaryContainer = BlueContainer80,
    onPrimaryContainer = OnBlueContainer80,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSearch,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outlineVariant = DarkOutline
)

private val GameDexShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun GameDexTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = GameDexShapes,
        content = content
    )
}
