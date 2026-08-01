package com.sila.messaging.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Choose username") }
        )
        Button(onClick = {
            if (username.length < 3) {
                error = "Username must be at least 3 characters"
                return@Button
            }
            error = null
            isLoading = true
            scope.launch {
                try {
                    val claimed = onClaim(username.trim())
                    if (claimed) {
                        onSuccess()
                    } else {
                        error = "Username is already taken. Please choose another."
                    }
                } catch (e: Exception) {
                    error = "Failed to claim username: ${e.localizedMessage ?: "unknown error"}"
                } finally {
                    isLoading = false
                }
            }
        }, modifier = Modifier.padding(top = 12.dp)) {
            Text(if (isLoading) "Claiming..." else "Claim username")
        }
        if (error != null) {
            Text(error!!)
        }
    }
}
