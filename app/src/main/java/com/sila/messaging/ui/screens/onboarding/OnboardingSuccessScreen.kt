package com.sila.messaging.ui.screens.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sila.messaging.ui.components.SilaButton
import com.sila.messaging.ui.viewmodel.OnboardingViewModel

@Composable
fun OnboardingSuccessScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // يُنفَّذ الحفظ الفعلي على Firestore لحظة وصول المستخدم لهذه الشاشة —
    // قبل هذا التعديل كانت completeOnboarding() لا تُستدعى من أي مكان بالتطبيق إطلاقًا.
    LaunchedEffect(Unit) {
        viewModel.completeOnboarding()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        when {
            isLoading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "جارِ حفظ ملفك الشخصي...", style = MaterialTheme.typography.bodyLarge)
            }
            error != null -> {
                Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null,
                    modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "تعذّر حفظ ملفك الشخصي", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error ?: "", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(24.dp))
                SilaButton(text = "إعادة المحاولة", onClick = { viewModel.completeOnboarding() }, modifier = Modifier.fillMaxWidth())
            }
            else -> {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null,
                    modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "تم الإعداد بنجاح!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "تم حفظ معلوماتك بنجاح. يمكنك الآن بدء استخدام Sila.",
                    style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        if (!isLoading && error == null) {
            SilaButton(text = "ابدأ المحادثة", onClick = onFinish, modifier = Modifier.fillMaxWidth())
        }
    }
}
