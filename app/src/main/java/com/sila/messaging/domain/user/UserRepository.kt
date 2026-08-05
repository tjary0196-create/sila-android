package com.sila.messaging.domain.user

import com.sila.messaging.core.result.AppResult

interface UserRepository {
    /** The full, private profile — only ever valid for the CURRENTLY SIGNED-IN user's own uid. */
    suspend fun getMyProfile(uid: String): AppResult<UserProfile?>

    /** The privacy-filtered, public projection of another user's profile — safe to call with any uid. */
    suspend fun getPublicProfile(uid: String): AppResult<UserProfile?>

    suspend fun updateMyProfile(uid: String, updates: Map<String, Any?>): AppResult<Unit>
    suspend fun searchUsers(prefix: String): AppResult<List<Pair<String, String>>>

    /** Uploads a profile photo via our own authenticated backend (Cloud Function) — the client never
     *  holds the imgbb API key. */
    suspend fun uploadProfilePhoto(imageBytes: ByteArray): AppResult<String>
}
