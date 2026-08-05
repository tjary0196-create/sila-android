package com.sila.messaging.data.user

import android.util.Base64
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.sila.messaging.core.result.AppResult
import com.sila.messaging.domain.user.PrivacySettings
import com.sila.messaging.domain.user.UserProfile
import com.sila.messaging.domain.user.UserRepository
import kotlinx.coroutines.tasks.await

class FirestoreUserRepository : UserRepository {
    private val db = Firebase.firestore
    private val functions = Firebase.functions
    private val users = db.collection("users")
    private val publicProfiles = db.collection("publicProfiles")
    private val usernames = db.collection("usernames")

    /** Maps a "users/{uid}" (private) document into the full [UserProfile], including real privacy flags. */
    private fun DocumentSnapshot.toPrivateProfile(): UserProfile = UserProfile(
        uid = getString("uid") ?: id,
        username = getString("username") ?: "",
        displayName = getString("displayName") ?: "",
        photoUrl = getString("photoUrl"),
        bio = getString("bio"),
        birthDate = getString("birthDate"),
        country = getString("country"),
        status = getString("status") ?: "online",
        privacy = PrivacySettings(
            showBio = getBoolean("showBio") ?: true,
            showBirthDate = getBoolean("showBirthDate") ?: true,
            showCountry = getBoolean("showCountry") ?: true,
            showLastSeen = getBoolean("showLastSeen") ?: true
        )
    )

    /** Maps a "publicProfiles/{uid}" document — already privacy-filtered at write time — into a [UserProfile].
     *  Fields that were hidden by the owner's privacy settings simply won't exist on this document, so we
     *  treat "field present" as the effective per-field privacy flag for display purposes. */
    private fun DocumentSnapshot.toPublicProfile(): UserProfile = UserProfile(
        uid = getString("uid") ?: id,
        username = getString("username") ?: "",
        displayName = getString("displayName") ?: "",
        photoUrl = getString("photoUrl"),
        bio = getString("bio"),
        birthDate = getString("birthDate"),
        country = getString("country"),
        status = getString("status") ?: "offline",
        privacy = PrivacySettings(
            showBio = contains("bio"),
            showBirthDate = contains("birthDate"),
            showCountry = contains("country"),
            showLastSeen = getBoolean("showLastSeen") ?: false
        )
    )

    override suspend fun getMyProfile(uid: String): AppResult<UserProfile?> = try {
        val doc = users.document(uid).get().await()
        AppResult.Success(if (doc.exists()) doc.toPrivateProfile() else null)
    } catch (t: Throwable) {
        AppResult.Error("فشل جلب الملف الشخصي", t)
    }

    override suspend fun getPublicProfile(uid: String): AppResult<UserProfile?> = try {
        val doc = publicProfiles.document(uid).get().await()
        AppResult.Success(if (doc.exists()) doc.toPublicProfile() else null)
    } catch (t: Throwable) {
        AppResult.Error("فشل جلب الملف الشخصي العام", t)
    }

    /**
     * Writes [updates] to the private "users/{uid}" document (source of truth for everything,
     * including the privacy toggles themselves), then derives a privacy-respecting projection and
     * *overwrites* (not merges) "publicProfiles/{uid}" with only the fields the owner has chosen to
     * show. Using `.set()` without merge here is deliberate: if a user flips a privacy toggle off,
     * the corresponding field must actually disappear from the public document, not just stop being
     * updated while a stale copy lingers from before the toggle was flipped.
     */
    override suspend fun updateMyProfile(uid: String, updates: Map<String, Any?>): AppResult<Unit> = try {
        val privateRef = users.document(uid)
        privateRef.set(updates, com.google.firebase.firestore.SetOptions.merge()).await()

        // Re-read the merged document so we always project from the complete, current state —
        // not just the fields present in this particular update call.
        val merged = privateRef.get().await()
        val profile = merged.toPrivateProfile()

        val publicData = mutableMapOf<String, Any?>(
            "uid" to uid,
            "username" to profile.username,
            "displayName" to profile.displayName,
            "photoUrl" to profile.photoUrl,
            "showLastSeen" to profile.privacy.showLastSeen,
            "status" to if (profile.privacy.showLastSeen) profile.status else "hidden"
        )
        if (profile.privacy.showBio && !profile.bio.isNullOrBlank()) publicData["bio"] = profile.bio
        if (profile.privacy.showBirthDate && !profile.birthDate.isNullOrBlank()) publicData["birthDate"] = profile.birthDate
        if (profile.privacy.showCountry && !profile.country.isNullOrBlank()) publicData["country"] = profile.country

        // Full overwrite (no merge) so toggled-off fields are actually removed, not left stale.
        publicProfiles.document(uid).set(publicData).await()
        AppResult.Success(Unit)
    } catch (t: Throwable) {
        AppResult.Error("فشل تحديث الملف الشخصي", t)
    }

    override suspend fun searchUsers(prefix: String): AppResult<List<Pair<String, String>>> = try {
        if (prefix.isBlank()) {
            AppResult.Success(emptyList())
        } else {
            val end = prefix + '\uf8ff'
            val querySnapshot = usernames
                .whereGreaterThanOrEqualTo("__name__", prefix)
                .whereLessThanOrEqualTo("__name__", end)
                .limit(20).get().await()
            val result = querySnapshot.documents.mapNotNull { doc ->
                val uname = doc.id
                val uid = doc.getString("uid") ?: return@mapNotNull null
                Pair(uname, uid)
            }
            AppResult.Success(result)
        }
    } catch (t: Throwable) {
        AppResult.Error("فشل البحث عن المستخدمين", t)
    }

    /**
     * Uploads via the `uploadProfilePhoto` Firebase Cloud Function (see /functions), which holds the
     * imgbb API key server-side and verifies the caller's Firebase Auth ID token before doing anything.
     * The client never sees, stores, or transmits the imgbb key — closing the "secret embedded in the
     * APK" issue entirely, not just hiding it better.
     */
    override suspend fun uploadProfilePhoto(imageBytes: ByteArray): AppResult<String> {
        return try {
            if (imageBytes.isEmpty()) return AppResult.Error("لا توجد صورة لرفعها")
            if (imageBytes.size > 5 * 1024 * 1024) return AppResult.Error("حجم الصورة كبير جداً (الحد الأقصى 5 ميجابايت)")

            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            val payload = hashMapOf("image" to base64Image)

            val result = functions
                .getHttpsCallable("uploadProfilePhoto")
                .call(payload)
                .await()

            @Suppress("UNCHECKED_CAST")
            val data = result.data as? Map<String, Any?>
            val url = data?.get("url") as? String
                ?: return AppResult.Error("استجابة غير متوقعة من خادم الرفع")

            AppResult.Success(url)
        } catch (t: Throwable) {
            AppResult.Error("فشل رفع الصورة", t)
        }
    }
}
