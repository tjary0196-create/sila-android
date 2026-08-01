package com.sila.messaging.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sila.messaging.data.ChatRepository
import com.sila.messaging.data.Message
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(otherUid: String) {
    val auth = FirebaseAuth.getInstance()
    val me = auth.currentUser?.uid ?: return
    val repo = remember { ChatRepository() }
    val messages = remember { mutableStateOf<List<Message>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(otherUid) {
        val chatId = repo.chatIdFor(me, otherUid)
        repo.messagesListener(chatId).collectLatest { list ->
            messages.value = list
        }
    }

    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages.value) { msg ->
                Text("${if (msg.senderId == me) "Me" else "Them"}: ${msg.text}")
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (text.isBlank()) return@Button
                val chatId = repo.chatIdFor(me, otherUid)
                scope.launch {
                    repo.sendMessage(chatId, me, text)
                }
                text = ""
            }) {
                Text("Send")
            }
        }
    }
}
