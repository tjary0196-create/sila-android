package com.sila.messaging.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Sila's shared motion language — a small set of physics-based specs and
 * modifiers reused across every screen/component so the whole app moves with
 * one consistent, "bouncy but controlled" personality instead of each screen
 * inventing its own timing curves.
 */

/** Punchy spring for entrances / press feedback (buttons, avatars, rows). */
val SilaBounceSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMediumLow
)

/** Softer spring for larger surfaces (cards, sheets, bubbles) that shouldn't overshoot much. */
val SilaSoftSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessLow
)

/** Snappy spring for tiny UI feedback (chips, badges, switches). */
val SilaSnapSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessHigh
)

/**
 * Adds a tactile "press to shrink slightly" effect driven by an interaction
 * source — used to make every tappable surface in Sila feel alive instead of
 * flat. Pair with `indication = null` on the clickable if you want a purely
 * scale-driven press effect without the default ripple.
 */
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.95f
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = SilaSnapSpring,
        label = "pressScale"
    )
    graphicsLayer { scaleX = scale; scaleY = scale }
}

/**
 * A slowly-drifting animated gradient brush — used behind primary buttons,
 * hero headers and the login screen to give Sila a premium "alive" surface
 * instead of a flat fill, without any extra draw cost beyond a color lerp.
 */
@Composable
fun rememberAnimatedGradient(colors: List<Color>): Brush {
    val transition = rememberInfiniteTransition(label = "silaGradient")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )
    return remember(colors, shift) {
        Brush.linearGradient(
            colors = colors,
            start = androidx.compose.ui.geometry.Offset(shift * 300f, 0f),
            end = androidx.compose.ui.geometry.Offset(300f + shift * 300f, 300f)
        )
    }
}

/** A gentle continuous pulse (0.9x–1.08x) used for online dots / verified badges / live indicators. */
@Composable
fun rememberPulseScale(minScale: Float = 0.92f, maxScale: Float = 1.1f, periodMs: Int = 1400): Float {
    val transition = rememberInfiniteTransition(label = "silaPulse")
    val scale by transition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(periodMs, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    return scale
}

/** A moving shimmer highlight position (0f..1f) used to sweep a light band across skeletons/spinners. */
@Composable
fun rememberShimmerProgress(periodMs: Int = 1100): Float {
    val transition = rememberInfiniteTransition(label = "silaShimmer")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(periodMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )
    return progress
}

/** Sila brand gradients, reused by buttons, avatars-of-honor, badges and the login hero. */
object SilaGradients {
    val PrimaryAccent = listOf(SilaPrimaryLight, SilaAccentLight)
    val PrimaryAccentDark = listOf(SilaPrimaryDark, SilaAccentDark)
    val Glow = listOf(Color(0xFF7C6BFF), Color(0xFF5CC6FF), Color(0xFF00C2A8))
}
