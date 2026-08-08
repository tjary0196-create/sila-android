package com.sila.messaging.data.model

import com.google.firebase.Timestamp

data class Session(
    val sessionId: String = "",
    val uid: String = "",
    val deviceName: String = "",
    val deviceModel: String = "",
    val osVersion: String = "",
    val appVersion: String = "",
    val ipAddress: String = "",
    val location: String? = null,
    val lastActive: Timestamp = Timestamp.now(),
    val isCurrent: Boolean = false,
    val fcmToken: String = ""
)
