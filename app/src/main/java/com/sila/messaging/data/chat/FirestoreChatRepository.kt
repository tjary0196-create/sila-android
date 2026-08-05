package com.sila.messaging.data.chat

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sila.messaging.core.result.AppResult
import com.sila.messaging.domain.chat.ChatMessage
import com.sila.messaging.domain.chat.ChatRepository
import com.sila.messaging.domain.chat.ChatSummary
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreChatRepository : ChatRepository {
    private val db = Firebase.firestore
    private val chats = db.collection("chats")

    private fun chatIdFor(u1: String, u2: String): String {
        return listOf(u1, u2).sorted().joinToString("_")
    }

    override suspend fun getOrCreateDirectChat(u1: String, u2: String): AppResult<String> = try {
        val chatId = chatIdFor(u1, u2)
        val chatRef = chats.document(chatId)
        val snapshot = chatRef.get().await()
        if (!snapshot.exists()) {
            db.runTransaction { tx ->
                val s = tx.get(chatRef)
                if (!s.exists()) {
                    tx.set(chatRef, mapOf(
                        "participants" to listOf(u1, u2),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                        "lastMessage" to ""
                    ))
                }
                null
            }.await()
        }
        AppResult.Success(chatId)
    } catch (t: Throwable) {
        AppResult.Error("فشل إنشاء المحادثة", t)
    }

    override suspend fun sendText(chatId: String, senderId: String, text: String): AppResult<Unit> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return AppResult.Error("لا يمكن إرسال رسالة فارغة")
        if (trimmed.length > 4000) return AppResult.Error("الرسالة طويلة جداً (الحد الأقصى 4000 حرف)")

        return try {
            val chatRef = chats.document(chatId)
            val messagesRef = chatRef.collection("messages")
            val newMsgRef = messagesRef.document()

            val data = mapOf(
                "senderId" to senderId,
                "text" to trimmed,
                "createdAt" to FieldValue.serverTimestamp()
            )

            db.runTransaction { tx ->
                tx.set(newMsgRef, data)
                tx.update(chatRef, mapOf(
                    "lastMessage" to trimmed,
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
                null
            }.await()
            AppResult.Success(Unit)
        } catch (t: Throwable) {
            AppResult.Error("فشل إرسال الرسالة", t)
        }
    }

    override fun listenMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val query = chats.document(chatId).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            
        val listener = query.addSnapshotListener { snap, ex ->
            if (ex != null) {
                close(ex)
                return@addSnapshotListener
            }
            val list = snap?.documents?.map { doc ->
                ChatMessage(
                    id = doc.id,
                    senderId = doc.getString("senderId") ?: "",
                    text = doc.getString("text") ?: "",
                    createdAtMillis = doc.getTimestamp("createdAt")?.toDate()?.time
                )
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    override fun listenMyChats(uid: String): Flow<List<ChatSummary>> = callbackFlow {
        val query = chats.whereArrayContains("participants", uid)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            
        val listener = query.addSnapshotListener { snap, ex ->
            if (ex != null) {
                close(ex)
                return@addSnapshotListener
            }
            val list = snap?.documents?.map { doc ->
                ChatSummary(
                    chatId = doc.id,
                    participants = doc.get("participants") as? List<String> ?: emptyList(),
                    lastMessage = doc.getString("lastMessage"),
                    updatedAtMillis = doc.getTimestamp("updatedAt")?.toDate()?.time
                )
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }
}
