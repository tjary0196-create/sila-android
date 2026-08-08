package com.sila.messaging.data.model

import com.google.firebase.Timestamp

data class MessageRequest(
    val requestId: String = "",
    val fromUid: String = "",
    val toUid: String = "",
    val status: String = "pending",
    val message: String? = null,
    val timestamp: Timestamp = Timestamp.now()
)
