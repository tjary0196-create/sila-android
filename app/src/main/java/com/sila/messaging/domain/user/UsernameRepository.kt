package com.sila.messaging.domain.user

import com.sila.messaging.core.result.AppResult

interface UsernameRepository {
    suspend fun claimUsername(
        uid: String,
        username: String,
        displayName: String?,
        photoUrl: String?
    ): AppResult<Unit>
}
