package com.sila.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sila.di.ServiceLocator
import com.sila.messaging.core.result.AppResult
import com.sila.messaging.domain.chat.ChatRepository
import com.sila.messaging.domain.user.UserProfile
import com.sila.messaging.domain.user.UserRepository
import com.sila.model.Chat
import com.sila.ui.mapper.toUiChat
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatsViewModel(
    private val myUid: String,
    private val chatRepository: ChatRepository = ServiceLocator.chatRepository,
    private val userRepository: UserRepository = ServiceLocator.userRepository
) : ViewModel() {

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Small in-memory cache so we don't re-fetch the same peer's profile on every chat-list update.
    private val profileCache = mutableMapOf<String, UserProfile?>()

    init {
        viewModelScope.launch {
            chatRepository.listenMyChats(myUid).collect { summaries ->
                _isLoading.value = false
                _chats.value = coroutineScope {
                    summaries.map { summary ->
                        async {
                            val peerUid = summary.participants.firstOrNull { it != myUid }
                            val profile = peerUid?.let { uid ->
                                profileCache.getOrPut(uid) {
                                    when (val result = userRepository.getPublicProfile(uid)) {
                                        is AppResult.Success -> result.data
                                        is AppResult.Error -> null
                                    }
                                }
                            }
                            summary.toUiChat(myUid, profile)
                        }
                    }.map { it.await() }
                }
            }
        }
    }
}
