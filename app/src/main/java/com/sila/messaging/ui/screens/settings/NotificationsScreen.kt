package com.sila.messaging.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * ⚠️ Firebase Cloud Messaging غير مفعل حالياً
 * لتفعيل الإشعارات:
 * 1. أضف com.google.firebase:firebase-messaging إلى build.gradle
 * 2. أنشئ FirebaseMessagingService
 * 3. اطلب إذن POST_NOTIFICATIONS (Android 13+)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإشعارات", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            ToggleItem("تمكين الإشعارات", false) { }
            ToggleItem("إشعارات الرسائل", false) { }
            ToggleItem("إشعارات المجموعات", false) { }
            ToggleItem("الإشعارات والتنبيهات", false) { }
            ToggleItem("إشعارات المكالمات", false) { }
        }
    }
}
