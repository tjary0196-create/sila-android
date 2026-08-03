package com.sila.messaging.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sila.messaging.ui.theme.SilaSpacing

/**
 * A single chat message bubble, Telegram-style: large rounded corners on
 * three sides, with the bubble's own outer corner (bottom-end for a sent
 * bubble, bottom-start for a received one) flattened only on the *last*
 * bubble of a consecutive run from the same sender — this is what visually
 * groups a burst of messages together instead of showing disconnected pills.
 *
 * The timestamp sits inside the bubble, aligned under the text on the same
 * trailing edge — the common layout used by most chat apps for short,
 * single-line messages.
 *
 * @param isLastInGroup whether this is the last message in a run from the
 *   same sender — controls the flattened "tail" corner. Callers building a
 *   conversation list should pass `true` for isolated messages and only
 *   `false` for a bubble immediately followed by another one from the same
 *   sender.
 */
@Composable
fun SilaMessageBubble(
    text: String,
    time: String,
    isMe: Boolean,
    modifier: Modifier = Modifier,
    isLastInGroup: Boolean = true
) {
    val bigCorner = 18.dp
    val tailCorner = 4.dp

    val shape: Shape = RoundedCornerShape(
        topStart = bigCorner,
        topEnd = bigCorner,
        bottomStart = if (!isMe && isLastInGroup) tailCorner else bigCorner,
        bottomEnd = if (isMe && isLastInGroup) tailCorner else bigCorner
    )

    val bubbleColor = if (isMe) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (isMe) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .widthIn(max = 280.dp)
            .background(color = bubbleColor, shape = shape)
            .padding(horizontal = SilaSpacing.sm, vertical = SilaSpacing.xs),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Text(text = text, color = textColor, fontSize = 15.sp)
        Text(
            text = time,
            color = textColor.copy(alpha = 0.6f),
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
