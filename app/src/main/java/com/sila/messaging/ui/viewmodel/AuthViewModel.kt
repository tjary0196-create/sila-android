package com.sila.messaging.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.sila.messaging.data.repository.AuthRepository
import com.sila.messaging.data.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthResult>(AuthResult.Loading)
    val authState: StateFlow<AuthResult> = _authState

    fun isLoggedIn(): Boolean = authRepository.isUserLoggedIn()

    suspend fun isProfileComplete(): Boolean {
        val uid = authRepository.currentUser?.uid ?: return false
        return authRepository.checkProfileComplete(uid)
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            authRepository.signInWithGoogle().collect { result ->
                _authState.value = result
            }
        }
    }

    fun resetState() {
        _authState.value = AuthResult.Loading
    }

    fun getCurrentUser(): FirebaseUser? = authRepository.currentUser
}
