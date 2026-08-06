package com.sila.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sila.model.User
import com.sila.ui.components.SilaAvatar
import com.sila.ui.components.SilaDivider
import com.sila.ui.components.SilaTopBar
import com.sila.ui.theme.*

@Composable
fun ChatInfoScreen(
    user: User,
    onBackClick: () -> Unit,
    onAudioCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSharedMediaClick: () -> Unit,
    onFilesClick: () -> Unit,
    onPinnedMessagesClick: () -> Unit,
    onClearChatClick: () -> Unit,
    onBlockUserClick: () -> Unit,
    onDeleteChatClick: () -> Unit
) {
    var isMuted by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SilaTopBar(
                title = "Chat Info",
                onBackClick = onBackClick
            )
        },
        containerColor = BackgroundPrimary
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // User Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SilaAvatar(
                        imageUrl = user.avatarUrl,
                        isOnline = user.isOnline,
                        size = 84
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = user.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = user.handle,
                        fontSize = 14.sp,
                        color = TextMuted
                    )
                    if (user.isOnline) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Online",
                            fontSize = 13.sp,
                            color = StatusOnline,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Quick Actions
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickActionItem(Icons.Outlined.Phone, "Audio", onAudioCallClick)
                    QuickActionItem(Icons.Outlined.Videocam, "Video", onVideoCallClick)
                    QuickActionItem(Icons.Outlined.Search, "Search", onSearchClick)
                    QuickActionItem(Icons.Outlined.MoreVert, "More", {})
                }
            }

            item { SilaDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

            // Info Items
            item {
                InfoSection(
                    items = listOf(
                        InfoItemData(Icons.Outlined.Image, "Shared Media", "142", onSharedMediaClick),
                        InfoItemData(Icons.Outlined.InsertDriveFile, "Files", "12", onFilesClick),
                        InfoItemData(Icons.Outlined.PushPin, "Pinned Messages", "5", onPinnedMessagesClick)
                    )
                )
            }

            item { SilaDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

            // Mute Toggle
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isMuted = !isMuted }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsOff,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Mute Notifications",
                        fontSize = 15.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isMuted,
                        onCheckedChange = { isMuted = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentBlue,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = SurfaceSecondary,
                            uncheckedBorderColor = BorderColor
                        )
                    )
                }
            }

            item { SilaDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

            // Danger Actions
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    DangerActionItem(
                        icon = Icons.Outlined.Delete,
                        text = "Clear Chat",
                        onClick = onClearChatClick,
                        color = ErrorRed
                    )
                    DangerActionItem(
                        icon = Icons.Outlined.Block,
                        text = "Block User",
                        onClick = onBlockUserClick,
                        color = ErrorRed
                    )
                    DangerActionItem(
                        icon = Icons.Outlined.DeleteForever,
                        text = "Delete Chat",
                        onClick = onDeleteChatClick,
                        color = ErrorRed
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "quick_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(SurfacePrimary)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = AccentBlue,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

data class InfoItemData(
    val icon: ImageVector,
    val title: String,
    val count: String,
    val onClick: () -> Unit
)

@Composable
fun InfoSection(items: List<InfoItemData>) {
    Column {
        items.forEach { item ->
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.98f else 1f,
                label = "info_scale"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = item.onClick
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = item.count,
                    fontSize = 14.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun DangerActionItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    color: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "danger_scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 15.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}
