package com.sila.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sila.model.User
import com.sila.ui.screens.*

sealed class Screen(val route: String) {
    object Chats : Screen("chats")
    object MessageRequests : Screen("message_requests")
    object Profile : Screen("profile")
    object Search : Screen("search")
    object Chat : Screen("chat/{userId}") {
        fun createRoute(userId: String) = "chat/$userId"
    }
    object ChatInfo : Screen("chat_info/{userId}") {
        fun createRoute(userId: String) = "chat_info/$userId"
    }
    object VoiceCall : Screen("voice_call")
    object Settings : Screen("settings")
}

@Composable
fun SilaNavigation(
    navController: NavHostController = rememberNavController()
) {
    val demoUser = User(
        id = "1",
        name = "Ali Hassan",
        handle = "@alihassan",
        isOnline = true,
        bio = "Software developer & UI/UX Designer
Coffee ☕ • Code 💻 • Travel ✈️",
        joinedDate = "Joined January 2026",
        chats = 125,
        friends = 42,
        groups = 18
    )

    NavHost(navController = navController, startDestination = Screen.Chats.route) {
        composable(Screen.Chats.route) {
            ChatsScreen(
                onChatClick = { navController.navigate(Screen.Chat.createRoute(it.user.id)) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onNewChatClick = { },
                onCallsClick = { },
                onPeopleClick = { },
                onRequestsClick = { navController.navigate(Screen.MessageRequests.route) }
            )
        }

        composable(Screen.MessageRequests.route) {
            MessageRequestsScreen(
                onBackClick = { navController.popBackStack() },
                onAcceptClick = { },
                onDeleteClick = { },
                onRequestClick = { }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                user = demoUser,
                onBackClick = { navController.popBackStack() },
                onEditClick = { },
                onMessageRequestClick = { },
                onBlockClick = { },
                onReportClick = { },
                onViewAllMedia = { }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onUserClick = { },
                onMessageClick = { }
            )
        }

        composable(Screen.Chat.route) {
            ChatScreen(
                user = demoUser,
                onBackClick = { navController.popBackStack() },
                onCallClick = { navController.navigate(Screen.VoiceCall.route) },
                onMoreClick = { navController.navigate(Screen.ChatInfo.createRoute(demoUser.id)) },
                onSendMessage = { }
            )
        }

        composable(Screen.ChatInfo.route) {
            ChatInfoScreen(
                user = demoUser,
                onBackClick = { navController.popBackStack() },
                onAudioCallClick = { navController.navigate(Screen.VoiceCall.route) },
                onVideoCallClick = { },
                onSearchClick = { },
                onSharedMediaClick = { },
                onFilesClick = { },
                onPinnedMessagesClick = { },
                onClearChatClick = { },
                onBlockUserClick = { },
                onDeleteChatClick = { }
            )
        }

        composable(Screen.VoiceCall.route) {
            VoiceCallScreen(
                userName = demoUser.name,
                onMuteClick = { },
                onSpeakerClick = { },
                onVideoClick = { },
                onEndCall = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                user = demoUser,
                onBackClick = { navController.popBackStack() },
                onAccountClick = { },
                onPrivacyClick = { },
                onNotificationsClick = { },
                onAppearanceClick = { },
                onChatsClick = { },
                onDataStorageClick = { },
                onHelpSupportClick = { },
                onAboutClick = { },
                onLogoutClick = { }
            )
        }
    }
}
