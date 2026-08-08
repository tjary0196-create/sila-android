package com.sila.messaging.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.PhoneAndroid
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
import com.sila.messaging.data.model.Session
import com.sila.messaging.ui.components.EmptyState
import com.sila.messaging.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveSessionsScreen(onNavigateBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val sessions by viewModel.activeSessions.collectAsState()
    var showTerminateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الأجهزة النشطة", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        if (sessions.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Computer,
                title = "لا توجد أجهزة نشطة",
                subtitle = "الأجهزة المتصلة ستظهر هنا",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(sessions) { session ->
                    SessionItem(session = session, onTerminate = { showTerminateDialog = true })
                }
            }
        }

        if (showTerminateDialog) {
            AlertDialog(
                onDismissRequest = { showTerminateDialog = false },
                title = { Text("تسجيل الخروج من الأجهزة الأخرى") },
                text = { Text("هل تريد تسجيل الخروج من جميع الأجهزة الأخرى؟") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.terminateAllOtherSessions()
                        showTerminateDialog = false
                    }) { Text("تسجيل الخروج", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = { TextButton(onClick = { showTerminateDialog = false }) { Text("إلغاء") } }
            )
        }
    }
}

@Composable
fun SessionItem(session: Session, onTerminate: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (session.deviceName.contains("iPhone") || session.deviceName.contains("Android")) 
                Icons.Default.PhoneAndroid else Icons.Default.Computer,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = session.deviceName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = "${session.deviceModel} • ${session.lastActive.toDate()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (session.isCurrent) {
                Text(text = "هذا الجهاز", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
            }
        }
        if (!session.isCurrent) {
            TextButton(onClick = onTerminate) { Text("تسجيل خروج", color = MaterialTheme.colorScheme.error) }
        }
    }
}
