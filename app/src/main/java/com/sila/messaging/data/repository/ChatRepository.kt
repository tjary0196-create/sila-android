package com.sila.messaging.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sila.messaging.data.model.Chat
import com.sila.messaging.data.model.LastMessage
import com.sila.messaging.data.model.Message
import com.sila.messaging.data.model.MessageRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val currentUid: String? get() = auth.currentUser?.uid

    fun getChats(): Flow<List<Chat>> = callbackFlow {
        val uid = currentUid ?: run { trySend(emptyList()); close(); return@callbackFlow }
        val listener = firestore.collection("chats")
            .whereArrayContains("participants", uid)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull { it.toObject(Chat::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val uid = currentUid ?: run { trySend(emptyList()); close(); return@callbackFlow }
        val listener = firestore.collection("messages")
            .whereEqualTo("chatId", chatId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull { 
                    it.toObject(Message::class.java)?.copy(messageId = it.id)
                }?.filter { !it.deletedFor.contains(uid) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(chatId: String, text: String, replyTo: String? = null): Result<Unit> {
        val uid = currentUid ?: return Result.failure(Exception("Not logged in"))
        return try {
            val message = Message(
                messageId = "", chatId = chatId, senderId = uid,
                text = text, type = "text", replyTo = replyTo,
                status = "sent", timestamp = com.google.firebase.Timestamp.now()
            )
            val msgRef = firestore.collection("messages").add(message).await()
            firestore.collection("chats").document(chatId).update(
                "lastMessage", LastMessage(text, uid, com.google.firebase.Timestamp.now(), "text"),
                "updatedAt", com.google.firebase.Timestamp.now()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deleteMessageForMe(messageId: String): Result<Unit> {
        val uid = currentUid ?: return Result.failure(Exception("Not logged in"))
        return try {
            firestore.collection("messages").document(messageId)
                .update("deletedFor", com.google.firebase.firestore.FieldValue.arrayUnion(uid)).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deleteMessageForEveryone(messageId: String): Result<Unit> {
        return try {
            firestore.collection("messages").document(messageId)
                .update("deletedForEveryone", true, "text", "تم حذف هذه الرسالة").await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun addReaction(messageId: String, emoji: String): Result<Unit> {
        val uid = currentUid ?: return Result.failure(Exception("Not logged in"))
        return try {
            firestore.collection("messages").document(messageId)
                .update("reactions.$uid", emoji).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun removeReaction(messageId: String): Result<Unit> {
        val uid = currentUid ?: return Result.failure(Exception("Not logged in"))
        return try {
            firestore.collection("messages").document(messageId)
                .update("reactions.$uid", com.google.firebase.firestore.FieldValue.delete()).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun getMessageRequests(): Flow<List<MessageRequest>> = callbackFlow {
        val uid = currentUid ?: run { trySend(emptyList()); close(); return@callbackFlow }
        val listener = firestore.collection("messageRequests")
            .whereEqualTo("toUid", uid)
            .whereEqualTo("status", "pending")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull { it.toObject(MessageRequest::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    /**
     * قبول طلب الرسالة كان يكتفي بتغيير status فقط، بدون إنشاء محادثة فعلية —
     * الآن يجلب بيانات الطرفين وينشئ/يرجّع المحادثة، ليقدر المستخدم يفتحها فورًا.
     */
    suspend fun acceptMessageRequest(requestId: String): Result<String> {
        val myUid = currentUid ?: return Result.failure(Exception("Not logged in"))
        return try {
            val reqDoc = firestore.collection("messageRequests").document(requestId).get().await()
            val fromUid = reqDoc.getString("fromUid") ?: return Result.failure(Exception("Invalid request"))

            firestore.collection("messageRequests").document(requestId).update("status", "accepted").await()

            val meDoc = firestore.collection("users").document(myUid).get().await()
            val fromDoc = firestore.collection("users").document(fromUid).get().await()

            createOrGetChat(
                otherUid = fromUid,
                myName = meDoc.getString("displayName") ?: "",
                myPhotoUrl = meDoc.getString("photoUrl"),
                otherName = fromDoc.getString("displayName") ?: "",
                otherPhotoUrl = fromDoc.getString("photoUrl")
            )
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun declineMessageRequest(requestId: String): Result<Unit> {
        return try {
            firestore.collection("messageRequests").document(requestId).update("status", "declined").await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    /**
     * ينشئ محادثة جديدة بين طرفين إذا ما كانت موجودة، أو يرجّع الموجودة مسبقًا.
     * كانت مفقودة بالكامل — يعني زر "مراسلة" على أي بروفايل، وحتى قبول طلب رسالة،
     * ما كانا يُنشئان محادثة فعلية على Firestore.
     */
    suspend fun createOrGetChat(
        otherUid: String,
        myName: String,
        myPhotoUrl: String?,
        otherName: String,
        otherPhotoUrl: String?
    ): Result<String> {
        val uid = currentUid ?: return Result.failure(Exception("Not logged in"))
        return try {
            val existing = firestore.collection("chats")
                .whereArrayContains("participants", uid)
                .get().await()
                .documents
                .firstOrNull { doc ->
                    val participants = doc.get("participants") as? List<*>
                    participants != null && participants.contains(otherUid) && participants.size == 2
                }
            if (existing != null) return Result.success(existing.id)

            val chatRef = firestore.collection("chats").document()
            val chat = Chat(
                chatId = chatRef.id,
                participants = listOf(uid, otherUid),
                participantNames = mapOf(uid to myName, otherUid to otherName),
                participantPhotos = mapOf(uid to myPhotoUrl, otherUid to otherPhotoUrl),
                isGroup = false
            )
            chatRef.set(chat).await()
            Result.success(chatRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMediaMessage(chatId: String, mediaUrl: String, type: String, text: String = ""): Result<Unit> {
        val uid = currentUid ?: return Result.failure(Exception("Not logged in"))
        return try {
            val message = Message(
                messageId = "", chatId = chatId, senderId = uid,
                text = text, type = type, mediaUrl = mediaUrl,
                status = "sent", timestamp = com.google.firebase.Timestamp.now()
            )
            val msgRef = firestore.collection("messages").add(message).await()
            firestore.collection("chats").document(chatId).update(
                "lastMessage", LastMessage(
                    if (type == "image") "صورة" else if (type == "file") "ملف" else "رسالة صوتية",
                    uid, com.google.firebase.Timestamp.now(), type
                ),
                "updatedAt", com.google.firebase.Timestamp.now()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}
