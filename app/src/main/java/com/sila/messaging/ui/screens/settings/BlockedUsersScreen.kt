package com.sila.messaging.ui.screens.settings

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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sila.messaging.data.model.BlockedUser
import com.sila.messaging.ui.components.EmptyState
import com.sila.messaging.ui.components.SilaAvatar
import com.sila.messaging.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedUsersScreen(onNavigateBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val blockedUsers by viewModel.blockedUsers.collectAsState()
    var selectedUser by remember { mutableStateOf<BlockedUser?>(null) }
    var showUnblockDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المحظورون", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        if (blockedUsers.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Block,
                title = "لا يوجد محظورون",
                subtitle = "المستخدمون المحظورون سيظهرون هنا",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(blockedUsers) { user ->
                    BlockedUserItem(
                        user = user,
                        onUnblock = {
                            selectedUser = user
                            showUnblockDialog = true
                        }
                    )
                }
            }
        }

        if (showUnblockDialog) {
            AlertDialog(
                onDismissRequest = { showUnblockDialog = false },
                title = { Text("إلغاء الحظر") },
                text = { Text("هل تريد إلغاء حظر ${selectedUser?.blockedUsername}؟") },
                confirmButton = {
                    TextButton(onClick = {
                        selectedUser?.let { viewModel.unblockUser(it.blockId) }
                        showUnblockDialog = false
                    }) { Text("إلغاء الحظر") }
                },
                dismissButton = { TextButton(onClick = { showUnblockDialog = false }) { Text("إلغاء") } }
            )
        }
    }
}

@Composable
fun BlockedUserItem(user: BlockedUser, onUnblock: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SilaAvatar(photoUrl = user.blockedPhotoUrl, name = user.blockedUsername, size = 48)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = user.blockedUsername, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
        TextButton(onClick = onUnblock) { Text("إلغاء الحظر", color = MaterialTheme.colorScheme.error) }
    }
}
