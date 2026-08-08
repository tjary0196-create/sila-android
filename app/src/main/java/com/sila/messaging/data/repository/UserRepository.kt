package com.sila.messaging.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sila.messaging.data.model.BlockedUser
import com.sila.messaging.data.model.Session
import com.sila.messaging.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val currentUid: String? get() = auth.currentUser?.uid

    fun getCurrentUserFlow(): Flow<User?> = callbackFlow {
        val uid = currentUid ?: run { trySend(null); close(); return@callbackFlow }
        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.toObject(User::class.java))
            }
        awaitClose { listener.remove() }
    }

    suspend fun getUserById(uid: String): Result<User> {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            val user = doc.toObject(User::class.java)
            if (user != null) Result.success(user) else Result.failure(Exception("User not found"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun checkUsernameAvailability(username: String): Result<Boolean> {
        return try {
            val doc = firestore.collection("usernames").document(username.lowercase()).get().await()
            Result.success(!doc.exists())
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun updateProfile(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun updatePublicProfileSettings(settings: com.sila.messaging.data.model.PublicProfileSettings): Result<Unit> {
        val uid = currentUid ?: return Result.failure(Exception("Not logged in"))
        return try {
            firestore.collection("users").document(uid).update("publicProfile", settings).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun getBlockedUsers(): Flow<List<BlockedUser>> = callbackFlow {
        val uid = currentUid ?: run { trySend(emptyList()); close(); return@callbackFlow }
        val listener = firestore.collection("blockedUsers")
            .whereEqualTo("blockerUid", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull { it.toObject(BlockedUser::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun blockUser(blockedUser: BlockedUser): Result<Unit> {
        return try {
            firestore.collection("blockedUsers").document(blockedUser.blockId).set(blockedUser).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun unblockUser(blockId: String): Result<Unit> {
        return try {
            firestore.collection("blockedUsers").document(blockId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun getActiveSessions(): Flow<List<Session>> = callbackFlow {
        val uid = currentUid ?: run { trySend(emptyList()); close(); return@callbackFlow }
        val listener = firestore.collection("sessions")
            .whereEqualTo("uid", uid)
            .orderBy("lastActive", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull { it.toObject(Session::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun terminateAllOtherSessions(): Result<Unit> {
        val uid = currentUid ?: return Result.failure(Exception("Not logged in"))
        return try {
            val batch = firestore.batch()
            val sessions = firestore.collection("sessions")
                .whereEqualTo("uid", uid)
                .whereEqualTo("isCurrent", false)
                .get().await()
            sessions.documents.forEach { batch.update(it.reference, "isActive", false) }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}
