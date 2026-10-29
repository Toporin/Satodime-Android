package org.satochip.satodimeapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val darkColorPalette = darkColors(
    primary = LightDarkBlue,
    primaryVariant = DarkBlue,
    secondary = Color.White,
    secondaryVariant = Color.LightGray,
    background = Dark
)

private val lightColorPalette = lightColors(
    primary = Color.LightGray,
    primaryVariant = PaleGray,
    secondary = LightDarkBlue,
    secondaryVariant = DarkBlue,
    background = PaleGray

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun SatodimeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) darkColorPalette else lightColorPalette

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}