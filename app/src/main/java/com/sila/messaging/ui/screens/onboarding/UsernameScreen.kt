package com.sila.messaging.ui.screens.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sila.messaging.ui.components.SilaButton
import com.sila.messaging.ui.components.SilaTextField
import com.sila.messaging.ui.viewmodel.OnboardingViewModel
import com.sila.messaging.util.ValidationUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UsernameScreen(onNext: () -> Unit, onBack: () -> Unit, viewModel: OnboardingViewModel = hiltViewModel()) {
    var username by remember { mutableStateOf(viewModel.username) }
    var error by remember { mutableStateOf<String?>(null) }
    var isChecking by remember { mutableStateOf(false) }
    var isAvailable by remember { mutableStateOf<Boolean?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "إكمال الملف الشخصي", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "اختر اسم مستخدم فريد", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "يمكنك استخدامه للتواصل مع الآخرين", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))

        SilaTextField(
            value = username,
            onValueChange = { 
                username = it
                error = null
                isAvailable = null
                // Debounce check
                scope.launch {
                    delay(500)
                    if (username.length >= 3) {
                        isChecking = true
                        val result = viewModel.checkUsername(username)
                        isAvailable = result
                        isChecking = false
                    }
                }
            },
            label = "اسم المستخدم",
            isError = error != null || isAvailable == false,
            errorMessage = when {
                error != null -> error
                isAvailable == false -> "اسم المستخدم غير متاح"
                else -> null
            },
            trailingIcon = {
                if (isChecking) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else if (isAvailable == true) Text("متاح", color = MaterialTheme.colorScheme.tertiary)
            }
        )

        Spacer(modifier = Modifier.weight(1f))
        SilaButton(
            text = "التالي",
            onClick = {
                when (val result = ValidationUtils.validateUsername(username)) {
                    is com.sila.messaging.util.ValidationResult.Success -> {
                        viewModel.username = username
                        onNext()
                    }
                    is com.sila.messaging.util.ValidationResult.Error -> error = result.message
                }
            },
            enabled = username.isNotBlank() && isAvailable == true && !isChecking,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
