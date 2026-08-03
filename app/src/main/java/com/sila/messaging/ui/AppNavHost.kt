package com.sila.messaging.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sila.messaging.data.AuthViewModel
import com.sila.messaging.data.UserRepository
import com.google.firebase.auth.FirebaseAuth

/**
 * Sila's shared motion language for screen-to-screen navigation:
 *  - "push" destinations (drilling deeper, e.g. opening a chat) slide in from
 *    the leading edge over a soft fade, and slide back out on pop — this is
 *    what makes navigation feel directional instead of a flat cut.
 *  - "modal" destinations (search, profile settings, onboarding steps) rise
 *    in with a gentle scale + fade, like a sheet taking focus, rather than
 *    sliding — signalling "this sits on top" instead of "this is deeper".
 * Durations are tuned short (260–360ms) with FastOutSlowIn so the app feels
 * snappy rather than sluggish on repeated navigation.
 */
private const val DURATION = 320

private fun AnimatedContentTransitionScope<NavBackStackEntry>.pushEnter() =
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(DURATION, easing = FastOutSlowInEasing)
    ) + fadeIn(tween(DURATION))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.pushExit() =
    slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(DURATION, easing = FastOutSlowInEasing)
    ) + fadeOut(tween(DURATION / 2))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnter() =
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(DURATION, easing = FastOutSlowInEasing)
    ) + fadeIn(tween(DURATION))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popExit() =
    slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(DURATION, easing = FastOutSlowInEasing)
    ) + fadeOut(tween(DURATION / 2))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.modalEnter() =
    scaleIn(
        initialScale = 0.92f,
        animationSpec = tween(DURATION, easing = FastOutSlowInEasing)
    ) + fadeIn(tween(DURATION))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.modalExit() =
    scaleOut(
        targetScale = 0.92f,
        animationSpec = tween(DURATION / 2)
    ) + fadeOut(tween(DURATION / 2))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.fadeThrough() =
    fadeIn(tween(DURATION, easing = FastOutSlowInEasing))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.fadeThroughExit() =
    fadeOut(tween(DURATION / 2))

@Composable
fun AppNavHost(onGoogleSignInClicked: () -> Unit, authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsState()

    NavHost(navController = navController, startDestination = "login") {
        composable(
            "login",
            enterTransition = { fadeThrough() },
            exitTransition = { fadeThroughExit() }
        ) {
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

        composable(
            "username",
            enterTransition = { modalEnter() },
            exitTransition = { modalExit() }
        ) {
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

        composable(
            "chats",
            enterTransition = { fadeThrough() },
            exitTransition = { pushExit() },
            popEnterTransition = { popEnter() }
        ) {
            ChatsScreen(
                onOpenChat = { otherUid -> navController.navigate("chat/$otherUid") },
                onSearchClick = { navController.navigate("search") },
                onProfileClick = { navController.navigate("profileSettings") }
            )
        }

        composable(
            "chat/{otherUid}",
            arguments = listOf(navArgument("otherUid") { type = NavType.StringType }),
            enterTransition = { pushEnter() },
            exitTransition = { fadeThroughExit() },
            popExitTransition = { popExit() }
        ) { backStackEntry ->
            val otherUid = backStackEntry.arguments?.getString("otherUid") ?: ""
            ChatScreen(otherUid = otherUid, onBack = { navController.popBackStack() })
        }

        composable(
            "search",
            enterTransition = { modalEnter() },
            exitTransition = { modalExit() },
            popExitTransition = { modalExit() }
        ) {
            SearchUsersScreen(
                onStartChat = { otherUid -> navController.navigate("chat/$otherUid") { popUpTo("chats") } },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            "profileSettings",
            enterTransition = { modalEnter() },
            exitTransition = { modalExit() },
            popExitTransition = { modalExit() }
        ) {
            ProfileSettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
