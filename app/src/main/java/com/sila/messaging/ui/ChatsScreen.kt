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
import com.sila.messaging.ui.screens.chats.ChatsViewModel
import com.sila.messaging.ui.screens.chats.ChatUiState
import com.sila.messaging.ui.components.SilaAvatar
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sila.messaging.data.chat.FirestoreChatRepository
import com.sila.messaging.data.user.FirestoreUserRepository
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(onOpenChat: (String) -> Unit, onSearchClick: () -> Unit, onProfileClick: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid ?: return

    val viewModel: ChatsViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ChatsViewModel(FirestoreChatRepository(), FirestoreUserRepository(), uid) as T
        }
    })

    val uiState by viewModel.ui.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    var screenVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { screenVisible = true }

    val filteredChats = if (searchQuery.isBlank()) {
        uiState.chats
    } else {
        uiState.chats.filter {
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
                                    name = uiState.myProfile?.displayName ?: "",
                                    photoUrl = uiState.myProfile?.photoUrl,
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
                uiState.error != null -> SilaErrorState(
                    message = uiState.error ?: "تعذر تحميل المحادثات",
                    modifier = Modifier.fillMaxSize().padding(padding),
                    onRetry = { viewModel.retry() }
                )
                uiState.isLoading -> SilaLoading(
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
                        itemsIndexed(filteredChats, key = { _, item -> item.chatId }) { _, item ->
                            SilaChatRow(
                                displayName = item.displayName,
                                username = item.username,
                                photoUrl = item.photoUrl,
                                lastMessage = item.lastMessage?.takeIf { it.isNotBlank() } ?: "ابدأ المحادثة",
                                timeLabel = formatChatTime(item.updatedAtMillis?.let { java.util.Date(it) }),
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
