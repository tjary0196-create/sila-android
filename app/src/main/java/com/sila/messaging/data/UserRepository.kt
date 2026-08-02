package com.sila.messaging.data

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

data class UserProfile(
    val uid: String = "",
    val username: String = "",
    val displayName: String? = null,
    val photoUrl: String? = null,
    val bio: String? = null,
    val birthDate: String? = null,
    val country: String? = null,
    val status: String = "online",
    val showBio: Boolean = true,
    val showBirthDate: Boolean = true,
    val showCountry: Boolean = true,
    val showLastSeen: Boolean = true,
    val createdAt: com.google.firebase.Timestamp? = null
)

class UserRepository {
    private val firestore = Firebase.firestore
    private val usersColl = firestore.collection("users")
    private val usernamesColl = firestore.collection("usernames")
    private val publicProfilesColl = firestore.collection("publicProfiles")
    private val httpClient = OkHttpClient()

    suspend fun getUserProfile(uid: String): UserProfile? {
        val doc = usersColl.document(uid).get().await()
        return if (doc.exists()) doc.toObject(UserProfile::class.java) else null
    }

    suspend fun getPublicProfile(uid: String): UserProfile? {
        val doc = publicProfilesColl.document(uid).get().await()
        return if (doc.exists()) doc.toObject(UserProfile::class.java) else null
    }

    suspend fun claimUsername(uid: String, username: String, displayName: String?, photoUrl: String?): Boolean {
        try {
            firestore.runTransaction { transaction ->
                val usernameDocRef = usernamesColl.document(username)
                val usernameSnapshot = transaction.get(usernameDocRef)
                if (usernameSnapshot.exists()) throw IllegalStateException("username_taken")
                transaction.set(usernameDocRef, mapOf("uid" to uid, "createdAt" to FieldValue.serverTimestamp()))
                val userRef = usersColl.document(uid)
                transaction.set(userRef, mapOf(
                    "uid" to uid, "username" to username,
                    "displayName" to (displayName ?: ""), "photoUrl" to (photoUrl ?: ""),
                    "createdAt" to FieldValue.serverTimestamp()
                ))
                val publicRef = publicProfilesColl.document(uid)
                transaction.set(publicRef, mapOf(
                    "uid" to uid, "username" to username,
                    "displayName" to (displayName ?: ""), "photoUrl" to (photoUrl ?: ""),
                    "createdAt" to FieldValue.serverTimestamp()
                ))
                null
            }.await()
            return true
        } catch (e: Exception) {
            Log.w("UserRepository", "claimUsername failed: ${e.localizedMessage}")
            return false
        }
    }

    suspend fun searchByPrefix(prefix: String, limit: Long = 20): List<Pair<String, String>> {
        if (prefix.isBlank()) return emptyList()
        val end = prefix + '\uf8ff'
        val querySnapshot = usernamesColl
            .whereGreaterThanOrEqualTo("__name__", prefix)
            .whereLessThanOrEqualTo("__name__", end)
            .limit(limit).get().await()
        val result = mutableListOf<Pair<String, String>>()
        for (doc in querySnapshot.documents) {
            val uname = doc.id
            val uid = doc.getString("uid") ?: continue
            result.add(Pair(uname, uid))
        }
        return result
    }

    suspend fun updateProfile(uid: String, updates: Map<String, Any?>) {
        usersColl.document(uid).set(updates, SetOptions.merge()).await()
        publicProfilesColl.document(uid).set(updates, SetOptions.merge()).await()
    }

    suspend fun uploadProfilePhoto(context: Context, uri: Uri, apiKey: String): String {
        if (apiKey.isBlank()) throw IllegalStateException("imgbb_api_key_missing")
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("cannot_read_image")
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

        val requestBody = FormBody.Builder()
            .add("key", apiKey)
            .add("image", base64)
            .build()
        val request = Request.Builder()
            .url("https://api.imgbb.com/1/upload")
            .post(requestBody)
            .build()

        return suspendCancellableCoroutine { cont ->
            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cont.resumeWith(Result.failure(e))
                }
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val body = response.body?.string() ?: throw IllegalStateException("empty_response")
                        val json = JSONObject(body)
                        if (!json.optBoolean("success", false)) throw IllegalStateException("upload_failed")
                        val url = json.getJSONObject("data").getString("url")
                        cont.resumeWith(Result.success(url))
                    } catch (e: Exception) {
                        cont.resumeWith(Result.failure(e))
                    }
                }
            })
        }
    }
}
