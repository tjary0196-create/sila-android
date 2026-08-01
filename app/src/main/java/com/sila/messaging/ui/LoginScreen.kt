package com.sila.messaging.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LoginScreen(
    isSignedIn: Boolean,
    onSignInClick: () -> Unit,
    onContinue: () -> Unit,
    errorMessage: String?
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isSignedIn) {
            Button(onClick = onContinue) {
                Text("Continue")
            }
        } else {
            Button(onClick = onSignInClick) {
                Text("Sign in with Google")
            }
        }
        if (!errorMessage.isNullOrEmpty()) {
            Text("Error: $errorMessage")
        }
    }
}
