package com.sila.messaging.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.collectAsState
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
import com.sila.messaging.ui.viewmodel.ProfileViewModel
import com.sila.messaging.util.ValidationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onNavigateBack: () -> Unit, viewModel: ProfileViewModel = hiltViewModel()) {
    val user by viewModel.user.collectAsState()
    var displayName by remember { mutableStateOf(user?.displayName ?: "") }
    var username by remember { mutableStateOf(user?.username ?: "") }
    var bio by remember { mutableStateOf(user?.bio ?: "") }
    var phone by remember { mutableStateOf(user?.phoneNumber ?: "") }
    var country by remember { mutableStateOf(user?.country ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تعديل الملف الشخصي", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            SilaTextField(value = displayName, onValueChange = { displayName = it }, label = "الاسم")
            Spacer(modifier = Modifier.height(12.dp))
            SilaTextField(value = username, onValueChange = { username = it }, label = "اسم المستخدم", enabled = false)
            Spacer(modifier = Modifier.height(12.dp))
            SilaTextField(value = bio, onValueChange = { bio = it }, label = "النبذة التعريفية", singleLine = false, maxLines = 3)
            Spacer(modifier = Modifier.height(12.dp))
            SilaTextField(value = phone, onValueChange = { phone = it }, label = "رقم الهاتف", keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone)
            Spacer(modifier = Modifier.height(12.dp))
            SilaTextField(value = country, onValueChange = { country = it }, label = "الدولة")

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(24.dp))
            SilaButton(
                text = "حفظ",
                onClick = {
                    when {
                        displayName.isBlank() -> error = "الاسم مطلوب"
                        phone.isNotBlank() && ValidationUtils.validatePhone(phone) is com.sila.messaging.util.ValidationResult.Error -> 
                            error = "رقم الهاتف غير صالح"
                        else -> {
                            error = null
                            viewModel.updateProfile(displayName, bio, phone, country)
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
