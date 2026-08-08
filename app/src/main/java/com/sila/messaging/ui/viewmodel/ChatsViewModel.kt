package com.sila.messaging.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sila.messaging.data.model.Chat
import com.sila.messaging.data.model.MessageRequest
import com.sila.messaging.data.repository.AuthRepository
import com.sila.messaging.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUid: String? get() = authRepository.currentUser?.uid

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats

    private val _messageRequests = MutableStateFlow<List<MessageRequest>>(emptyList())
    val messageRequests: StateFlow<List<MessageRequest>> = _messageRequests

    private val _requestCount = MutableStateFlow(0)
    val requestCount: StateFlow<Int> = _requestCount

    init {
        viewModelScope.launch {
            chatRepository.getChats().collect { _chats.value = it }
        }
        viewModelScope.launch {
            chatRepository.getMessageRequests().collect { 
                _messageRequests.value = it
                _requestCount.value = it.size
            }
        }
    }

    fun acceptRequest(requestId: String, onChatReady: (String) -> Unit = {}) {
        viewModelScope.launch {
            val result = chatRepository.acceptMessageRequest(requestId)
            result.getOrNull()?.let { chatId -> onChatReady(chatId) }
        }
    }

    fun declineRequest(requestId: String) {
        viewModelScope.launch { chatRepository.declineMessageRequest(requestId) }
    }
}
