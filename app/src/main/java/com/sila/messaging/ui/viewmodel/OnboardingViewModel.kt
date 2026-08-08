package com.sila.messaging.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sila.messaging.data.model.User
import com.sila.messaging.data.repository.AuthRepository
import com.sila.messaging.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    var fullName: String = ""
    var username: String = ""
    var bio: String = ""
    var language: String = "ar"
    var photoUrl: String? = null

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    suspend fun checkUsername(username: String): Boolean {
        return when (val result = userRepository.checkUsernameAvailability(username)) {
            is kotlin.Result.Success -> result.getOrDefault(false)
            else -> false
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            _isLoading.value = true
            val user = authRepository.currentUser ?: run {
                _error.value = "لم يتم تسجيل الدخول"
                _isLoading.value = false
                return@launch
            }

            val newUser = User(
                uid = user.uid,
                email = user.email ?: "",
                displayName = fullName,
                username = username.lowercase(),
                photoUrl = photoUrl,
                bio = bio.ifBlank { null },
                language = language,
                isProfileComplete = true
            )

            when (val result = authRepository.createUserProfile(newUser)) {
                is kotlin.Result.Success -> { }
                is kotlin.Result.Failure -> _error.value = result.exceptionOrNull()?.message
            }
            _isLoading.value = false
        }
    }
}
