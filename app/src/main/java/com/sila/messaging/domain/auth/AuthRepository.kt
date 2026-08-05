package com.sila.messaging.domain.auth

import com.sila.messaging.core.result.AppResult
import kotlinx.coroutines.flow.Flow

data class AuthSession(val uid: String)

interface AuthRepository {
    val session: Flow<AuthSession?>
    suspend fun signInWithGoogleIdToken(idToken: String): AppResult<Unit>
    suspend fun signOut(): AppResult<Unit>
}
