package com.sila.messaging.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TelegramBlue = Color(0xFF0088CC)
val SentBubble = Color(0xFFE3F2FD)
val ReceivedBubble = Color(0xFFF1F1F1)

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

@Composable
fun SilaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = SilaLightColors, content = content)
}
