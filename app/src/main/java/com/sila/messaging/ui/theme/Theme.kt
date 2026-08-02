package com.sila.messaging.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SilaLightColors = lightColorScheme(
    primary = SilaPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = SilaBubbleSentLight,
    onPrimaryContainer = SilaTextPrimaryLight,

    secondary = SilaSecondaryLight,
    onSecondary = Color.White,

    tertiary = SilaAccentLight,
    onTertiary = Color.White,

    error = SilaError,
    onError = Color.White,

    background = SilaBackgroundLight,
    onBackground = SilaTextPrimaryLight,

    surface = SilaSurfaceLight,
    onSurface = SilaTextPrimaryLight,

    surfaceVariant = SilaSurfaceVariantLight,
    onSurfaceVariant = SilaTextSecondaryLight,

    outline = SilaDividerLight,
    outlineVariant = SilaDividerLight
)

private val SilaDarkColors = darkColorScheme(
    primary = SilaPrimaryDark,
    onPrimary = Color(0xFF0D0E12),
    primaryContainer = SilaBubbleSentDark,
    onPrimaryContainer = SilaTextPrimaryDark,

    secondary = SilaSecondaryDark,
    onSecondary = Color(0xFF0D0E12),

    tertiary = SilaAccentDark,
    onTertiary = Color(0xFF0D0E12),

    error = SilaErrorDark,
    onError = Color(0xFF0D0E12),

    background = SilaBackgroundDark,
    onBackground = SilaTextPrimaryDark,

    surface = SilaSurfaceDark,
    onSurface = SilaTextPrimaryDark,

    surfaceVariant = SilaSurfaceVariantDark,
    onSurfaceVariant = SilaTextSecondaryDark,

    outline = SilaDividerDark,
    outlineVariant = SilaDividerDark
)

/**
 * App-wide theme wrapper. Signature is intentionally unchanged
 * (`SilaTheme(darkTheme, content)`) so no caller (MainActivity) needs to
 * change — only the color/typography/shape values it resolves to are new.
 */
@Composable
fun SilaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) SilaDarkColors else SilaLightColors
    MaterialTheme(
        colorScheme = colors,
        typography = SilaTypography,
        shapes = SilaShapes,
        content = content
    )
}
