package com.sila.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sila.di.ServiceLocator
import com.sila.messaging.core.result.AppResult
import com.sila.messaging.domain.user.UserRepository
import com.sila.model.User
import com.sila.ui.mapper.toUiUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val myUid: String,
    private val targetUid: String,
    private val userRepository: UserRepository = ServiceLocator.userRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val isOwnProfile: Boolean get() = myUid == targetUid

    init {
        viewModelScope.launch {
            val result = if (isOwnProfile) {
                userRepository.getMyProfile(targetUid)
            } else {
                userRepository.getPublicProfile(targetUid)
            }
            when (result) {
                is AppResult.Success -> _user.value = result.data?.toUiUser()
                is AppResult.Error -> _errorMessage.value = result.message
            }
        }
    }
}
