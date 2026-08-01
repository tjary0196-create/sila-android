package com.sila.messaging.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.sila.messaging.data.Chat
import com.sila.messaging.data.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier

@Composable
fun ChatsScreen(onOpenChat: (String) -> Unit) {
    val repo = remember { ChatRepository() }
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid ?: return
    val chats = remember { mutableStateOf<List<Chat>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uid) {
        repo.chatsForUserListener(uid).collectLatest { list ->
            chats.value = list
        }
    }

    Column {
        LazyColumn {
            items(chats.value) { chat ->
                val other = chat.participants.firstOrNull { it != uid } ?: "Unknown"
                Text(
                    text = "${other} — ${chat.lastMessage ?: ""}",
                    modifier = Modifier.clickable { onOpenChat(other) }
                )
            }
        }
    }
}
