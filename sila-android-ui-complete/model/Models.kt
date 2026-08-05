package com.sila.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector

data class User(
    val id: String,
    val name: String,
    val handle: String,
    val avatarUrl: String? = null,
    val isOnline: Boolean = false,
    val lastSeen: String = "",
    val bio: String = "",
    val chats: Int = 0,
    val friends: Int = 0,
    val groups: Int = 0,
    val joinedDate: String = "",
    val hasStory: Boolean = false
)

data class Chat(
    val id: String,
    val user: User,
    val lastMessage: String = "",
    val timestamp: String = "",
    val unreadCount: Int = 0,
    val isTyping: Boolean = false,
    val isVoiceMessage: Boolean = false,
    val voiceDuration: String = "",
    val isGroup: Boolean = false
)

data class Message(
    val id: String,
    val text: String = "",
    val timestamp: String,
    val isFromMe: Boolean,
    val isVoice: Boolean = false,
    val voiceDuration: String = "",
    val isRead: Boolean = false,
    val reactions: List<String> = emptyList()
)

data class MessageRequest(
    val id: String,
    val user: User,
    val previewText: String,
    val timeAgo: String
)

data class MediaItem(
    val id: String,
    val thumbnailUrl: String? = null,
    val type: MediaType = MediaType.IMAGE
)

enum class MediaType { IMAGE, VIDEO, FILE }

data class CallLog(
    val id: String,
    val user: User,
    val type: CallType,
    val duration: String = "",
    val timestamp: String,
    val isMissed: Boolean = false
)

enum class CallType { INCOMING, OUTGOING, MISSED }

data class SettingItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String? = null,
    val hasToggle: Boolean = false,
    val isToggleOn: Boolean = false,
    val showArrow: Boolean = true,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit = {}
)
