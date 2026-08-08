package com.sila.messaging.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sila.messaging.R
import com.sila.messaging.ui.components.SilaButton

@Composable
fun WelcomeScreen(onNavigateToLogin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Image(painter = painterResource(id = R.drawable.ic_welcome_illustration), contentDescription = null, modifier = Modifier.size(200.dp))
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "مرحباً بك في Sila", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "تطبيق مراسلة آمن وسريع مصمم لحماية خصوصيتك", style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.weight(1f))
        SilaButton(text = "تسجيل الدخول باستخدام Google", onClick = onNavigateToLogin, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "بالمتابعة، أنت توافق على شروط الاستخدام وسياسة الخصوصية",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
    }
}
