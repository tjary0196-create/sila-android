package com.sila.messaging.data.user

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sila.messaging.core.result.AppResult
import com.sila.messaging.domain.user.UsernameRepository
import kotlinx.coroutines.tasks.await

class FirestoreUsernameRepository : UsernameRepository {
    private val db = Firebase.firestore
    private val usernames = db.collection("usernames")
    private val users = db.collection("users")
    private val publicProfiles = db.collection("publicProfiles")

    override suspend fun claimUsername(
        uid: String,
        username: String,
        displayName: String?,
        photoUrl: String?
    ): AppResult<Unit> {
        val cleaned = username.trim()
        val key = cleaned.lowercase()

        if (key.length < 3) return AppResult.Error("اسم المستخدم قصير")
        if (key.length > 20) return AppResult.Error("اسم المستخدم طويل جداً (الحد الأقصى 20 حرف)")
        if (!key.matches(Regex("^[a-z0-9_.]+$"))) {
            return AppResult.Error("اسم المستخدم يجب أن يكون أحرف/أرقام/نقطة/شرطة سفلية")
        }

        val safeDisplayName = (displayName ?: "").take(60)
        val safePhotoUrl = photoUrl?.takeIf { it.length <= 2048 }

        return try {
            db.runTransaction { tx ->
                val usernameRef = usernames.document(key)
                if (tx.get(usernameRef).exists()) throw IllegalStateException("username_taken")

                tx.set(usernameRef, mapOf("uid" to uid, "createdAt" to FieldValue.serverTimestamp()))

                val privateProfile = mapOf(
                    "uid" to uid,
                    "username" to key,
                    "displayName" to safeDisplayName,
                    "photoUrl" to (safePhotoUrl ?: ""),
                    "status" to "online",
                    "showBio" to true,
                    "showBirthDate" to true,
                    "showCountry" to true,
                    "showLastSeen" to true,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                tx.set(users.document(uid), privateProfile)

                val publicProfile = mapOf(
                    "uid" to uid,
                    "username" to key,
                    "displayName" to safeDisplayName,
                    "photoUrl" to (safePhotoUrl ?: ""),
                    "status" to "online",
                    "showLastSeen" to true
                )
                tx.set(publicProfiles.document(uid), publicProfile)
                null
            }.await()
            AppResult.Success(Unit)
        } catch (t: Throwable) {
            val msg = if (t.message == "username_taken") "اسم المستخدم محجوز" else "فشل حفظ اسم المستخدم"
            AppResult.Error(msg, t)
        }
    }
}
