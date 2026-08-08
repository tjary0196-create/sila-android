package com.sila.messaging.ui.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sila.messaging.data.model.MessageRequest
import com.sila.messaging.ui.components.EmptyState
import com.sila.messaging.ui.components.SilaAvatar
import com.sila.messaging.ui.viewmodel.ChatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageRequestsScreen(
    onNavigateBack: () -> Unit,
    onOpenChat: (String) -> Unit = {},
    viewModel: ChatsViewModel = hiltViewModel()
) {
    val requests by viewModel.messageRequests.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("طلبات الرسائل", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        if (requests.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Check,
                title = "لا توجد طلبات",
                subtitle = "الطلبات غير مقروءة حتى تقبلها",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(requests) { request ->
                    RequestItem(
                        request = request,
                        onAccept = { viewModel.acceptRequest(request.requestId) { chatId -> onOpenChat(chatId) } },
                        onDecline = { viewModel.declineRequest(request.requestId) }
                    )
                }
            }
        }
    }
}

@Composable
fun RequestItem(request: MessageRequest, onAccept: () -> Unit, onDecline: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SilaAvatar(photoUrl = null, name = request.fromUid, size = 48)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = request.fromUid, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = request.message ?: "مرحباً، أود إمكانية التواصل معك", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        OutlinedButton(onClick = onAccept) { Text("قبول") }
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedButton(onClick = onDecline) { Text("حذف") }
    }
}
