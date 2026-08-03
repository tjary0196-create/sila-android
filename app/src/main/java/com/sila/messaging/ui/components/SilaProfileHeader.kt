package com.sila.messaging.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sila.messaging.ui.theme.SilaSpacing

/**
 * A single stat shown in [SilaProfileHeader] (e.g. "٢٤" over "محادثات").
 */
data class SilaProfileStat(val value: String, val label: String)

/**
 * Instagram-level profile hero: large avatar (with an optional verification
 * badge), display name, @username, bio, an optional row of extra badges
 * (Premium/Agent/Store), and a stats row. Purely presentational.
 */
@Composable
fun SilaProfileHeader(
    displayName: String,
    username: String,
    photoUrl: String?,
    modifier: Modifier = Modifier,
    bio: String = "",
    isVerified: Boolean = false,
    extraBadgeLabels: List<String> = emptyList(),
    stats: List<SilaProfileStat> = emptyList(),
    avatarSize: Dp = 110.dp,
    onAvatarClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SilaAvatar(
            name = displayName,
            photoUrl = photoUrl,
            seed = username.ifBlank { displayName },
            size = avatarSize,
            onClick = onAvatarClick
        )

        Spacer(modifier = Modifier.height(SilaSpacing.md))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                displayName.ifBlank { "بدون اسم" },
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            if (isVerified) {
                Spacer(modifier = Modifier.width(SilaSpacing.xxs))
                SilaVerificationBadge(size = 16.dp)
            }
        }

        if (username.isNotBlank()) {
            Text(
                "@$username",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (bio.isNotBlank()) {
            Spacer(modifier = Modifier.height(SilaSpacing.xs))
            Text(
                bio,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = SilaSpacing.xl)
            )
        }

        if (extraBadgeLabels.isNotEmpty()) {
            Spacer(modifier = Modifier.height(SilaSpacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(SilaSpacing.xs)) {
                extraBadgeLabels.forEach { label -> SilaBadge(text = label) }
            }
        }

        if (stats.isNotEmpty()) {
            Spacer(modifier = Modifier.height(SilaSpacing.lg))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = SilaSpacing.xxl),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                stats.forEach { stat ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stat.value, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(
                            stat.label,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
