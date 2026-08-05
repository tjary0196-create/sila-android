package com.sila.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary           = AccentBlue,
    onPrimary         = TextPrimary,
    primaryContainer  = AccentBlueDark,
    onPrimaryContainer= TextPrimary,
    secondary         = AccentBlueLight,
    onSecondary       = TextPrimary,
    background        = BackgroundPrimary,
    onBackground      = TextPrimary,
    surface           = SurfacePrimary,
    onSurface         = TextPrimary,
    surfaceVariant    = SurfaceSecondary,
    onSurfaceVariant  = TextSecondary,
    outline           = BorderColor,
    error             = ErrorRed,
    onError           = TextPrimary,
    scrim             = BackgroundPrimary.copy(alpha = 0.8f)
)

@Composable
fun SilaTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundPrimary.toArgb()
            window.navigationBarColor = SurfacePrimary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = SilaTypography,
        content     = content
    )
}
