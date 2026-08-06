package com.sila.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sila.model.Chat
import com.sila.model.User
import com.sila.ui.components.*
import com.sila.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    chats: List<Chat>,
    onChatClick: (Chat) -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNewChatClick: () -> Unit,
    onCallsClick: () -> Unit,
    onPeopleClick: () -> Unit,
    onRequestsClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val filters = listOf("All", "Unread", "Groups", "Requests")

    val stories = listOf(
        User("0", "Your note", "", isOnline = true),
        User("1", "Ali", "", isOnline = true, hasStory = true),
        User("2", "Lina", "", isOnline = true, hasStory = true),
        User("3", "Omar", "", isOnline = false, hasStory = true),
        User("4", "Sara", "", isOnline = true, hasStory = true),
        User("5", "Kareem", "", isOnline = false, hasStory = true)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chats",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                actions = {
                    IconButton(onClick = onNewChatClick) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "New Chat",
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundPrimary
                )
            )
        },
        bottomBar = {
            SilaBottomNav(
                selectedTab = selectedTab,
                onTabSelected = { index ->
                    selectedTab = index
                    when (index) {
                        1 -> onCallsClick()
                        2 -> onPeopleClick()
                        3 -> onProfileClick()
                    }
                },
                unreadChats = 3
            )
        },
        containerColor = BackgroundPrimary
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SilaSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search"
                )
            }

            // Stories Row
            LazyRow(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(stories) { user ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(64.dp)
                    ) {
                        SilaAvatar(
                            imageUrl = user.avatarUrl,
                            isOnline = user.isOnline,
                            hasStory = user.hasStory,
                            size = 58
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.name,
                            fontSize = 11.sp,
                            color = if (user.name == "Your note") TextMuted else TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = filter == selectedFilter
                    val hasBadge = filter == "Requests"

                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.95f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "filter_scale"
                    )

                    Box(
                        modifier = Modifier
                            .scale(scale)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) AccentBlue else SurfacePrimary)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = {
                                    selectedFilter = filter
                                    if (filter == "Requests") onRequestsClick()
                                }
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = filter,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                            if (hasBadge) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(AccentBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "3",
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Chats List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(chats, key = { it.id }) { chat ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
                        exit = fadeOut()
                    ) {
                        ChatListItem(
                            chat = chat,
                            onClick = { onChatClick(chat) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatListItem(
    chat: Chat,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chat_item_scale"
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
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SilaAvatar(
            imageUrl = chat.user.avatarUrl,
            isOnline = chat.user.isOnline,
            size = 54
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chat.user.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = chat.timestamp,
                    fontSize = 12.sp,
                    color = if (chat.unreadCount > 0) AccentBlue else TextMuted,
                    fontWeight = if (chat.unreadCount > 0) FontWeight.Medium else FontWeight.Normal
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (chat.isTyping) {
                    TypingIndicator()
                } else if (chat.isVoiceMessage) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = chat.voiceDuration,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                } else {
                    Text(
                        text = chat.lastMessage,
                        fontSize = 14.sp,
                        color = if (chat.unreadCount > 0) TextSecondary else TextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (chat.unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(AccentBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
