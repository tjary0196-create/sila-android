package com.sila.messaging

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.sila.messaging.data.AuthViewModel
import com.sila.messaging.ui.AppNavHost
import com.sila.messaging.ui.theme.SilaTheme

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private var authViewModel: AuthViewModel? = null

    private val googleLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account == null) {
                    authViewModel?.onSignInFailed("Google account is null")
                    return@registerForActivityResult
                }
                val idToken = account.idToken
                if (idToken == null) {
                    authViewModel?.onSignInFailed("No idToken from GoogleSignIn")
                    return@registerForActivityResult
                }
                authViewModel?.signInWithGoogle(idToken)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                authViewModel?.onSignInFailed(e.localizedMessage ?: "Google sign-in failed")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SilaTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val vm: AuthViewModel = viewModel()
                    authViewModel = vm

                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(vm.getWebClientId())
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(this, gso)

                    AppNavHost(
                        onGoogleSignInClicked = {
                            val intent = googleSignInClient.signInIntent
                            googleLauncher.launch(intent)
                        },
                        authViewModel = vm
                    )
                }
            }
        }
    }
}
