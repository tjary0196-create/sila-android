package com.sila.messaging.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import com.sila.messaging.BuildConfig

data class AuthState(
    val firebaseUserUid: String? = null,
    val isSignedIn: Boolean = false,
    val needsUsername: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel : ViewModel() {
    private val TAG = "AuthViewModel"
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = Firebase.firestore

    private val _uiState = MutableStateFlow(AuthState(
        firebaseUserUid = auth.currentUser?.uid,
        isSignedIn = auth.currentUser != null,
        needsUsername = false
    ))
    val uiState: StateFlow<AuthState> = _uiState

    init {
        auth.addIdTokenListener {
            val user = auth.currentUser
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(
                    firebaseUserUid = user?.uid,
                    isSignedIn = user != null
                )
            }
        }
    }

    fun getWebClientId(): String {
        return BuildConfig.WEB_CLIENT_ID
    }

    fun onSignInFailed(message: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = message)
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        viewModelScope.launch {
                            _uiState.value = _uiState.value.copy(
                                firebaseUserUid = uid,
                                isSignedIn = uid != null
                            )
                        }
                        viewModelScope.launch {
                            checkIfNeedsUsername()
                        }
                    } else {
                        val err = task.exception?.localizedMessage ?: "Auth failed"
                        onSignInFailed(err)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "signInWithGoogle error", e)
                onSignInFailed(e.localizedMessage ?: "signIn exception")
            }
        }
    }

    fun signOut() {
        auth.signOut()
        viewModelScope.launch {
            _uiState.value = AuthState()
        }
    }

    fun currentUid(): String? = auth.currentUser?.uid

    suspend fun checkIfNeedsUsername() {
        val uid = auth.currentUser?.uid ?: return
        try {
            val doc = firestore.collection("users").document(uid).get().await()
            val needs = !doc.exists()
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(needsUsername = needs)
            }
        } catch (e: Exception) {
            Log.w(TAG, "checkIfNeedsUsername error: ${e.localizedMessage}")
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(needsUsername = true)
            }
        }
    }
}
