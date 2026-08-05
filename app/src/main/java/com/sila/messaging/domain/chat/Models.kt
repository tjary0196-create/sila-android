package com.sila.messaging.domain.chat

data class ChatSummary(
    val chatId: String,
    val participants: List<String>,
    val lastMessage: String?,
    val updatedAtMillis: Long?
)

data class ChatMessage(
    val id: String,
    val senderId: String,
    val text: String,
    val createdAtMillis: Long?
)
