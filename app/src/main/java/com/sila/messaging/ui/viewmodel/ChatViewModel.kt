package com.sila.messaging.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sila.messaging.data.model.Message
import com.sila.messaging.data.repository.AuthRepository
import com.sila.messaging.data.repository.ChatRepository
import com.sila.messaging.data.repository.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val storageRepository: StorageRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUid: String? get() = authRepository.currentUser?.uid

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending

    fun loadMessages(chatId: String) {
        viewModelScope.launch {
            chatRepository.getMessages(chatId).collect { _messages.value = it }
        }
    }

    fun sendMessage(chatId: String, text: String, replyTo: String? = null) {
        viewModelScope.launch {
            _isSending.value = true
            chatRepository.sendMessage(chatId, text, replyTo)
            _isSending.value = false
        }
    }

    fun sendImageMessage(chatId: String, uri: Uri) {
        viewModelScope.launch {
            _isSending.value = true
            when (val result = storageRepository.uploadChatImage(chatId, uri)) {
                is kotlin.Result.Success -> {
                    chatRepository.sendMediaMessage(chatId, result.getOrDefault(""), "image")
                }
                is kotlin.Result.Failure -> {
                    // TODO: Show error
                }
            }
            _isSending.value = false
        }
    }

    fun sendFileMessage(chatId: String, uri: Uri, fileName: String) {
        viewModelScope.launch {
            _isSending.value = true
            when (val result = storageRepository.uploadChatFile(chatId, uri, fileName)) {
                is kotlin.Result.Success -> {
                    val (url, name) = result.getOrDefault("" to "")
                    chatRepository.sendMediaMessage(chatId, url, "file", name)
                }
                is kotlin.Result.Failure -> {
                    // TODO: Show error
                }
            }
            _isSending.value = false
        }
    }

    fun deleteForMe(messageId: String) {
        viewModelScope.launch { chatRepository.deleteMessageForMe(messageId) }
    }

    fun deleteForEveryone(messageId: String) {
        viewModelScope.launch { chatRepository.deleteMessageForEveryone(messageId) }
    }

    fun addReaction(messageId: String, emoji: String) {
        viewModelScope.launch { chatRepository.addReaction(messageId, emoji) }
    }

    fun removeReaction(messageId: String) {
        viewModelScope.launch { chatRepository.removeReaction(messageId) }
    }
}
