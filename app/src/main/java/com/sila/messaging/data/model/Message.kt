package com.sila.messaging.data.model

import com.google.firebase.Timestamp

data class Message(
    val messageId: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    val type: String = "text",
    val mediaUrl: String? = null,
    val replyTo: String? = null,
    val forwardedFrom: String? = null,
    val reactions: Map<String, String> = emptyMap(),
    val status: String = "sent",
    val deletedFor: List<String> = emptyList(),
    val deletedForEveryone: Boolean = false,
    val timestamp: Timestamp = Timestamp.now(),
    val editedAt: Timestamp? = null
)
