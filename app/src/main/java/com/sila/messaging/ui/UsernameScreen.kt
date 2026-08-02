package com.sila.messaging.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun UsernameScreen(
    onClaim: suspend (String) -> Boolean,
    onSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center) {
        Text("اختر اسم مستخدم", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("هيك رح يقدر أصدقاؤك يلاقوك", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it; error = null },
            label = { Text("اسم المستخدم") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (username.length < 3) {
                    error = "اسم المستخدم لازم يكون 3 أحرف على الأقل"
                    return@Button
                }
                error = null
                isLoading = true
                scope.launch {
                    try {
                        val claimed = onClaim(username.trim())
                        if (claimed) onSuccess() else error = "اسم المستخدم هذا محجوز، جرب اسم تاني"
                    } catch (e: Exception) {
                        error = "صار خطأ، حاول مرة تانية"
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp)
        ) { Text(if (isLoading) "جارٍ الحفظ..." else "متابعة", fontSize = 16.sp) }

        if (error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }
    }
}
