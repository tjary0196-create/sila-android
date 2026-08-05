package com.sila.messaging.domain.chat

import com.sila.messaging.core.result.AppResult
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun getOrCreateDirectChat(u1: String, u2: String): AppResult<String>
    suspend fun sendText(chatId: String, senderId: String, text: String): AppResult<Unit>
    fun listenMessages(chatId: String): Flow<List<ChatMessage>>
    fun listenMyChats(uid: String): Flow<List<ChatSummary>>
}
