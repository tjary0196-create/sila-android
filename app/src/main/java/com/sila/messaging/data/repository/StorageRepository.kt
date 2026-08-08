package com.sila.messaging.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) {
    private val currentUid: String? get() = auth.currentUser?.uid

    /**
     * رفع صورة الملف الشخصي
     */
    suspend fun uploadProfilePhoto(uri: Uri): Result<String> {
        val uid = currentUid ?: return Result.failure(Exception("Not logged in"))
        return try {
            val ref = storage.reference.child("profile_photos/$uid/${UUID.randomUUID()}.jpg")
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * رفع صورة في المحادثة
     */
    suspend fun uploadChatImage(chatId: String, uri: Uri): Result<String> {
        val uid = currentUid ?: return Result.failure(Exception("Not logged in"))
        return try {
            val ref = storage.reference.child("chat_images/$chatId/$uid/${UUID.randomUUID()}.jpg")
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * رفع ملف في المحادثة
     */
    suspend fun uploadChatFile(chatId: String, uri: Uri, fileName: String): Result<Pair<String, String>> {
        val uid = currentUid ?: return Result.failure(Exception("Not logged in"))
        return try {
            val ref = storage.reference.child("chat_files/$chatId/$uid/$fileName")
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl to fileName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * رفع رسالة صوتية
     */
    suspend fun uploadVoiceMessage(chatId: String, uri: Uri): Result<String> {
        val uid = currentUid ?: return Result.failure(Exception("Not logged in"))
        return try {
            val ref = storage.reference.child("voice_messages/$chatId/$uid/${UUID.randomUUID()}.aac")
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * حذف ملف
     */
    suspend fun deleteFile(url: String): Result<Unit> {
        return try {
            val ref = storage.getReferenceFromUrl(url)
            ref.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
