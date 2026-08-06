package com.sila.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sila.model.Message
import com.sila.model.User
import com.sila.ui.components.*
import com.sila.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    user: User,
    messages: List<Message>,
    onBackClick: () -> Unit,
    onCallClick: () -> Unit,
    onMoreClick: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SilaAvatar(
                            imageUrl = user.avatarUrl,
                            isOnline = user.isOnline,
                            size = 40
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = user.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (user.isOnline) "Online" else user.lastSeen,
                                fontSize = 12.sp,
                                color = if (user.isOnline) StatusOnline else TextMuted
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onCallClick) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            tint = TextPrimary
                        )
                    }
                    IconButton(onClick = onMoreClick) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundPrimary
                )
            )
        },
        containerColor = BackgroundPrimary
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Today",
                            fontSize = 12.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                items(messages, key = { it.id }) { message ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(
                            initialOffsetY = { if (message.isFromMe) it / 2 else -it / 2 }
                        )
                    ) {
                        ChatBubble(message = message)
                    }
                }
            }

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfacePrimary)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val addInteraction = remember { MutableInteractionSource() }
                val addPressed by addInteraction.collectIsPressedAsState()
                val addScale by animateFloatAsState(
                    targetValue = if (addPressed) 0.85f else 1f,
                    label = "add_scale"
                )

                IconButton(
                    onClick = { },
                    modifier = Modifier.scale(addScale)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = AccentBlue,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(BackgroundPrimary)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    BasicTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 14.sp),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(AccentBlue),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        decorationBox = { innerTextField ->
                            if (messageText.isEmpty()) {
                                Text(
                                    text = "Type a message...",
                                    fontSize = 14.sp,
                                    color = TextMuted
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                if (messageText.isNotBlank()) {
                    IconButton(
                        onClick = {
                            onSendMessage(messageText.trim())
                            messageText = ""
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = AccentBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                } else {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Outlined.EmojiEmotions,
                            contentDescription = "Emoji",
                            tint = TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice",
                            tint = TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message) {
    val isMe = message.isFromMe
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe) {
            SilaAvatar(
                imageUrl = null,
                size = 32,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
            if (message.isVoice) {
                VoiceMessageBubble(
                    duration = message.voiceDuration,
                    isFromMe = isMe
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomStart = if (isMe) 18.dp else 4.dp,
                                bottomEnd = if (isMe) 4.dp else 18.dp
                            )
                        )
                        .background(if (isMe) BubbleSent else BubbleReceived)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = message.text,
                        fontSize = 15.sp,
                        color = if (isMe) BubbleSentText else BubbleReceivedText,
                        lineHeight = 20.sp
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp, end = 4.dp)
            ) {
                Text(
                    text = message.timestamp,
                    fontSize = 11.sp,
                    color = TextMuted
                )
                if (isMe) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = "Read",
                        tint = if (message.isRead) AccentBlue else TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceMessageBubble(
    duration: String,
    isFromMe: Boolean
) {
    Row(
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isFromMe) 18.dp else 4.dp,
                    bottomEnd = if (isFromMe) 4.dp else 18.dp
                )
            )
            .background(if (isFromMe) BubbleSent else BubbleReceived)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val playInteraction = remember { MutableInteractionSource() }
        val playPressed by playInteraction.collectIsPressedAsState()
        val playScale by animateFloatAsState(
            targetValue = if (playPressed) 0.85f else 1f,
            label = "play_scale"
        )

        Box(
            modifier = Modifier
                .size(32.dp)
                .scale(playScale)
                .clip(CircleShape)
                .background(if (isFromMe) Color.White.copy(alpha = 0.2f) else SurfaceSecondary)
                .clickable(
                    interactionSource = playInteraction,
                    indication = null,
                    onClick = { }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = if (isFromMe) Color.White else TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        VoiceWaveform(
            isPlaying = false,
            modifier = Modifier.width(100.dp),
            barColor = if (isFromMe) Color.White.copy(alpha = 0.7f) else TextMuted
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = duration,
            fontSize = 12.sp,
            color = if (isFromMe) Color.White.copy(alpha = 0.8f) else TextSecondary
        )
    }
}
