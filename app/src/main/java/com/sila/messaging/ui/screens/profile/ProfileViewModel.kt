package com.sila.messaging.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sila.messaging.domain.user.UserProfile
import com.sila.messaging.domain.user.UserRepository
import com.sila.messaging.core.result.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUi(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false
)

class ProfileViewModel(
    private val userRepo: UserRepository,
    private val currentUid: String
) : ViewModel() {

    private val _ui = MutableStateFlow(ProfileUi())
    val ui: StateFlow<ProfileUi> = _ui

    init {
        loadProfile()
    }

    private fun loadProfile() {
        _ui.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = userRepo.getMyProfile(currentUid)
            if (result is AppResult.Success) {
                _ui.update { it.copy(profile = result.data, isLoading = false) }
            } else if (result is AppResult.Error) {
                _ui.update { it.copy(error = result.message, isLoading = false) }
            }
        }
    }

    fun updateProfile(updates: Map<String, Any?>) {
        _ui.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = userRepo.updateMyProfile(currentUid, updates)
            if (result is AppResult.Success) {
                loadProfile()
            } else if (result is AppResult.Error) {
                _ui.update { it.copy(error = result.message, isSaving = false) }
            }
        }
    }

    fun uploadPhoto(imageBytes: ByteArray) {
        _ui.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = userRepo.uploadProfilePhoto(imageBytes)
            if (result is AppResult.Success) {
                updateProfile(mapOf("photoUrl" to result.data))
            } else if (result is AppResult.Error) {
                _ui.update { it.copy(error = result.message, isSaving = false) }
            }
        }
    }
}
