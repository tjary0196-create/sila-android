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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@Composable
fun FullNameScreen(onNext: () -> Unit, onBack: () -> Unit, viewModel: OnboardingViewModel = hiltViewModel()) {
    var name by remember { mutableStateOf(viewModel.fullName) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "إكمال الملف الشخصي", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "ما هو اسمك؟", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "يجب أن يكون الاسم بين ٢ و٦٠ حرف", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))

        SilaTextField(
            value = name,
            onValueChange = { 
                name = it
                error = null
            },
            label = "الاسم الكامل",
            isError = error != null,
            errorMessage = error
        )

        Spacer(modifier = Modifier.weight(1f))
        SilaButton(
            text = "التالي",
            onClick = {
                when (val result = ValidationUtils.validateFullName(name)) {
                    is com.sila.messaging.util.ValidationResult.Success -> {
                        viewModel.fullName = name
                        onNext()
                    }
                    is com.sila.messaging.util.ValidationResult.Error -> error = result.message
                }
            },
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
