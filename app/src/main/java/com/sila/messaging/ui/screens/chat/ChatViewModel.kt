package com.sila.messaging.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sila.messaging.domain.chat.ChatMessage
import com.sila.messaging.domain.chat.ChatRepository
import com.sila.messaging.domain.user.UserProfile
import com.sila.messaging.domain.user.UserRepository
import com.sila.messaging.core.result.AppResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatUi(
    val otherUser: UserProfile? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val chatId: String? = null
)

class ChatViewModel(
    private val chatRepo: ChatRepository,
    private val userRepo: UserRepository,
    private val currentUid: String,
    private val otherUid: String
) : ViewModel() {

    private val _ui = MutableStateFlow(ChatUi())
    val ui: StateFlow<ChatUi> = _ui

    init {
        loadOtherUser()
        initChat()
    }

    private fun loadOtherUser() {
        viewModelScope.launch {
            val result = userRepo.getPublicProfile(otherUid)
            if (result is AppResult.Success) {
                _ui.update { it.copy(otherUser = result.data) }
            }
        }
    }

    private fun initChat() {
        _ui.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = chatRepo.getOrCreateDirectChat(currentUid, otherUid)
            if (result is AppResult.Success) {
                val chatId = result.data
                _ui.update { it.copy(chatId = chatId) }
                observeMessages(chatId)
            } else if (result is AppResult.Error) {
                _ui.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    private fun observeMessages(chatId: String) {
        viewModelScope.launch {
            chatRepo.listenMessages(chatId)
                .catch { e ->
                    _ui.update { it.copy(error = e.localizedMessage) }
                }
                .collectLatest { messages ->
                    _ui.update { it.copy(messages = messages, isLoading = false) }
                }
        }
    }

    fun sendMessage(text: String) {
        val chatId = _ui.value.chatId ?: return
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        // Matches the 4000-char cap enforced server-side in firestore.rules — checked here too
        // so the sender gets an immediate error instead of a silent server-side rejection.
        if (trimmed.length > 4000) {
            _ui.update { it.copy(error = "الرسالة طويلة جداً (الحد الأقصى 4000 حرف)") }
            return
        }

        viewModelScope.launch {
            val result = chatRepo.sendText(chatId, currentUid, trimmed)
            if (result is AppResult.Error) {
                _ui.update { it.copy(error = result.message) }
            }
        }
    }
}
