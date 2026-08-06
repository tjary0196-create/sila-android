package com.sila.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sila.messaging.data.auth.FirebaseAuthRepository
import com.sila.messaging.data.chat.FirestoreChatRepository
import com.sila.messaging.data.user.FirestoreUserRepository
import com.sila.messaging.data.user.FirestoreUsernameRepository
import com.sila.messaging.domain.auth.AuthRepository
import com.sila.messaging.domain.chat.ChatRepository
import com.sila.messaging.domain.user.UserRepository
import com.sila.messaging.domain.user.UsernameRepository

/**
 * Minimal hand-rolled dependency container. The project doesn't use Hilt/Dagger, so this object
 * hands out singleton repository instances that back the ViewModels — one place to swap
 * implementations (e.g. for tests) instead of `FirebaseX()` scattered across the UI layer.
 */
object ServiceLocator {
    val authRepository: AuthRepository by lazy { FirebaseAuthRepository() }
    val userRepository: UserRepository by lazy { FirestoreUserRepository() }
    val usernameRepository: UsernameRepository by lazy { FirestoreUsernameRepository() }
    val chatRepository: ChatRepository by lazy { FirestoreChatRepository() }
}

/**
 * Builds a [ViewModelProvider.Factory] from a plain lambda, so screens can do:
 * `viewModel(factory = viewModelFactory { MyViewModel(ServiceLocator.xRepository) })`
 * without pulling in Hilt just for constructor injection.
 */
fun <VM : ViewModel> viewModelFactory(create: () -> VM): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = create() as T
    }
