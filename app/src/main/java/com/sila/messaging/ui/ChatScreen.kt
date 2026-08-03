package com.sila.messaging.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.sila.messaging.data.ChatRepository
import com.sila.messaging.data.Message
import com.sila.messaging.data.UserRepository
import com.sila.messaging.ui.components.SilaAvatar
import com.sila.messaging.ui.components.SilaDateSeparator
import com.sila.messaging.ui.components.SilaMessageBubble
import com.sila.messaging.ui.components.SilaTopBar
import com.sila.messaging.ui.theme.SilaSpacing
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    var otherPhotoUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(otherUid) {
        val profile = userRepo.getPublicProfile(otherUid)
        if (profile != null) {
            otherName = profile.username
            otherPhotoUrl = profile.photoUrl
        }
        val chatId = repo.getOrCreateChat(me, otherUid)
        repo.messagesListener(chatId).collectLatest { list -> messages.value = list }
    }

    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to the newest message as the conversation grows.
    LaunchedEffect(messages.value.size) {
        if (messages.value.isNotEmpty()) {
            listState.animateScrollToItem(messages.value.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            SilaTopBar(
                title = otherName,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    SilaAvatar(
                        name = otherName,
                        photoUrl = otherPhotoUrl,
                        seed = otherUid,
                        size = 34.dp,
                        modifier = Modifier.padding(end = SilaSpacing.sm)
                    )
                },
                containerColor = MaterialTheme.colorScheme.background
            )
        },
        bottomBar = {
            val canSend = text.isNotBlank()
            val sendButtonColor by animateColorAsState(
                targetValue = if (canSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                label = "sendButtonColor"
            )
            val sendIconColor by animateColorAsState(
                targetValue = if (canSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "sendIconColor"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .imePadding()
                    .padding(horizontal = SilaSpacing.sm, vertical = SilaSpacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("اكتب رسالة...") },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(SilaSpacing.xs))
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
                    modifier = Modifier
                        .size(48.dp)
                        .background(sendButtonColor, shape = RoundedCornerShape(24.dp))
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "إرسال", tint = sendIconColor)
                }
            }
        }
    ) { padding ->
        val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
        val list = messages.value

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = SilaSpacing.sm)
        ) {
            itemsIndexed(list, key = { _, msg -> msg.id }) { index, msg ->
                val isMe = msg.senderId == me
                val prev = list.getOrNull(index - 1)
                val next = list.getOrNull(index + 1)

                val isNewDay = isDifferentDay(prev?.createdAt, msg.createdAt)
                val isLastInGroup = next == null ||
                    next.senderId != msg.senderId ||
                    isDifferentDay(msg.createdAt, next.createdAt)
                val isFirstInGroup = prev == null ||
                    prev.senderId != msg.senderId ||
                    isNewDay

                if (isNewDay) {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = SilaSpacing.sm), horizontalArrangement = Arrangement.Center) {
                        SilaDateSeparator(label = formatDayLabel(msg.createdAt?.toDate()))
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (isFirstInGroup) SilaSpacing.xs else 2.dp, bottom = 2.dp),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    SilaMessageBubble(
                        text = msg.text,
                        time = msg.createdAt?.toDate()?.let { timeFormatter.format(it) } ?: "",
                        isMe = isMe,
                        isLastInGroup = isLastInGroup
                    )
                }
            }
        }
    }
}

private fun isDifferentDay(a: Timestamp?, b: Timestamp?): Boolean {
    if (a == null || b == null) return true
    val calA = Calendar.getInstance().apply { time = a.toDate() }
    val calB = Calendar.getInstance().apply { time = b.toDate() }
    return calA.get(Calendar.YEAR) != calB.get(Calendar.YEAR) ||
        calA.get(Calendar.DAY_OF_YEAR) != calB.get(Calendar.DAY_OF_YEAR)
}

private fun formatDayLabel(date: Date?): String {
    if (date == null) return ""
    val today = Calendar.getInstance()
    val target = Calendar.getInstance().apply { time = date }

    val isToday = today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
        today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    if (isToday) return "اليوم"

    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val isYesterday = yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
        yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    if (isYesterday) return "أمس"

    return SimpleDateFormat("d MMMM yyyy", Locale("ar")).format(date)
}
