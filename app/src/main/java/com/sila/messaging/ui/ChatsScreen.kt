package com.sila.messaging.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.sila.messaging.data.Chat
import com.sila.messaging.data.ChatRepository
import com.sila.messaging.data.UserProfile
import com.sila.messaging.data.UserRepository
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class ChatUiState(
    val chat: Chat,
    val otherUid: String,
    val displayName: String,
    val username: String,
    val photoUrl: String?,
    val isOnline: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(onOpenChat: (String) -> Unit, onSearchClick: () -> Unit, onProfileClick: () -> Unit) {
    val repo = remember { ChatRepository() }
    val userRepo = remember { UserRepository() }
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid ?: return

    val rawChats = remember { mutableStateOf<List<Chat>>(emptyList()) }
    val profileCache = remember { mutableStateMapOf<String, UserProfile>() }
    var myPhotoUrl by remember { mutableStateOf<String?>(null) }
    var myDisplayName by remember { mutableStateOf("") }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var screenVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { screenVisible = true }

    LaunchedEffect(uid) {
        repo.chatsForUserListener(uid).collectLatest { list -> rawChats.value = list }
    }

    LaunchedEffect(uid) {
        val profile = userRepo.getUserProfile(uid)
        myPhotoUrl = profile?.photoUrl
        myDisplayName = profile?.displayName ?: ""
    }

    // نجيب بروفايل كل طرف تاني بمحادثة (اسم، يوزرنيم، صورة، وحالة الاتصال الحقيقية)
    LaunchedEffect(rawChats.value) {
        rawChats.value.forEach { chat ->
            val otherUid = chat.participants.firstOrNull { it != uid } ?: return@forEach
            if (!profileCache.containsKey(otherUid)) {
                val profile = userRepo.getPublicProfile(otherUid)
                if (profile != null) profileCache[otherUid] = profile
            }
        }
    }

    val chatUiList = rawChats.value.mapNotNull { chat ->
        val otherUid = chat.participants.firstOrNull { it != uid } ?: return@mapNotNull null
        val profile = profileCache[otherUid]
        val name = profile?.displayName?.takeIf { it.isNotBlank() } ?: profile?.username ?: otherUid.take(6)
        val isOnline = profile?.status == "online" && profile.showLastSeen
        ChatUiState(
            chat = chat,
            otherUid = otherUid,
            displayName = name,
            username = profile?.username ?: "",
            photoUrl = profile?.photoUrl,
            isOnline = isOnline
        )
    }

    val filteredChats = if (searchQuery.isBlank()) {
        chatUiList
    } else {
        chatUiList.filter {
            it.displayName.contains(searchQuery, ignoreCase = true) ||
                it.username.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                TopAppBar(
                    title = { Text("الرسائل", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable { onProfileClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!myPhotoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = myPhotoUrl,
                                    contentDescription = "ملفي الشخصي",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    myDisplayName.take(1).uppercase().ifBlank { "؟" },
                                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                // شريط بحث حديث بحواف مدورة
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicSearchField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            isSearchActive = it.isNotEmpty()
                        },
                        placeholder = "بحث بالمحادثات..."
                    )
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "مسح",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp).clickable {
                                searchQuery = ""
                                isSearchActive = false
                            }
                        )
                    }
                }

                // تبويبات All / Unread / Requests
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
                ) {
                    listOf("الكل", "غير مقروءة", "الطلبات").forEachIndexed { index, label ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(label, fontSize = 13.sp) }
                        )
                    }
                }
                Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSearchClick,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.shadow(elevation = 8.dp, shape = CircleShape)
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "محادثة جديدة", tint = Color.White)
            }
        }
    ) { padding ->
        AnimatedVisibility(
            visible = screenVisible,
            enter = fadeIn(animationSpec = tween(300))
        ) {
            when (selectedTab) {
                1, 2 -> ComingSoonPlaceholder(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    text = if (selectedTab == 1) "ميزة الرسائل غير المقروءة قريباً" else "ميزة طلبات المراسلة قريباً"
                )
                else -> {
                    if (filteredChats.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(padding),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                if (searchQuery.isBlank()) "ما في محادثات بعد" else "ما في نتائج مطابقة",
                                fontSize = 16.sp, color = Color.Gray
                            )
                            if (searchQuery.isBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("دوس على زر التعديل لتبلش محادثة جديدة", fontSize = 13.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.padding(padding)) {
                            itemsIndexed(filteredChats, key = { _, item -> item.chat.id }) { _, item ->
                                ChatRow(item = item, onClick = { onOpenChat(item.otherUid) })
                                Divider(
                                    color = MaterialTheme.colorScheme.outline,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(start = 80.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatRow(item: ChatUiState, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                if (!item.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.photoUrl,
                        contentDescription = item.displayName,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        item.displayName.take(1).uppercase(),
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp
                    )
                }
            }
            if (item.isOnline) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF34C759))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(item.displayName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            if (item.username.isNotBlank()) {
                Text("@${item.username}", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                item.chat.lastMessage?.takeIf { it.isNotBlank() } ?: "ابدأ المحادثة",
                fontSize = 13.sp, color = Color.Gray, maxLines = 1
            )
        }

        Text(
            formatChatTime(item.chat.updatedAt?.toDate()),
            fontSize = 11.sp, color = Color.Gray
        )
    }
}

@Composable
private fun ComingSoonPlaceholder(modifier: Modifier = Modifier, text: String) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text, fontSize = 15.sp, color = Color.Gray)
    }
}

@Composable
private fun BasicSearchField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Box(modifier = Modifier.weight(1f)) {
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            ),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
        )
        if (value.isEmpty()) {
            Text(placeholder, fontSize = 14.sp, color = Color.Gray)
        }
    }
}

private fun formatChatTime(date: Date?): String {
    if (date == null) return ""
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(date)
}
