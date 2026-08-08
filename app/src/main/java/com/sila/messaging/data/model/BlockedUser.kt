package com.sila.messaging.data.model

import com.google.firebase.Timestamp

data class BlockedUser(
    val blockId: String = "",
    val blockerUid: String = "",
    val blockedUid: String = "",
    val blockedUsername: String = "",
    val blockedPhotoUrl: String? = null,
    val reason: String? = null,
    val timestamp: Timestamp = Timestamp.now()
)
