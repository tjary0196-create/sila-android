package com.sila.ui.mapper

import com.sila.messaging.domain.chat.ChatMessage
import com.sila.messaging.domain.chat.ChatSummary
import com.sila.messaging.domain.user.UserProfile
import com.sila.model.Chat
import com.sila.model.Message
import com.sila.model.User
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Formats a millisecond timestamp the way the UI expects: "10:30 AM" for today, "Mon"/date otherwise. */
fun formatTimestamp(millis: Long?): String {
    if (millis == null) return ""
    val then = Calendar.getInstance().apply { timeInMillis = millis }
    val now = Calendar.getInstance()
    val sameDay = then.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
        then.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val wasYesterday = then.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
        then.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
    return when {
        sameDay -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(millis))
        wasYesterday -> "Yesterday"
        else -> SimpleDateFormat("EEE", Locale.getDefault()).format(Date(millis))
    }
}

/** Maps a Firestore [UserProfile] to the UI-layer [User] model used across all the existing screens. */
fun UserProfile.toUiUser(): User = User(
    id = uid,
    name = displayName.ifBlank { "@$username" },
    handle = "@$username",
    avatarUrl = photoUrl ?: "",
    isOnline = status == "online",
    lastSeen = if (status != "online") "Last seen recently" else "",
    bio = bio ?: "",
    hasStory = false
)

/**
 * Maps a [ChatSummary] (raw chat doc) to the UI [Chat] model. Needs the peer's resolved profile
 * (fetched separately, since Firestore only stores uids on the chat doc) and the current user's
 * uid to figure out which participant is "the other person".
 */
fun ChatSummary.toUiChat(myUid: String, peerProfile: UserProfile?): Chat {
    val peerUid = participants.firstOrNull { it != myUid } ?: myUid
    val peerUser = peerProfile?.toUiUser() ?: User(
        id = peerUid,
        name = "مستخدم",
        handle = "",
        avatarUrl = "",
        isOnline = false
    )
    return Chat(
        id = chatId,
        user = peerUser,
        lastMessage = lastMessage ?: "",
        timestamp = formatTimestamp(updatedAtMillis)
    )
}

/** Maps a [ChatMessage] to the UI [Message] model. `myUid` decides the left/right bubble side. */
fun ChatMessage.toUiMessage(myUid: String): Message = Message(
    id = id,
    text = text,
    timestamp = formatTimestamp(createdAtMillis),
    isFromMe = senderId == myUid
)
