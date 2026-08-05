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
import com.google.firebase.auth.FirebaseAuth
import com.sila.messaging.ui.screens.chat.ChatViewModel
import com.sila.messaging.ui.components.SilaAvatar
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sila.messaging.data.chat.FirestoreChatRepository
import com.sila.messaging.data.user.FirestoreUserRepository
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
    
    val viewModel: ChatViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(FirestoreChatRepository(), FirestoreUserRepository(), me, otherUid) as T
        }
    })

    val uiState by viewModel.ui.collectAsState()
    val messages = uiState.messages
    val otherUser = uiState.otherUser
    val otherName = otherUser?.displayName ?: otherUser?.username ?: otherUid.take(6)
    val otherPhotoUrl = otherUser?.photoUrl

    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to the newest message as the conversation grows.
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
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
                            viewModel.sendMessage(text)
                            text = ""
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
        val list = messages

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = SilaSpacing.sm)
        ) {
            itemsIndexed(list, key = { _, msg -> msg.id }) { index, msg ->
                val isMe = msg.senderId == me
                val prev = list.getOrNull(index - 1)
                val next = list.getOrNull(index + 1)

                val isNewDay = isDifferentDay(prev?.createdAtMillis, msg.createdAtMillis)
                val isLastInGroup = next == null ||
                    next.senderId != msg.senderId ||
                    isDifferentDay(msg.createdAtMillis, next.createdAtMillis)
                val isFirstInGroup = prev == null ||
                    prev.senderId != msg.senderId ||
                    isNewDay

                if (isNewDay) {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = SilaSpacing.sm), horizontalArrangement = Arrangement.Center) {
                        SilaDateSeparator(label = formatDayLabel(msg.createdAtMillis?.let { Date(it) }))
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
                        time = msg.createdAtMillis?.let { timeFormatter.format(Date(it)) } ?: "",
                        isMe = isMe,
                        isLastInGroup = isLastInGroup
                    )
                }
            }
        }
    }
}

private fun isDifferentDay(a: Long?, b: Long?): Boolean {
    if (a == null || b == null) return true
    val calA = Calendar.getInstance().apply { time = Date(a) }
    val calB = Calendar.getInstance().apply { time = Date(b) }
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
