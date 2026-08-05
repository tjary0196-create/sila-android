package com.sila.messaging.ui.screens.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sila.messaging.domain.chat.ChatRepository
import com.sila.messaging.domain.chat.ChatSummary
import com.sila.messaging.domain.user.UserProfile
import com.sila.messaging.domain.user.UserRepository
import com.sila.messaging.core.result.AppResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatUiState(
    val chatId: String,
    val otherUid: String,
    val displayName: String,
    val username: String,
    val photoUrl: String?,
    val lastMessage: String?,
    val updatedAtMillis: Long?,
    val isOnline: Boolean
)

data class ChatsUi(
    val isLoading: Boolean = false,
    val chats: List<ChatUiState> = emptyList(),
    val error: String? = null,
    val myProfile: UserProfile? = null
)

class ChatsViewModel(
    private val chatRepo: ChatRepository,
    private val userRepo: UserRepository,
    private val currentUid: String
) : ViewModel() {

    private val _ui = MutableStateFlow(ChatsUi())
    val ui: StateFlow<ChatsUi> = _ui

    private val profileCache = MutableStateFlow<Map<String, UserProfile>>(emptyMap())
    private var observeChatsJob: Job? = null

    init {
        loadMyProfile()
        observeChats()
    }

    /** Re-attempts loading chats after a failure (used by the error state's retry action). */
    fun retry() {
        observeChats()
    }

    private fun loadMyProfile() {
        viewModelScope.launch {
            val result = userRepo.getMyProfile(currentUid)
            if (result is AppResult.Success) {
                _ui.update { it.copy(myProfile = result.data) }
            }
        }
    }

    private fun observeChats() {
        observeChatsJob?.cancel()
        _ui.update { it.copy(isLoading = true, error = null) }

        observeChatsJob = viewModelScope.launch {
            chatRepo.listenMyChats(currentUid)
                .catch { e ->
                    _ui.update { it.copy(isLoading = false, error = e.localizedMessage) }
                }
                .collectLatest { summaries ->
                    updateUiWithChats(summaries)
                }
        }
    }

    private suspend fun updateUiWithChats(summaries: List<ChatSummary>) {
        val currentCache = profileCache.value.toMutableMap()
        
        val uiChats = summaries.map { summary ->
            val otherUid = summary.participants.firstOrNull { it != currentUid } ?: ""
            
            if (!currentCache.containsKey(otherUid) && otherUid.isNotEmpty()) {
                val profileResult = userRepo.getPublicProfile(otherUid)
                if (profileResult is AppResult.Success && profileResult.data != null) {
                    currentCache[otherUid] = profileResult.data
                }
            }
            
            val profile = currentCache[otherUid]
            ChatUiState(
                chatId = summary.chatId,
                otherUid = otherUid,
                displayName = profile?.displayName ?: profile?.username ?: "مستخدم",
                username = profile?.username ?: "",
                photoUrl = profile?.photoUrl,
                lastMessage = summary.lastMessage,
                updatedAtMillis = summary.updatedAtMillis,
                isOnline = profile?.status == "online" && profile.privacy.showLastSeen
            )
        }
        
        profileCache.value = currentCache
        _ui.update { it.copy(isLoading = false, chats = uiChats) }
    }
}
