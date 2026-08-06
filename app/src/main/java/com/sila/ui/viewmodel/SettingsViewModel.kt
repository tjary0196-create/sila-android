package com.sila.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sila.di.ServiceLocator
import com.sila.messaging.core.result.AppResult
import com.sila.messaging.domain.auth.AuthRepository
import com.sila.messaging.domain.user.UserRepository
import com.sila.model.User
import com.sila.ui.mapper.toUiUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val myUid: String,
    private val userRepository: UserRepository = ServiceLocator.userRepository,
    private val authRepository: AuthRepository = ServiceLocator.authRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = userRepository.getMyProfile(myUid)) {
                is AppResult.Success -> _user.value = result.data?.toUiUser()
                is AppResult.Error -> Unit
            }
        }
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}
