package com.sila.messaging.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.sila.messaging.core.result.AppResult
import com.sila.messaging.domain.auth.AuthRepository
import com.sila.messaging.domain.auth.AuthSession
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override val session: Flow<AuthSession?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { fa ->
            val uid = fa.currentUser?.uid
            trySend(uid?.let { AuthSession(it) })
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): AppResult<Unit> = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
        AppResult.Success(Unit)
    } catch (t: Throwable) {
        AppResult.Error("فشل تسجيل الدخول عبر Google", t)
    }

    override suspend fun signOut(): AppResult<Unit> = try {
        auth.signOut()
        AppResult.Success(Unit)
    } catch (t: Throwable) {
        AppResult.Error("فشل تسجيل الخروج", t)
    }
}
