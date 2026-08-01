package com.sila.messaging

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.sila.messaging.ui.AppNavHost
import com.sila.messaging.ui.theme.SilaTheme
import com.sila.messaging.data.AuthViewModel
import com.google.android.gms.common.api.ApiException
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SilaTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val authViewModel: AuthViewModel = viewModel()
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(authViewModel.getWebClientId())
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(this, gso)

                    val googleLauncher = registerForActivityResult(
                        ActivityResultContracts.StartActivityForResult(),
                        ActivityResultCallback<ActivityResult> { result ->
                            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                            try {
                                val account = task.getResult(ApiException::class.java)
                                if (account == null) {
                                    authViewModel.onSignInFailed("Google account is null")
                                    return@ActivityResultCallback
                                }
                                val idToken = account.idToken
                                if (idToken == null) {
                                    authViewModel.onSignInFailed("No idToken from GoogleSignIn")
                                    return@ActivityResultCallback
                                }
                                authViewModel.signInWithGoogle(idToken)
                            } catch (e: ApiException) {
                                Log.w(TAG, "Google sign in failed", e)
                                authViewModel.onSignInFailed(e.localizedMessage ?: "Google sign-in failed")
                            }
                        }
                    )

                    AppNavHost(
                        onGoogleSignInClicked = {
                            val intent = googleSignInClient.signInIntent
                            googleLauncher.launch(intent)
                        },
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}
