package com.sila.messaging.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.sila.messaging.data.ChatRepository
import com.sila.messaging.data.Message
import com.sila.messaging.data.UserRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(otherUid: String, onBack: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val me = auth.currentUser?.uid ?: return
    val repo = remember { ChatRepository() }
    val userRepo = remember { UserRepository() }
    val messages = remember { mutableStateOf<List<Message>>(emptyList()) }
    val scope = rememberCoroutineScope()
    var otherName by remember { mutableStateOf(otherUid.take(6)) }

    LaunchedEffect(otherUid) {
        val profile = userRepo.getPublicProfile(otherUid)
        if (profile != null) otherName = profile.username
        val chatId = repo.getOrCreateChat(me, otherUid)
        repo.messagesListener(chatId).collectLatest { list -> messages.value = list }
    }

    var text by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(otherName, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("اكتب رسالة...") },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (text.isBlank()) return@IconButton
                        val toSend = text
                        text = ""
                        scope.launch {
                            val chatId = repo.getOrCreateChat(me, otherUid)
                            repo.sendMessage(chatId, me, toSend)
                        }
                    },
                    modifier = Modifier.size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(24.dp))
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "إرسال", tint = Color.White)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp)
        ) {
            items(messages.value) { msg ->
                val isMe = msg.senderId == me
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .widthIn(max = 260.dp)
                    ) {
                        Text(msg.text, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
