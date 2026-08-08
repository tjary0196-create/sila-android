package com.sila.messaging.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.sila.messaging.BuildConfig
import com.sila.messaging.data.model.User
import com.sila.messaging.util.NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val credentialManager: CredentialManager,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AuthRepository"
        // ⚠️ يجب أن تُقرأ من BuildConfig.FIREBASE_WEB_CLIENT_ID
        private const val WEB_CLIENT_ID = BuildConfig.FIREBASE_WEB_CLIENT_ID
    }

    val currentUser: FirebaseUser? get() = auth.currentUser
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    /**
     * يتحقق فعليًا من حقل isProfileComplete في مستند users/{uid} على Firestore
     * (بدل قيمة ثابتة False كانت موجودة سابقًا وتمنع أي مستخدم قديم من الدخول مباشرة).
     */
    suspend fun checkProfileComplete(uid: String): Boolean {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.getBoolean("isProfileComplete") ?: false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun signInWithGoogle(): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)

        if (!NetworkUtils.isOnline(context)) {
            emit(AuthResult.Error("لا يوجد اتصال بالإنترنت. تأكد من الشبكة وأعد المحاولة."))
            return@flow
        }

        try {
            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(WEB_CLIENT_ID) // ← التصحيح الرئيسي
                .setFilterByAuthorizedAccounts(false)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken

            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()

            val user = authResult.user
            if (user != null) {
                val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
                if (isNewUser) {
                    emit(AuthResult.NewUser(user))
                } else {
                    firestore.collection("users").document(user.uid)
                        .update("lastSeen", com.google.firebase.Timestamp.now(), "online", true)
                        .await()
                    emit(AuthResult.Success(user))
                }
            } else {
                emit(AuthResult.Error("فشل في الحصول على بيانات المستخدم"))
            }

        } catch (e: androidx.credentials.exceptions.GetCredentialException) {
            Log.e(TAG, "Credential error", e)
            when {
                e.message?.contains("No credentials available") == true ->
                    emit(AuthResult.Error("لا يوجد حساب Google متاح. أضف حساباً في إعدادات الجهاز."))
                e.message?.contains("cancelled") == true || e.message?.contains("canceled") == true ->
                    emit(AuthResult.Error("تم إلغاء اختيار الحساب."))
                else -> emit(AuthResult.Error("فشل في تسجيل الدخول: ${e.localizedMessage}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sign in error", e)
            emit(AuthResult.Error("حدث خطأ غير متوقع: ${e.localizedMessage}"))
        }
    }

    suspend fun checkUserProfileComplete(uid: String): Boolean {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                doc.toObject(User::class.java)?.isProfileComplete ?: false
            } else false
        } catch (e: Exception) { false }
    }

    suspend fun createUserProfile(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.uid).set(user).await()
            firestore.collection("usernames").document(user.username)
                .set(mapOf("uid" to user.uid, "createdAt" to com.google.firebase.Timestamp.now()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid)
                .update("online", false, "lastSeen", com.google.firebase.Timestamp.now())
        }
        auth.signOut()
    }
}

sealed class AuthResult {
    object Loading : AuthResult()
    data class Success(val user: FirebaseUser) : AuthResult()
    data class NewUser(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
