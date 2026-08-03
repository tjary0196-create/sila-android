package com.sila.messaging.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Small pill-shaped label used for status tags (e.g. "Premium", "Agent",
 * "Store", "New") — the same primitive can back any future badge type
 * without a redesign, just a different [text]/[containerColor].
 */
@Composable
fun SilaBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier
            .background(containerColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = contentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * A custom-drawn, premium verification badge: a 12-point scalloped seal
 * (like Instagram/X's verified mark) with a checkmark stroke on top —
 * deliberately not a stock Material icon, so it reads as a distinct brand
 * element. Drawn directly with [Canvas] so it stays crisp at any size and
 * automatically follows the current theme's primary color.
 */
@Composable
fun SilaVerificationBadge(
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    seedColor: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val outerR = this.size.minDimension / 2f
        val innerR = outerR * 0.82f
        val points = 12

        val seal = Path()
        for (i in 0 until points * 2) {
            val angle = (Math.PI * i / points) - Math.PI / 2
            val r = if (i % 2 == 0) outerR else innerR
            val x = cx + r * cos(angle).toFloat()
            val y = cy + r * sin(angle).toFloat()
            if (i == 0) seal.moveTo(x, y) else seal.lineTo(x, y)
        }
        seal.close()

        drawPath(path = seal, color = seedColor)

        val check = Path().apply {
            moveTo(cx - outerR * 0.32f, cy + outerR * 0.02f)
            lineTo(cx - outerR * 0.06f, cy + outerR * 0.28f)
            lineTo(cx + outerR * 0.36f, cy - outerR * 0.24f)
        }
        drawPath(
            path = check,
            color = Color.White,
            style = Stroke(
                width = outerR * 0.22f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
