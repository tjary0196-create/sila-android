package com.sila.messaging.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sila.messaging.core.result.AppResult
import com.sila.messaging.domain.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AuthUi(
    val signedIn: Boolean = false,
    val uid: String? = null,
    val error: String? = null
)

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {
    private val _ui = MutableStateFlow(AuthUi())
    val ui: StateFlow<AuthUi> = _ui

    init {
        viewModelScope.launch {
            repo.session.collectLatest { session ->
                _ui.value = _ui.value.copy(
                    signedIn = session != null,
                    uid = session?.uid
                )
            }
        }
    }

    fun onErrorDismiss() {
        _ui.value = _ui.value.copy(error = null)
    }

    /** Surfaces a Google Sign-In failure that happened before we ever reached Firebase
     *  (e.g. the system account picker returned no account, or no idToken). */
    fun onSignInFailed(message: String) {
        _ui.value = _ui.value.copy(error = message)
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            val res = repo.signInWithGoogleIdToken(idToken)
            if (res is AppResult.Error) {
                _ui.value = _ui.value.copy(error = res.message)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repo.signOut()
        }
    }
}
