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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.sila.BuildConfig
import com.sila.ui.theme.AccentBlue
import com.sila.ui.theme.BackgroundPrimary
import com.sila.ui.theme.ErrorRed
import com.sila.ui.theme.TextPrimary
import com.sila.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/**
 * The very first screen an unauthenticated user sees. Signs in with Google via Credential Manager
 * and hands the resulting ID token up to [onSignIn], which forwards it to [AuthRepository]
 * (Firebase's `signInWithCredential`). Requires `BuildConfig.FIREBASE_WEB_CLIENT_ID` to be set —
 * see `app/build.gradle.kts` / `local.properties`.
 */
@Composable
fun LoginScreen(
    onSignIn: (idToken: String) -> Unit,
    isSigningIn: Boolean,
    errorMessage: String?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var localError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "سيلا",
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "مراسلة بسيطة وآمنة",
                fontSize = 15.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(56.dp))

            Button(
                onClick = {
                    localError = null
                    scope.launch {
                        try {
                            val credentialManager = CredentialManager.create(context)
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(BuildConfig.FIREBASE_WEB_CLIENT_ID)
                                .build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()
                            val response = credentialManager.getCredential(context, request)
                            val credential = response.credential
                            if (credential is CustomCredential &&
                                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                            ) {
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                onSignIn(googleIdTokenCredential.idToken)
                            } else {
                                localError = "نوع بيانات اعتماد غير متوقع من Google"
                            }
                        } catch (e: GetCredentialException) {
                            android.util.Log.e("SilaAuth", "GetCredentialException: ${e.message}", e)
                            android.util.Log.e("SilaAuth", "webClientId used = [${BuildConfig.FIREBASE_WEB_CLIENT_ID}]")
                            localError = "خطأ في تسجيل الدخول: ${e.message ?: "حاول مرة أخرى"} | webClientId=[${BuildConfig.FIREBASE_WEB_CLIENT_ID}]"
                        } catch (e: Exception) {
                            android.util.Log.e("SilaAuth", "Unexpected error: ${e.message}", e)
                            localError = "خطأ غير متوقع: ${e.message}"
                        }
                    }
                },
                enabled = !isSigningIn,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                if (isSigningIn) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "المتابعة عبر Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            val shownError = errorMessage ?: localError
            if (shownError != null) {
                Spacer(Modifier.height(16.dp))
                Text(text = shownError, color = ErrorRed, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        }
    }
}
