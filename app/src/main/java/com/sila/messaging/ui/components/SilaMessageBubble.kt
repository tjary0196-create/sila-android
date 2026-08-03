package com.sila.messaging.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sila.messaging.ui.theme.SilaSpacing

/**
 * A single chat message bubble, Telegram-style. Sent bubbles carry a subtle
 * brand gradient instead of a flat fill, and every new bubble "pops" in with
 * a spring (scale + fade, anchored to its own trailing/leading corner) —
 * giving the conversation the same lively, springy feel as the rest of Sila
 * instead of messages simply appearing.
 *
 * @param isLastInGroup whether this is the last message in a run from the
 *   same sender — controls the flattened "tail" corner.
 * @param animateIn whether to play the pop-in entrance (defaults to true;
 *   set false when re-rendering already-seen history to avoid replaying it).
 */
@Composable
fun SilaMessageBubble(
    text: String,
    time: String,
    isMe: Boolean,
    modifier: Modifier = Modifier,
    isLastInGroup: Boolean = true,
    animateIn: Boolean = true
) {
    val bigCorner = 18.dp
    val tailCorner = 4.dp

    val shape: Shape = RoundedCornerShape(
        topStart = bigCorner,
        topEnd = bigCorner,
        bottomStart = if (!isMe && isLastInGroup) tailCorner else bigCorner,
        bottomEnd = if (isMe && isLastInGroup) tailCorner else bigCorner
    )

    val progress = remember { Animatable(if (animateIn) 0.6f else 1f) }
    LaunchedEffect(Unit) {
        if (animateIn) {
            progress.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    val backgroundModifier = if (isMe) {
        Modifier.background(
            brush = Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f)
                )
            ),
            shape = shape
        )
    } else {
        Modifier.background(color = MaterialTheme.colorScheme.surfaceVariant, shape = shape)
    }

    val textColor = if (isMe) {
        androidx.compose.ui.graphics.Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .widthIn(max = 280.dp)
            .graphicsLayer {
                scaleX = progress.value
                scaleY = progress.value
                alpha = ((progress.value - 0.6f) / 0.4f).coerceIn(0f, 1f)
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(if (isMe) 1f else 0f, 1f)
            }
            .then(backgroundModifier)
            .padding(horizontal = SilaSpacing.sm, vertical = SilaSpacing.xs),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Text(text = text, color = textColor, fontSize = 15.sp)
        Text(
            text = time,
            color = textColor.copy(alpha = 0.7f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/**
 * A small centered pill used to separate messages from different days
 * (e.g. "اليوم", "أمس", or a full date) inside the chat's LazyColumn.
 */
@Composable
fun SilaDateSeparator(label: String, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                RoundedCornerShape(SilaSpacing.sm)
            )
            .padding(horizontal = SilaSpacing.sm, vertical = 4.dp)
    ) {
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
