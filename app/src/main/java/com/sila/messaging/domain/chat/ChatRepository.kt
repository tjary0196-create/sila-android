package com.sila.messaging.domain.chat

import com.sila.messaging.core.result.AppResult
import kotlinx.coroutines.flow.Flow

data class ChatMessage(
    val id: String,
    val senderId: String,
    val text: String,
    val createdAtMillis: Long?
)

data class ChatSummary(
    val chatId: String,
    val participants: List<String>,
    val lastMessage: String?,
    val updatedAtMillis: Long?
)

interface ChatRepository {
    suspend fun getOrCreateDirectChat(u1: String, u2: String): AppResult<String>
    suspend fun sendText(chatId: String, senderId: String, text: String): AppResult<Unit>
    fun listenMessages(chatId: String): Flow<List<ChatMessage>>
    fun listenMyChats(uid: String): Flow<List<ChatSummary>>
}
