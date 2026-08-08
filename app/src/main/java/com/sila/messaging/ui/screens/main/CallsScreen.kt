package com.sila.messaging.ui.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
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
import com.sila.messaging.ui.components.EmptyState

/**
 * ⚠️ قيد التطوير — تحتاج ربط WebRTC
 * المكالمات الصوتية/الفيديو الحقيقية تتطلب:
 * - خدمة WebRTC أو طرف ثالث (Agora / Twilio / LiveKit)
 * - تكلفة تشغيلية شهرية
 * - بنية تحتية للـ Signaling Server
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المكالمات", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            EmptyState(
                icon = Icons.Default.Call,
                title = "قيد التطوير",
                subtitle = "ميزة المكالمات تحتاج ربط بـ WebRTC أو خدمة طرف ثالث (Agora/Twilio). هذه الواجهة استعراضية فقط حتى يتم اتخاذ القرار التقني."
            )
        }
    }
}
