package com.sila.messaging.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.sila.messaging.ui.screens.auth.LoginScreen
import com.sila.messaging.ui.screens.auth.SplashScreen
import com.sila.messaging.ui.screens.auth.WelcomeScreen
import com.sila.messaging.ui.screens.main.CallsScreen
import com.sila.messaging.ui.screens.main.ChatScreen
import com.sila.messaging.ui.screens.main.ChatsScreen
import com.sila.messaging.ui.screens.main.MessageRequestsScreen
import com.sila.messaging.ui.screens.onboarding.*
import com.sila.messaging.ui.screens.profile.EditProfileScreen
import com.sila.messaging.ui.screens.profile.MyProfileScreen
import com.sila.messaging.ui.screens.profile.UserProfileScreen
import com.sila.messaging.ui.screens.settings.*
import com.sila.messaging.ui.viewmodel.OnboardingViewModel

private const val ONBOARDING_GRAPH_ROUTE = "onboarding_graph"

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object ProfilePhoto : Screen("onboarding/photo")
    object FullName : Screen("onboarding/name")
    object Username : Screen("onboarding/username")
    object Bio : Screen("onboarding/bio")
    object Language : Screen("onboarding/language")
    object OnboardingSuccess : Screen("onboarding/success")
    object Chats : Screen("chats")
    object Chat : Screen("chat/{chatId}") {
        fun createRoute(chatId: String) = "chat/$chatId"
    }
    object MessageRequests : Screen("message_requests")
    object MyProfile : Screen("my_profile")
    object EditProfile : Screen("edit_profile")
    object UserProfile : Screen("user_profile/{userId}") {
        fun createRoute(userId: String) = "user_profile/$userId"
    }
    object Settings : Screen("settings")
    object PrivacySecurity : Screen("privacy_security")
    object BlockedUsers : Screen("blocked_users")
    object LanguageSettings : Screen("language_settings")
    object ActiveSessions : Screen("active_sessions")
    object Notifications : Screen("notifications")
    object Calls : Screen("calls")
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Splash.route
) {
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToWelcome = { navController.navigate(Screen.Welcome.route) { popUpTo(Screen.Splash.route) { inclusive = true } } },
                onNavigateToChats = { navController.navigate(Screen.Chats.route) { popUpTo(Screen.Splash.route) { inclusive = true } } },
                onNavigateToOnboarding = { navController.navigate(ONBOARDING_GRAPH_ROUTE) { popUpTo(Screen.Splash.route) { inclusive = true } } }
            )
        }
        composable(Screen.Welcome.route) {
            WelcomeScreen(onNavigateToLogin = { navController.navigate(Screen.Login.route) })
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Screen.Chats.route) { popUpTo(Screen.Welcome.route) { inclusive = true } } },
                onNewUser = { navController.navigate(ONBOARDING_GRAPH_ROUTE) { popUpTo(Screen.Welcome.route) { inclusive = true } } }
            )
        }
        // كل شاشات onboarding تشترك بنفس نسخة OnboardingViewModel (مربوطة بالـ graph نفسه) —
        // بدون هيك، كانت كل شاشة تاخد نسخة منفصلة وتضيع البيانات (اسم/يوزرنيم/بايو) بين الخطوات،
        // ودالة completeOnboarding() ما كانت تُستدعى أبدًا فعليًا.
        navigation(startDestination = Screen.ProfilePhoto.route, route = ONBOARDING_GRAPH_ROUTE) {
            composable(Screen.ProfilePhoto.route) {
                ProfilePhotoScreen(onNext = { navController.navigate(Screen.FullName.route) }, onSkip = { navController.navigate(Screen.FullName.route) })
            }
            composable(Screen.FullName.route) { entry ->
                val parentEntry = remember(entry) { navController.getBackStackEntry(ONBOARDING_GRAPH_ROUTE) }
                val sharedViewModel: OnboardingViewModel = hiltViewModel(parentEntry)
                FullNameScreen(onNext = { navController.navigate(Screen.Username.route) }, onBack = { navController.popBackStack() }, viewModel = sharedViewModel)
            }
            composable(Screen.Username.route) { entry ->
                val parentEntry = remember(entry) { navController.getBackStackEntry(ONBOARDING_GRAPH_ROUTE) }
                val sharedViewModel: OnboardingViewModel = hiltViewModel(parentEntry)
                UsernameScreen(onNext = { navController.navigate(Screen.Bio.route) }, onBack = { navController.popBackStack() }, viewModel = sharedViewModel)
            }
            composable(Screen.Bio.route) { entry ->
                val parentEntry = remember(entry) { navController.getBackStackEntry(ONBOARDING_GRAPH_ROUTE) }
                val sharedViewModel: OnboardingViewModel = hiltViewModel(parentEntry)
                BioScreen(onNext = { navController.navigate(Screen.Language.route) }, onSkip = { navController.navigate(Screen.Language.route) }, onBack = { navController.popBackStack() }, viewModel = sharedViewModel)
            }
            composable(Screen.Language.route) { entry ->
                val parentEntry = remember(entry) { navController.getBackStackEntry(ONBOARDING_GRAPH_ROUTE) }
                val sharedViewModel: OnboardingViewModel = hiltViewModel(parentEntry)
                LanguageScreen(onNext = { navController.navigate(Screen.OnboardingSuccess.route) }, onBack = { navController.popBackStack() }, viewModel = sharedViewModel)
            }
            composable(Screen.OnboardingSuccess.route) { entry ->
                val parentEntry = remember(entry) { navController.getBackStackEntry(ONBOARDING_GRAPH_ROUTE) }
                val sharedViewModel: OnboardingViewModel = hiltViewModel(parentEntry)
                OnboardingSuccessScreen(
                    viewModel = sharedViewModel,
                    onFinish = { navController.navigate(Screen.Chats.route) { popUpTo(ONBOARDING_GRAPH_ROUTE) { inclusive = true } } }
                )
            }
        }
        composable(Screen.Chats.route) {
            ChatsScreen(
                onNavigateToChat = { chatId -> navController.navigate(Screen.Chat.createRoute(chatId)) },
                onNavigateToProfile = { navController.navigate(Screen.MyProfile.route) },
                onNavigateToRequests = { navController.navigate(Screen.MessageRequests.route) },
                onNavigateToCalls = { navController.navigate(Screen.Calls.route) }
            )
        }
        composable(Screen.Chat.route) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
            ChatScreen(
                chatId = chatId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.MessageRequests.route) {
            MessageRequestsScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenChat = { chatId -> navController.navigate(Screen.Chat.createRoute(chatId)) }
            )
        }
        composable(Screen.MyProfile.route) {
            MyProfileScreen(
                onNavigateToEdit = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.EditProfile.route) {
            EditProfileScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.UserProfile.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            UserProfileScreen(userId = userId, onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { chatId -> navController.navigate(Screen.Chat.createRoute(chatId)) })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToPrivacy = { navController.navigate(Screen.PrivacySecurity.route) },
                onNavigateToBlocked = { navController.navigate(Screen.BlockedUsers.route) },
                onNavigateToLanguage = { navController.navigate(Screen.LanguageSettings.route) },
                onNavigateToSessions = { navController.navigate(Screen.ActiveSessions.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.PrivacySecurity.route) { PrivacySecurityScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.BlockedUsers.route) { BlockedUsersScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.LanguageSettings.route) { LanguageSettingsScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.ActiveSessions.route) { ActiveSessionsScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.Notifications.route) { NotificationsScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.Calls.route) { CallsScreen(onNavigateBack = { navController.popBackStack() }) }
    }
}
