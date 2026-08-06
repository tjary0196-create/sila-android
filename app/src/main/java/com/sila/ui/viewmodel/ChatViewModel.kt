package com.sila.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sila.di.ServiceLocator
import com.sila.messaging.core.result.AppResult
import com.sila.messaging.domain.chat.ChatRepository
import com.sila.messaging.domain.user.UserProfile
import com.sila.messaging.domain.user.UserRepository
import com.sila.model.Message
import com.sila.ui.mapper.toUiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Backs a single conversation screen. [peerUid] is the other participant; the direct chat between
 * them and the current user is created on demand (or reused) the first time this loads.
 */
class ChatViewModel(
    private val myUid: String,
    private val peerUid: String,
    private val chatRepository: ChatRepository = ServiceLocator.chatRepository,
    private val userRepository: UserRepository = ServiceLocator.userRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _peerProfile = MutableStateFlow<UserProfile?>(null)
    val peerProfile: StateFlow<UserProfile?> = _peerProfile.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var chatId: String? = null

    init {
        viewModelScope.launch {
            when (val profileResult = userRepository.getPublicProfile(peerUid)) {
                is AppResult.Success -> _peerProfile.value = profileResult.data
                is AppResult.Error -> _errorMessage.value = profileResult.message
            }

            when (val chatResult = chatRepository.getOrCreateDirectChat(myUid, peerUid)) {
                is AppResult.Success -> {
                    chatId = chatResult.data
                    chatRepository.listenMessages(chatResult.data).collect { chatMessages ->
                        _messages.value = chatMessages.map { it.toUiMessage(myUid) }
                    }
                }
                is AppResult.Error -> _errorMessage.value = chatResult.message
            }
        }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val currentChatId = chatId ?: return
        viewModelScope.launch {
            when (val result = chatRepository.sendText(currentChatId, myUid, trimmed)) {
                is AppResult.Error -> _errorMessage.value = result.message
                is AppResult.Success -> Unit
            }
        }
    }
}
