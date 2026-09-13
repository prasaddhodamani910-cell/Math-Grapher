package com.prasad.mathgrapher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Custom color scheme holder for non-Material colors (gridLine, curve colors)
data class MathGrapherColors(
    val gridLine: Color,
    val axisLine: Color,
    val surfaceElevated: Color,
    val curveColors: List<Color>,
    val onSurfaceMuted: Color,
)

val LocalMathGrapherColors = staticCompositionLocalOf {
    MathGrapherColors(
        gridLine = LightGridLine,
        axisLine = LightAxisLine,
        surfaceElevated = LightSurfaceElevated,
        curveColors = CurveColors,
        onSurfaceMuted = LightOnSurfaceMuted
    )
}

@Composable
fun MathGrapherTheme(
    darkTheme: Boolean = true, // Default to dark theme as requested by guide
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            background = DarkBackground,
            surface = DarkSurface,
            primary = DarkPrimary,
            onBackground = DarkOnSurface,
            onSurface = DarkOnSurface,
        )
    } else {
        lightColorScheme(
            background = LightBackground,
            surface = LightSurface,
            primary = LightPrimary,
            onBackground = LightOnSurface,
            onSurface = LightOnSurface,
        )
    }

    val mathColors = if (darkTheme) {
        MathGrapherColors(DarkGridLine, DarkAxisLine, DarkSurfaceElevated, CurveColors, DarkOnSurfaceMuted)
    } else {
        MathGrapherColors(LightGridLine, LightAxisLine, LightSurfaceElevated, CurveColors, LightOnSurfaceMuted)
    }

    CompositionLocalProvider(LocalMathGrapherColors provides mathColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MathGrapherTypography,
            shapes = MathGrapherShapes,
            content = content
        )
    }
}
