package com.sila.messaging.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlin.math.abs

/**
 * Curated gradient pairs used to give each user a consistent, premium-looking
 * avatar color derived from their name/uid, similar to Telegram/Discord.
 */
private val avatarGradients = listOf(
    Brush.linearGradient(listOf(Color(0xFF6A5AE0), Color(0xFF7C6BFF))),
    Brush.linearGradient(listOf(Color(0xFF00A884), Color(0xFF00C2A8))),
    Brush.linearGradient(listOf(Color(0xFFFF7A59), Color(0xFFFFA26B))),
    Brush.linearGradient(listOf(Color(0xFF2E9EFF), Color(0xFF5CC6FF))),
    Brush.linearGradient(listOf(Color(0xFFE85D9C), Color(0xFFFF8FBF))),
    Brush.linearGradient(listOf(Color(0xFF34B27B), Color(0xFF6FE3AE))),
    Brush.linearGradient(listOf(Color(0xFFDB7EFF), Color(0xFFB16CFF))),
    Brush.linearGradient(listOf(Color(0xFFFFA83E), Color(0xFFFFD166))),
)

private fun gradientFor(seed: String): Brush {
    if (seed.isBlank()) return avatarGradients[0]
    val index = abs(seed.hashCode()) % avatarGradients.size
    return avatarGradients[index]
}

/**
 * Reusable circular avatar: shows [photoUrl] when available, otherwise falls back
 * to a colored gradient with the first letter of [name]. Optionally shows an
 * online presence dot and/or an uploading spinner overlay.
 */
@Composable
fun SilaAvatar(
    name: String,
    photoUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    seed: String = name,
    isOnline: Boolean = false,
    isLoading: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Box(modifier = modifier, contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(gradientFor(seed.ifBlank { name }))
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            if (!photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = name.trim().take(1).uppercase().ifBlank { "؟" },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.36f).sp
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(size * 0.35f)
                    )
                }
            }
        }

        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF34C759))
            )
        }
    }
}
