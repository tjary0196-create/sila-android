package com.sila.messaging.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sila.messaging.data.AuthViewModel
import com.sila.messaging.data.UserRepository
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavHost(onGoogleSignInClicked: () -> Unit, authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsState()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                isSignedIn = authState.isSignedIn,
                onSignInClick = onGoogleSignInClicked,
                onContinue = {
                    val destination = if (authState.needsUsername) "username" else "chats"
                    navController.navigate(destination) { popUpTo("login") { inclusive = true } }
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
                try { repo.claimUsername(uid, username, displayName, photoUrl) } catch (e: Exception) { false }
            }, onSuccess = {
                navController.navigate("chats") { popUpTo("login") { inclusive = true } }
            })
        }

        composable("chats") {
            ChatsScreen(
                onOpenChat = { otherUid -> navController.navigate("chat/$otherUid") },
                onSearchClick = { navController.navigate("search") },
                onProfileClick = { navController.navigate("profileSettings") }
            )
        }

        composable(
            "chat/{otherUid}",
            arguments = listOf(navArgument("otherUid") { type = NavType.StringType })
        ) { backStackEntry ->
            val otherUid = backStackEntry.arguments?.getString("otherUid") ?: ""
            ChatScreen(otherUid = otherUid, onBack = { navController.popBackStack() })
        }

        composable("search") {
            SearchUsersScreen(
                onStartChat = { otherUid -> navController.navigate("chat/$otherUid") { popUpTo("chats") } },
                onBack = { navController.popBackStack() }
            )
        }

        composable("profileSettings") {
            ProfileSettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
