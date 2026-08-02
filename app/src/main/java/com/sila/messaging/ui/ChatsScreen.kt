package com.sila.messaging.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.sila.messaging.data.Chat
import com.sila.messaging.data.ChatRepository
import com.sila.messaging.data.UserProfile
import com.sila.messaging.data.UserRepository
import com.sila.messaging.ui.components.SilaAvatar
import com.sila.messaging.ui.components.SilaChatRow
import com.sila.messaging.ui.components.SilaEmptyState
import com.sila.messaging.ui.components.SilaErrorState
import com.sila.messaging.ui.components.SilaLoading
import com.sila.messaging.ui.components.SilaSearchBar
import com.sila.messaging.ui.components.SilaTopBar
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Calendar
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
    var selectedTab by remember { mutableStateOf(0) }
    var screenVisible by remember { mutableStateOf(false) }

    // حالتا التحميل والخطأ (UI فقط) — لا تغيّران مصدر البيانات، فقط تعكسان حالته
    var isLoadingChats by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var reloadTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) { screenVisible = true }

    LaunchedEffect(uid, reloadTrigger) {
        isLoadingChats = true
        loadError = null
        repo.chatsForUserListener(uid)
            .catch { e ->
                loadError = e.localizedMessage ?: "تعذر تحميل المحادثات"
                isLoadingChats = false
            }
            .collectLatest { list ->
                rawChats.value = list
                isLoadingChats = false
            }
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
                SilaTopBar(
                    title = "الرسائل",
                    navigationIcon = {
                        Box(modifier = Modifier.padding(start = 12.dp)) {
                            SilaAvatar(
                                name = myDisplayName,
                                photoUrl = myPhotoUrl,
                                seed = uid,
                                size = 38.dp,
                                onClick = onProfileClick
                            )
                        }
                    },
                    containerColor = Color.Transparent
                )

                SilaSearchBar(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "بحث بالمحادثات...",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
                )

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
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "محادثة جديدة", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        AnimatedVisibility(
            visible = screenVisible,
            enter = fadeIn(animationSpec = tween(300))
        ) {
            when {
                selectedTab == 1 || selectedTab == 2 -> ComingSoonPlaceholder(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    text = if (selectedTab == 1) "ميزة الرسائل غير المقروءة قريباً" else "ميزة طلبات المراسلة قريباً"
                )
                loadError != null -> SilaErrorState(
                    message = loadError ?: "تعذر تحميل المحادثات",
                    modifier = Modifier.fillMaxSize().padding(padding),
                    onRetry = { reloadTrigger++ }
                )
                isLoadingChats -> SilaLoading(
                    modifier = Modifier.fillMaxSize().padding(padding)
                )
                filteredChats.isEmpty() -> {
                    if (searchQuery.isBlank()) {
                        SilaEmptyState(
                            icon = Icons.Filled.Chat,
                            title = "ما في محادثات بعد",
                            subtitle = "دوس على زر التعديل لتبلش محادثة جديدة",
                            modifier = Modifier.fillMaxSize().padding(padding)
                        )
                    } else {
                        SilaEmptyState(
                            icon = Icons.Filled.SearchOff,
                            title = "ما في نتائج مطابقة",
                            modifier = Modifier.fillMaxSize().padding(padding)
                        )
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.padding(padding)) {
                        itemsIndexed(filteredChats, key = { _, item -> item.chat.id }) { _, item ->
                            SilaChatRow(
                                displayName = item.displayName,
                                username = item.username,
                                photoUrl = item.photoUrl,
                                lastMessage = item.chat.lastMessage?.takeIf { it.isNotBlank() } ?: "ابدأ المحادثة",
                                timeLabel = formatChatTime(item.chat.updatedAt?.toDate()),
                                isOnline = item.isOnline,
                                onClick = { onOpenChat(item.otherUid) }
                            )
                            Divider(
                                color = MaterialTheme.colorScheme.outline,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(start = 84.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComingSoonPlaceholder(modifier: Modifier = Modifier, text: String) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * تنسيق وقت آخر رسالة: الساعة لليوم الحالي، "أمس" لأمس، اسم اليوم لآخر أسبوع،
 * وإلا التاريخ الكامل. تحسين عرض فقط، لا يغيّر أي بيانات.
 */
private fun formatChatTime(date: Date?): String {
    if (date == null) return ""
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { time = date }

    val isSameDay = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

    if (isSameDay) {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    }

    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val isYesterday = yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
        yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    if (isYesterday) return "أمس"

    val diffDays = ((now.timeInMillis - target.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
    if (diffDays in 2..6) {
        return SimpleDateFormat("EEEE", Locale("ar")).format(date)
    }

    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
}
