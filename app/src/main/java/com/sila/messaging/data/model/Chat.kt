package com.sila.messaging.data.model

import com.google.firebase.Timestamp

data class Chat(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val participantPhotos: Map<String, String?> = emptyMap(),
    val lastMessage: LastMessage? = null,
    val unreadCount: Map<String, Int> = emptyMap(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isGroup: Boolean = false
)

data class LastMessage(
    val text: String = "",
    val senderId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val type: String = "text"
)
