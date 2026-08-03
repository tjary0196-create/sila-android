package com.sila.messaging.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sila.messaging.ui.theme.SilaAwayDot
import com.sila.messaging.ui.theme.SilaOnlineDot
import com.sila.messaging.ui.theme.SilaRadius
import com.sila.messaging.ui.theme.SilaSpacing

/**
 * Presence status chip (e.g. "متصل" / "بعيد" / "غير ظاهر"). Custom-built
 * instead of a stock Material FilterChip so it can carry a colored presence
 * dot and a softer, on-brand selected state.
 */
@Composable
fun SilaStatusChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    dotColor: Color? = null
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
        label = "statusChipContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "statusChipContent"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(SilaRadius.sm))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = SilaSpacing.sm, vertical = SilaSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (dotColor != null) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(label, color = contentColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

/** Convenience presets matching Sila's presence states. */
object SilaStatusDots {
    val Online = SilaOnlineDot
    val Away = SilaAwayDot
}
