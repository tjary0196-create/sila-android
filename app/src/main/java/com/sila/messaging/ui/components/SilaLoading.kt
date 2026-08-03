package com.sila.messaging.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sila.messaging.ui.theme.SilaGradients

/**
 * Full-area loading indicator with an optional message. Uses a custom
 * rotating gradient-sweep ring (brand colors) instead of the stock Material
 * spinner, so even a plain loading state carries Sila's identity.
 */
@Composable
fun SilaLoading(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SilaSpinner()
        if (message != null) {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(14.dp))
            Text(message, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Standalone rotating gradient-arc spinner, reusable inline (e.g. inside buttons/rows). */
@Composable
fun SilaSpinner(modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 40.dp) {
    val transition = rememberInfiniteTransition(label = "silaSpinner")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(950, easing = LinearEasing)),
        label = "spinnerRotation"
    )
    val brush = Brush.sweepGradient(SilaGradients.Glow + SilaGradients.Glow.first())

    Canvas(modifier = modifier.size(size)) {
        rotate(rotation) {
            drawArc(
                brush = brush,
                startAngle = 0f,
                sweepAngle = 300f,
                useCenter = false,
                style = Stroke(width = size.toPx() * 0.11f, cap = StrokeCap.Round)
            )
        }
    }
}
