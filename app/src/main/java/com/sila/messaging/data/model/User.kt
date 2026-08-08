package com.sila.messaging.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class User(
    val uid: String = "",
    val email: String = "",
    @PropertyName("displayName") val displayName: String = "",
    val username: String = "",
    @PropertyName("photoUrl") val photoUrl: String? = null,
    val bio: String? = null,
    @PropertyName("phoneNumber") val phoneNumber: String? = null,
    @PropertyName("birthDate") val birthDate: Timestamp? = null,
    val country: String? = null,
    val language: String = "ar",
    @PropertyName("isProfileComplete") val isProfileComplete: Boolean = false,
    @PropertyName("createdAt") val createdAt: Timestamp = Timestamp.now(),
    @PropertyName("lastSeen") val lastSeen: Timestamp = Timestamp.now(),
    val online: Boolean = false,
    @PropertyName("publicProfile") val publicProfile: PublicProfileSettings = PublicProfileSettings(),
    @PropertyName("fcmToken") val fcmToken: String? = null
)

data class PublicProfileSettings(
    @PropertyName("showPhone") val showPhone: Boolean = false,
    @PropertyName("showBirthDate") val showBirthDate: Boolean = false,
    @PropertyName("showLastSeen") val showLastSeen: Boolean = true,
    @PropertyName("allowAddToGroups") val allowAddToGroups: Boolean = true,
    @PropertyName("readReceipts") val readReceipts: Boolean = true
)
