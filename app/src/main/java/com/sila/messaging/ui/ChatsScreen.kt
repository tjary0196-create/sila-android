package com.sila.messaging.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.sila.messaging.data.Chat
import com.sila.messaging.data.ChatRepository
import com.sila.messaging.data.UserRepository
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(onOpenChat: (String) -> Unit, onSearchClick: () -> Unit) {
    val repo = remember { ChatRepository() }
    val userRepo = remember { UserRepository() }
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid ?: return
    val chats = remember { mutableStateOf<List<Chat>>(emptyList()) }

    LaunchedEffect(uid) {
        repo.chatsForUserListener(uid).collectLatest { list -> chats.value = list }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("صلة", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onSearchClick, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Filled.Search, contentDescription = "بحث", tint = Color.White)
            }
        }
    ) { padding ->
        if (chats.value.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("ما في محادثات بعد", fontSize = 16.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text("دوس على زر البحث لتبلش محادثة جديدة", fontSize = 13.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(chats.value) { chat ->
                    val otherUid = chat.participants.firstOrNull { it != uid } ?: "Unknown"
                    var displayName by remember(otherUid) { mutableStateOf(otherUid.take(6)) }

                    LaunchedEffect(otherUid) {
                        val profile = userRepo.getPublicProfile(otherUid)
                        if (profile != null) displayName = profile.username
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { onOpenChat(otherUid) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(displayName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(displayName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text(
                                chat.lastMessage?.takeIf { it.isNotBlank() } ?: "ابدأ المحادثة",
                                fontSize = 13.sp, color = Color.Gray, maxLines = 1
                            )
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                }
            }
        }
    }
}
