package com.sila.messaging.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sila.messaging.data.model.User
import com.sila.messaging.data.repository.AuthRepository
import com.sila.messaging.data.repository.ChatRepository
import com.sila.messaging.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    val user: StateFlow<User?> = MutableStateFlow(null)

    init {
        viewModelScope.launch {
            userRepository.getCurrentUserFlow().collect { (user as MutableStateFlow).value = it }
        }
    }

    fun getUserById(uid: String): Flow<User?> {
        return kotlinx.coroutines.flow.flow {
            val result = userRepository.getUserById(uid)
            emit(result.getOrNull())
        }
    }

    fun updateProfile(displayName: String, bio: String, phone: String, country: String) {
        viewModelScope.launch {
            val current = (user as MutableStateFlow).value ?: return@launch
            val updated = current.copy(
                displayName = displayName,
                bio = bio.ifBlank { null },
                phoneNumber = phone.ifBlank { null },
                country = country.ifBlank { null }
            )
            userRepository.updateProfile(updated)
        }
    }

    fun blockUser(userId: String, targetUsername: String = "", targetPhotoUrl: String? = null) {
        viewModelScope.launch {
            val me = authRepository.currentUser?.uid ?: return@launch
            userRepository.blockUser(
                com.sila.messaging.data.model.BlockedUser(
                    blockId = "${me}_$userId",
                    blockerUid = me,
                    blockedUid = userId,
                    blockedUsername = targetUsername,
                    blockedPhotoUrl = targetPhotoUrl
                )
            )
        }
    }

    /** يبدأ محادثة فعلية مع المستخدم المستهدف (أو يفتح الموجودة مسبقًا) ويرجّع chatId. */
    fun startChat(otherUid: String, otherName: String, otherPhotoUrl: String?, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val me = (user as MutableStateFlow).value
            val result = chatRepository.createOrGetChat(
                otherUid = otherUid,
                myName = me?.displayName ?: "",
                myPhotoUrl = me?.photoUrl,
                otherName = otherName,
                otherPhotoUrl = otherPhotoUrl
            )
            onResult(result.getOrNull())
        }
    }

    fun signOut() {
        authRepository.signOut()
    }
}
