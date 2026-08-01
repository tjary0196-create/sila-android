package com.sila.messaging.data

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val uid: String = "",
    val username: String = "",
    val displayName: String? = null,
    val photoUrl: String? = null,
    val createdAt: com.google.firebase.Timestamp? = null
)

class UserRepository {
    private val firestore = Firebase.firestore
    private val usersColl = firestore.collection("users")
    private val usernamesColl = firestore.collection("usernames")
    private val publicProfilesColl = firestore.collection("publicProfiles")

    suspend fun getUserProfile(uid: String): UserProfile? {
        val doc = usersColl.document(uid).get().await()
        return if (doc.exists()) {
            doc.toObject(UserProfile::class.java)
        } else null
    }

    suspend fun claimUsername(uid: String, username: String, displayName: String?, photoUrl: String?): Boolean {
        try {
            firestore.runTransaction { transaction ->
                val usernameDocRef = usernamesColl.document(username)
                val usernameSnapshot = transaction.get(usernameDocRef)
                if (usernameSnapshot.exists()) {
                    throw IllegalStateException("username_taken")
                }
                transaction.set(usernameDocRef, mapOf(
                    "uid" to uid,
                    "createdAt" to FieldValue.serverTimestamp()
                ))
                val userRef = usersColl.document(uid)
                transaction.set(userRef, mapOf(
                    "uid" to uid,
                    "username" to username,
                    "displayName" to (displayName ?: ""),
                    "photoUrl" to (photoUrl ?: ""),
                    "createdAt" to FieldValue.serverTimestamp()
                ))
                val publicRef = publicProfilesColl.document(uid)
                transaction.set(publicRef, mapOf(
                    "uid" to uid,
                    "username" to username,
                    "displayName" to (displayName ?: ""),
                    "photoUrl" to (photoUrl ?: ""),
                    "createdAt" to FieldValue.serverTimestamp()
                ))
                null
            }.await()
            return true
        } catch (e: Exception) {
            Log.w("UserRepository", "claimUsername failed: ${e.localizedMessage}")
            return false
        }
    }

    suspend fun searchByPrefix(prefix: String, limit: Long = 20): List<Pair<String, String>> {
        if (prefix.isBlank()) return emptyList()
        val end = prefix + '\uf8ff'
        val querySnapshot = usernamesColl
            .whereGreaterThanOrEqualTo("__name__", prefix)
            .whereLessThanOrEqualTo("__name__", end)
            .limit(limit)
            .get()
            .await()
        val result = mutableListOf<Pair<String, String>>()
        for (doc in querySnapshot.documents) {
            val uname = doc.id
            val uid = doc.getString("uid") ?: continue
            result.add(Pair(uname, uid))
        }
        return result
    }
}
