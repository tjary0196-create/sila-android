package com.sila.messaging.ui.screens.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sila.messaging.ui.components.SilaButton
import com.sila.messaging.ui.components.SilaTextField
import com.sila.messaging.ui.viewmodel.OnboardingViewModel
import com.sila.messaging.util.ValidationUtils

@Composable
fun BioScreen(onNext: () -> Unit, onSkip: () -> Unit, onBack: () -> Unit, viewModel: OnboardingViewModel = hiltViewModel()) {
    var bio by remember { mutableStateOf(viewModel.bio) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "إكمال الملف الشخصي", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "أخبرنا عن نفسك", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "اكتب نبذة قصيرة عنك (اختياري)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))

        SilaTextField(
            value = bio,
            onValueChange = { 
                bio = it
                error = null
            },
            label = "نبذة تعريفية",
            isError = error != null,
            errorMessage = error,
            singleLine = false,
            maxLines = 4
        )
        Text(text = "${bio.length}/120", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.weight(1f))
        SilaButton(
            text = "التالي",
            onClick = {
                when (val result = ValidationUtils.validateBio(bio)) {
                    is com.sila.messaging.util.ValidationResult.Success -> {
                        viewModel.bio = bio
                        onNext()
                    }
                    is com.sila.messaging.util.ValidationResult.Error -> error = result.message
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onSkip) { Text("تخطي") }
    }
}
