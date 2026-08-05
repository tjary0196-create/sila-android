package com.sila.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sila.model.MessageRequest
import com.sila.model.User
import com.sila.ui.components.SilaAvatar
import com.sila.ui.components.SilaTopBar
import com.sila.ui.theme.*

@Composable
fun MessageRequestsScreen(
    onBackClick: () -> Unit,
    onAcceptClick: (MessageRequest) -> Unit,
    onDeleteClick: (MessageRequest) -> Unit,
    onRequestClick: (MessageRequest) -> Unit
) {
    val requests = listOf(
        MessageRequest(
            "1",
            User("1", "Moataz Ashraf", "@moataz_ashraf"),
            "مرحباً، رأيت تعليقك وأريد التواصل معك...",
            "Just now"
        ),
        MessageRequest(
            "2",
            User("2", "Yousef Mohsen", "@yousef_mohsen"),
            "السلام عليكم، ممكن نتعرف؟",
            "2m"
        ),
        MessageRequest(
            "3",
            User("3", "Rahma Ahmed", "@rahma_ahmed"),
            "مرحباً!",
            "10m"
        ),
        MessageRequest(
            "4",
            User("4", "Kareem Emad", "@kareem_emad"),
            "عندي مشروعك على التطبيق...",
            "1h"
        )
    )

    Scaffold(
        topBar = {
            SilaTopBar(
                title = "Message Requests",
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = TextPrimary
                        )
                    }
                }
            )
        },
        containerColor = BackgroundPrimary
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Text(
                    text = "Requests aren't marked as seen until you accept.",
                    fontSize = 13.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(16.dp)
                )
            }

            items(requests, key = { it.id }) { request ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
                ) {
                    MessageRequestItem(
                        request = request,
                        onAccept = { onAcceptClick(request) },
                        onDelete = { onDeleteClick(request) },
                        onClick = { onRequestClick(request) }
                    )
                }
            }
        }
    }
}

@Composable
fun MessageRequestItem(
    request: MessageRequest,
    onAccept: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SilaAvatar(
                imageUrl = request.user.avatarUrl,
                size = 48
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = request.user.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = request.timeAgo,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
                Text(
                    text = request.user.handle,
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = request.previewText,
            fontSize = 14.sp,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 60.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val acceptInteraction = remember { MutableInteractionSource() }
            val acceptPressed by acceptInteraction.collectIsPressedAsState()
            val acceptScale by animateFloatAsState(
                targetValue = if (acceptPressed) 0.95f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "accept_scale"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .scale(acceptScale)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AccentBlue)
                    .clickable(
                        interactionSource = acceptInteraction,
                        indication = null,
                        onClick = onAccept
                    )
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Accept",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            val deleteInteraction = remember { MutableInteractionSource() }
            val deletePressed by deleteInteraction.collectIsPressedAsState()
            val deleteScale by animateFloatAsState(
                targetValue = if (deletePressed) 0.95f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "delete_scale"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .scale(deleteScale)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceSecondary)
                    .clickable(
                        interactionSource = deleteInteraction,
                        indication = null,
                        onClick = onDelete
                    )
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Delete",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }
    }
}
