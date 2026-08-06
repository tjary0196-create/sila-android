package com.sila.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sila.ui.components.SilaAvatar
import com.sila.ui.theme.AccentBlue
import com.sila.ui.theme.BackgroundPrimary
import com.sila.ui.theme.ErrorRed
import com.sila.ui.theme.SurfacePrimary
import com.sila.ui.theme.TextMuted
import com.sila.ui.theme.TextPrimary
import com.sila.ui.theme.TextSecondary

/**
 * Shown once, right after the very first Google sign-in for a uid with no Firestore profile yet.
 * Lets the person pick a unique @username; [onClaim] wires up to [UsernameRepository.claimUsername].
 */
@Composable
fun UsernameSetupScreen(
    displayName: String,
    photoUrl: String?,
    onClaim: (username: String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    var username by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            SilaAvatar(imageUrl = photoUrl, size = 88)
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (displayName.isNotBlank()) "أهلًا $displayName 👋" else "أهلًا فيك 👋",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "اختر اسم مستخدم فريد للتعريف عنك",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it.trim().lowercase() },
                placeholder = { Text("username", color = TextMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfacePrimary,
                    unfocusedContainerColor = SurfacePrimary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            val shownError = errorMessage
            if (shownError != null) {
                Spacer(Modifier.height(8.dp))
                Text(shownError, color = ErrorRed, fontSize = 13.sp, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onClaim(username) },
                enabled = username.length >= 3 && !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("متابعة", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
