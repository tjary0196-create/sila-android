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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sila.model.User
import com.sila.ui.components.SilaAvatar
import com.sila.ui.components.SilaSearchBar
import com.sila.ui.components.SilaTopBar
import com.sila.ui.theme.*

@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onUserClick: (User) -> Unit,
    onMessageClick: (User) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val searchResults = listOf(
        User("1", "Ali Hassan", "@alihassan", isOnline = true),
        User("2", "Ali Mohsen", "@ali_mohsen", lastSeen = "Last seen 1h ago"),
        User("3", "Alia Ahmed", "@alia_ahmed", lastSeen = "Last seen 3h ago"),
        User("4", "Alina Saad", "@alina_saad", lastSeen = "Last seen yesterday"),
        User("5", "Alaa Mahmoud", "@alaa_mahmoud", lastSeen = "Last seen 2d ago")
    )

    Scaffold(
        topBar = {
            SilaTopBar(
                title = "Search",
                onBackClick = onBackClick
            )
        },
        containerColor = BackgroundPrimary
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                SilaSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Ali"
                )
            }

            LazyColumn {
                items(searchResults, key = { it.id }) { user ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
                    ) {
                        SearchResultItem(
                            user = user,
                            onClick = { onUserClick(user) },
                            onMessageClick = { onMessageClick(user) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    user: User,
    onClick: () -> Unit,
    onMessageClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "search_scale"
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
            imageUrl = user.avatarUrl,
            isOnline = user.isOnline,
            size = 48
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = user.handle,
                fontSize = 13.sp,
                color = TextMuted
            )
            if (user.lastSeen.isNotEmpty()) {
                Text(
                    text = user.lastSeen,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        val msgInteraction = remember { MutableInteractionSource() }
        val msgPressed by msgInteraction.collectIsPressedAsState()
        val msgScale by animateFloatAsState(
            targetValue = if (msgPressed) 0.9f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "msg_btn_scale"
        )

        Box(
            modifier = Modifier
                .scale(msgScale)
                .clip(RoundedCornerShape(10.dp))
                .background(AccentBlue)
                .clickable(
                    interactionSource = msgInteraction,
                    indication = null,
                    onClick = onMessageClick
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Message",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}
