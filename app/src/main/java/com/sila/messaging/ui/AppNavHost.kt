package com.sila.messaging.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.Column
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.navArgument
import com.sila.messaging.data.AuthViewModel
import androidx.compose.runtime.rememberCoroutineScope
import com.sila.messaging.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(onGoogleSignInClicked: () -> Unit, authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                isSignedIn = authState.isSignedIn,
                onSignInClick = onGoogleSignInClicked,
                onContinue = {
                    val destination = if (authState.needsUsername) "username" else "chats"
                    navController.navigate(destination) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                errorMessage = authState.errorMessage
            )
        }

        composable("username") {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val uid = currentUser?.uid ?: ""
            val displayName = currentUser?.displayName
            val photoUrl = currentUser?.photoUrl?.toString()

            UsernameScreen(onClaim = { username ->
                val repo = UserRepository()
                try {
                    repo.claimUsername(uid, username, displayName, photoUrl)
                } catch (e: Exception) {
                    false
                }
            }, onSuccess = {
                navController.navigate("chats") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }

        composable("chats") {
            ChatsScreen(
                onOpenChat = { otherUid ->
                    navController.navigate("chat/$otherUid")
                }
            )
        }

        composable(
            "chat/{otherUid}",
            arguments = listOf(navArgument("otherUid") { type = NavType.StringType })
        ) { backStackEntry ->
            val otherUid = backStackEntry.arguments?.getString("otherUid") ?: ""
            ChatScreen(otherUid = otherUid)
        }

        composable("search") {
            SearchUsersScreen(onStartChat = { otherUid ->
                navController.navigate("chat/$otherUid")
            })
        }
    }
}
