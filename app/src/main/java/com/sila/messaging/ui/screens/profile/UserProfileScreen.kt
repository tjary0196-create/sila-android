package com.sila.messaging.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Report
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
import com.sila.messaging.ui.components.SilaAvatar
import com.sila.messaging.ui.components.SilaButton
import com.sila.messaging.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.getUserById(userId).collectAsState(initial = null)
    var showBlockDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ملف مستخدم آخر", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            SilaAvatar(photoUrl = user?.photoUrl, name = user?.displayName ?: "", size = 100)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = user?.displayName ?: "", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = "@${user?.username ?: ""}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = user?.bio ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly) {
                StatItem("126", "محادثة")
                StatItem("48", "الصدقاء")
                StatItem("19", "المجموعات")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                SilaButton(text = "مراسلة", onClick = {
                    viewModel.startChat(userId, user?.username ?: "", user?.photoUrl) { chatId ->
                        chatId?.let { onNavigateToChat(it) }
                    }
                }, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                IconButton(onClick = { showBlockDialog = true }) { Icon(Icons.Default.Block, contentDescription = "Block", tint = MaterialTheme.colorScheme.error) }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = { showBlockDialog = true }) { Text("حظر المستخدم", color = MaterialTheme.colorScheme.error) }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { showReportDialog = true }) { Icon(Icons.Default.Report, contentDescription = "Report") }
                TextButton(onClick = { showReportDialog = true }) { Text("إبلاغ") }
            }
        }

        if (showBlockDialog) {
            AlertDialog(
                onDismissRequest = { showBlockDialog = false },
                title = { Text("حظر المستخدم") },
                text = { Text("هل أنت متأكد من حظر ${user?.displayName}؟ لن يتمكن من مراسلتك.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.blockUser(userId, user?.username ?: "", user?.photoUrl)
                        showBlockDialog = false
                    }) { Text("حظر", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = { TextButton(onClick = { showBlockDialog = false }) { Text("إلغاء") } }
            )
        }

        if (showReportDialog) {
            AlertDialog(
                onDismissRequest = { showReportDialog = false },
                title = { Text("إبلاغ عن مستخدم") },
                text = { Text("سيتم مراجعة البلاغ من فريقنا. هل تريد المتابعة؟") },
                confirmButton = {
                    TextButton(onClick = { showReportDialog = false }) { Text("إرسال البلاغ") }
                },
                dismissButton = { TextButton(onClick = { showReportDialog = false }) { Text("إلغاء") } }
            )
        }
    }
}
