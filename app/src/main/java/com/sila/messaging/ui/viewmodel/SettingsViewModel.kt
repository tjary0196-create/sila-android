package com.sila.messaging.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sila.messaging.data.model.BlockedUser
import com.sila.messaging.data.model.PublicProfileSettings
import com.sila.messaging.data.model.Session
import com.sila.messaging.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _privacySettings = MutableStateFlow<PublicProfileSettings?>(null)
    val privacySettings: StateFlow<PublicProfileSettings?> = _privacySettings

    private val _blockedUsers = MutableStateFlow<List<BlockedUser>>(emptyList())
    val blockedUsers: StateFlow<List<BlockedUser>> = _blockedUsers

    private val _activeSessions = MutableStateFlow<List<Session>>(emptyList())
    val activeSessions: StateFlow<List<Session>> = _activeSessions

    init {
        viewModelScope.launch {
            userRepository.getCurrentUserFlow().collect { user ->
                _privacySettings.value = user?.publicProfile
            }
        }
        viewModelScope.launch {
            userRepository.getBlockedUsers().collect { _blockedUsers.value = it }
        }
        viewModelScope.launch {
            userRepository.getActiveSessions().collect { _activeSessions.value = it }
        }
    }

    fun updateShowPhone(value: Boolean) {
        val current = _privacySettings.value ?: return
        _privacySettings.value = current.copy(showPhone = value)
        viewModelScope.launch { userRepository.updatePublicProfileSettings(current.copy(showPhone = value)) }
    }

    fun updateShowBirthDate(value: Boolean) {
        val current = _privacySettings.value ?: return
        _privacySettings.value = current.copy(showBirthDate = value)
        viewModelScope.launch { userRepository.updatePublicProfileSettings(current.copy(showBirthDate = value)) }
    }

    fun updateShowLastSeen(value: Boolean) {
        val current = _privacySettings.value ?: return
        _privacySettings.value = current.copy(showLastSeen = value)
        viewModelScope.launch { userRepository.updatePublicProfileSettings(current.copy(showLastSeen = value)) }
    }

    fun updateAllowAddToGroups(value: Boolean) {
        val current = _privacySettings.value ?: return
        _privacySettings.value = current.copy(allowAddToGroups = value)
        viewModelScope.launch { userRepository.updatePublicProfileSettings(current.copy(allowAddToGroups = value)) }
    }

    fun updateReadReceipts(value: Boolean) {
        val current = _privacySettings.value ?: return
        _privacySettings.value = current.copy(readReceipts = value)
        viewModelScope.launch { userRepository.updatePublicProfileSettings(current.copy(readReceipts = value)) }
    }

    fun unblockUser(blockId: String) {
        viewModelScope.launch { userRepository.unblockUser(blockId) }
    }

    fun terminateAllOtherSessions() {
        viewModelScope.launch { userRepository.terminateAllOtherSessions() }
    }
}
