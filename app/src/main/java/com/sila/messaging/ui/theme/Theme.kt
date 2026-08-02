package com.sila.messaging.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TelegramBlue = Color(0xFF0088CC)
val SentBubble = Color(0xFFE3F2FD)
val ReceivedBubble = Color(0xFFF1F1F1)

val SentBubbleDark = Color(0xFF1E3A5F)
val ReceivedBubbleDark = Color(0xFF2A2A2A)

private val SilaLightColors = lightColorScheme(
    primary = TelegramBlue,
    onPrimary = Color.White,
    primaryContainer = SentBubble,
    onPrimaryContainer = Color(0xFF1A1A1A),
    background = Color.White,
    onBackground = Color(0xFF1A1A1A),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = ReceivedBubble,
    onSurfaceVariant = Color(0xFF1A1A1A),
    outline = Color(0xFFE0E0E0)
)

private val SilaDarkColors = darkColorScheme(
    primary = TelegramBlue,
    onPrimary = Color.White,
    primaryContainer = SentBubbleDark,
    onPrimaryContainer = Color(0xFFECECEC),
    background = Color(0xFF121212),
    onBackground = Color(0xFFECECEC),
    surface = Color(0xFF1C1C1E),
    onSurface = Color(0xFFECECEC),
    surfaceVariant = ReceivedBubbleDark,
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF3A3A3C)
)

@Composable
fun SilaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) SilaDarkColors else SilaLightColors
    MaterialTheme(colorScheme = colors, content = content)
}
