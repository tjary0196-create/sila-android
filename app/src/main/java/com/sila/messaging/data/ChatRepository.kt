package com.sila.messaging.data

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String? = null,
    val updatedAt: Timestamp? = null
)

data class Message(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val createdAt: Timestamp? = null
)

class ChatRepository {
    private val firestore = Firebase.firestore
    private val chats = firestore.collection("chats")

    fun chatIdFor(u1: String, u2: String): String {
        val list = listOf(u1, u2).sorted()
        return list.joinToString("_")
    }

    suspend fun getOrCreateChat(u1: String, u2: String): String {
        val chatId = chatIdFor(u1, u2)
        val chatRef = chats.document(chatId)
        val snapshot = chatRef.get().await()
        if (snapshot.exists()) return chatId
        firestore.runTransaction { tx ->
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
        return chatId
    }

    suspend fun sendMessage(chatId: String, senderId: String, text: String) {
        val messagesRef = chats.document(chatId).collection("messages")
        val newMsgRef = messagesRef.document()
        val chatRef = chats.document(chatId)
        val data = mapOf(
            "senderId" to senderId,
            "text" to text,
            "createdAt" to FieldValue.serverTimestamp()
        )
        firestore.runTransaction { tx ->
            tx.set(newMsgRef, data)
            tx.update(chatRef, mapOf(
                "lastMessage" to text,
                "updatedAt" to FieldValue.serverTimestamp()
            ))
            null
        }.await()
    }

    fun messagesListener(chatId: String): Flow<List<Message>> = callbackFlow {
        val messagesRef = chats.document(chatId).collection("messages").orderBy("createdAt", Query.Direction.ASCENDING)
        val listener = messagesRef.addSnapshotListener { snap, ex ->
            if (ex != null) {
                close(ex)
                return@addSnapshotListener
            }
            val list = snap?.documents?.map { doc ->
                Message(
                    id = doc.id,
                    senderId = doc.getString("senderId") ?: "",
                    text = doc.getString("text") ?: "",
                    createdAt = doc.getTimestamp("createdAt")
                )
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    fun chatsForUserListener(uid: String): Flow<List<Chat>> = callbackFlow {
        val query = chats.whereArrayContains("participants", uid).orderBy("updatedAt", Query.Direction.DESCENDING)
        val listener = query.addSnapshotListener { snap, ex ->
            if (ex != null) {
                close(ex)
                return@addSnapshotListener
            }
            val list = snap?.documents?.map { doc ->
                Chat(
                    id = doc.id,
                    participants = doc.get("participants") as? List<String> ?: emptyList(),
                    lastMessage = doc.getString("lastMessage"),
                    updatedAt = doc.getTimestamp("updatedAt")
                )
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }
}
