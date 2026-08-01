package com.sila.messaging.model

data class SimpleUser(
    val uid: String = "",
    val username: String = "",
    val displayName: String? = null,
    val photoUrl: String? = null
)
