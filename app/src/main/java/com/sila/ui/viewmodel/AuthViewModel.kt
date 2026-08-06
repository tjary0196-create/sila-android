package com.sila.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sila.di.ServiceLocator
import com.sila.messaging.core.result.AppResult
import com.sila.messaging.domain.auth.AuthRepository
import com.sila.messaging.domain.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    /** We haven't heard from Firebase Auth yet — don't show login OR the app, just wait. */
    data object Loading : AuthUiState

    /** No signed-in user. */
    data object LoggedOut : AuthUiState

    /** Signed in with Firebase, but we're still checking Firestore for an existing profile. */
    data class CheckingProfile(val uid: String) : AuthUiState

    /** Signed in, but this uid has no `users/{uid}` doc yet — must claim a username first. */
    data class NeedsUsername(val uid: String) : AuthUiState

    /** Signed in and has a complete profile — the main app graph can load. */
    data class LoggedIn(val uid: String) : AuthUiState
}

class AuthViewModel(
    private val authRepository: AuthRepository = ServiceLocator.authRepository,
    private val userRepository: UserRepository = ServiceLocator.userRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    var signInError by mutableStateOf<String?>(null)
        private set

    var isSigningIn by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            authRepository.session.collect { session ->
                if (session == null) {
                    _uiState.value = AuthUiState.LoggedOut
                } else {
                    refreshProfileState(session.uid)
                }
            }
        }
    }

    private suspend fun refreshProfileState(uid: String) {
        _uiState.value = AuthUiState.CheckingProfile(uid)
        when (val result = userRepository.getMyProfile(uid)) {
            is AppResult.Success -> {
                _uiState.value = if (result.data == null) {
                    AuthUiState.NeedsUsername(uid)
                } else {
                    AuthUiState.LoggedIn(uid)
                }
            }
            is AppResult.Error -> {
                // We know they're authenticated but couldn't confirm a profile — safest is to
                // route through username setup rather than get stuck on a spinner forever.
                _uiState.value = AuthUiState.NeedsUsername(uid)
            }
        }
    }

    /** Called by [com.sila.ui.screens.UsernameSetupScreen] after successfully claiming a username. */
    fun onProfileCreated(uid: String) {
        viewModelScope.launch { refreshProfileState(uid) }
    }

    fun signInWithGoogleIdToken(idToken: String) {
        if (isSigningIn) return
        isSigningIn = true
        signInError = null
        viewModelScope.launch {
            when (val result = authRepository.signInWithGoogleIdToken(idToken)) {
                is AppResult.Error -> signInError = result.message
                is AppResult.Success -> signInError = null
            }
            isSigningIn = false
        }
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}
