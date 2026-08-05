package com.sila.messaging.domain.user

data class PrivacySettings(
    val showBio: Boolean = true,
    val showBirthDate: Boolean = true,
    val showCountry: Boolean = true,
    val showLastSeen: Boolean = true
)

data class UserProfile(
    val uid: String,
    val username: String,
    val displayName: String,
    val photoUrl: String?,
    val bio: String?,
    val birthDate: String?,
    val country: String?,
    val status: String,
    val privacy: PrivacySettings
)
