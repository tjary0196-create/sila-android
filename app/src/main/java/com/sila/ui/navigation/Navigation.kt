package com.sila.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.sila.di.ServiceLocator
import com.sila.di.viewModelFactory
import com.sila.messaging.core.result.AppResult
import com.sila.model.User
import com.sila.ui.mapper.toUiUser
import com.sila.ui.screens.*
import com.sila.ui.theme.AccentBlue
import com.sila.ui.theme.BackgroundPrimary
import com.sila.ui.viewmodel.AuthUiState
import com.sila.ui.viewmodel.AuthViewModel
import com.sila.ui.viewmodel.ChatViewModel
import com.sila.ui.viewmodel.ChatsViewModel
import com.sila.ui.viewmodel.ProfileViewModel
import com.sila.ui.viewmodel.SearchViewModel
import com.sila.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    data object Chats : Screen("chats")
    data object MessageRequests : Screen("message_requests")
    data object Profile : Screen("profile")
    data object Search : Screen("search")

    data object UserProfile : Screen("user_profile/{userId}") {
        fun createRoute(userId: String) = "user_profile/$userId"
    }

    data object Chat : Screen("chat/{userId}") {
        fun createRoute(userId: String) = "chat/$userId"
    }

    data object ChatInfo : Screen("chat_info/{userId}") {
        fun createRoute(userId: String) = "chat_info/$userId"
    }

    data object VoiceCall : Screen("voice_call/{userId}") {
        fun createRoute(userId: String) = "voice_call/$userId"
    }

    data object Settings : Screen("settings")
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(BackgroundPrimary),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = AccentBlue)
    }
}

/**
 * Root of the app. Everything is gated behind Firebase Auth state: signed-out users only ever see
 * [LoginScreen]; signed-in users with no Firestore profile yet see [UsernameSetupScreen]; only a
 * fully signed-in user with a profile reaches the real chat app ([MainGraph]).
 */
@Composable
fun SilaNavigation() {
    val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory { AuthViewModel() })
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    when (val state = authState) {
        is AuthUiState.Loading, is AuthUiState.CheckingProfile -> LoadingScreen()

        is AuthUiState.LoggedOut -> LoginScreen(
            onSignIn = { idToken -> authViewModel.signInWithGoogleIdToken(idToken) },
            isSigningIn = authViewModel.isSigningIn,
            errorMessage = authViewModel.signInError
        )

        is AuthUiState.NeedsUsername -> {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            var isClaiming by remember { mutableStateOf(false) }
            var claimError by remember { mutableStateOf<String?>(null) }
            val usernameRepository = ServiceLocator.usernameRepository
            val scope = rememberCoroutineScope()

            UsernameSetupScreen(
                displayName = firebaseUser?.displayName ?: "",
                photoUrl = firebaseUser?.photoUrl?.toString(),
                isLoading = isClaiming,
                errorMessage = claimError,
                onClaim = { username ->
                    isClaiming = true
                    claimError = null
                    scope.launch {
                        val result = usernameRepository.claimUsername(
                            uid = state.uid,
                            username = username,
                            displayName = firebaseUser?.displayName,
                            photoUrl = firebaseUser?.photoUrl?.toString()
                        )
                        isClaiming = false
                        when (result) {
                            is AppResult.Success -> authViewModel.onProfileCreated(state.uid)
                            is AppResult.Error -> claimError = result.message
                        }
                    }
                }
            )
        }

        is AuthUiState.LoggedIn -> MainGraph(myUid = state.uid, onSignOut = { authViewModel.signOut() })
    }
}

@Composable
private fun MainGraph(
    myUid: String,
    onSignOut: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Screen.Chats.route) {

        composable(Screen.Chats.route) {
            val chatsViewModel: ChatsViewModel = viewModel(
                factory = viewModelFactory { ChatsViewModel(myUid) }
            )
            val chats by chatsViewModel.chats.collectAsStateWithLifecycle()

            ChatsScreen(
                chats = chats,
                onChatClick = { chat -> navController.navigate(Screen.Chat.createRoute(chat.user.id)) },
                onProfileClick = { navController.navigate(Screen.Settings.route) },
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onNewChatClick = { navController.navigate(Screen.Search.route) },
                onCallsClick = { },
                onPeopleClick = { navController.navigate(Screen.Search.route) },
                onRequestsClick = { navController.navigate(Screen.MessageRequests.route) }
            )
        }

        composable(Screen.MessageRequests.route) {
            // No backend concept of "message requests" exists yet (anyone can just start a chat) —
            // this screen is still UI-only until that feature is designed on the Firestore side.
            MessageRequestsScreen(
                onBackClick = { navController.popBackStack() },
                onAcceptClick = { },
                onDeleteClick = { },
                onRequestClick = { }
            )
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = viewModelFactory { SettingsViewModel(myUid) }
            )
            val user by settingsViewModel.user.collectAsStateWithLifecycle()

            val loadedUser = user
            if (loadedUser != null) {
                SettingsScreen(
                    user = loadedUser,
                    onBackClick = { navController.popBackStack() },
                    onAccountClick = { navController.navigate(Screen.Profile.route) },
                    onPrivacyClick = { },
                    onNotificationsClick = { },
                    onAppearanceClick = { },
                    onChatsClick = { },
                    onDataStorageClick = { },
                    onHelpSupportClick = { },
                    onAboutClick = { },
                    onLogoutClick = {
                        settingsViewModel.signOut()
                        onSignOut()
                    }
                )
            } else {
                LoadingScreen()
            }
        }

        composable(Screen.Profile.route) {
            val profileViewModel: ProfileViewModel = viewModel(
                factory = viewModelFactory { ProfileViewModel(myUid = myUid, targetUid = myUid) }
            )
            val user by profileViewModel.user.collectAsStateWithLifecycle()
            val loadedUser = user

            if (loadedUser != null) {
                ProfileScreen(
                    user = loadedUser,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { },
                    onMessageRequestClick = { },
                    onBlockClick = { },
                    onReportClick = { },
                    onViewAllMedia = { }
                )
            } else {
                LoadingScreen()
            }
        }

        composable(
            route = Screen.UserProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val targetUid = backStackEntry.arguments?.getString("userId") ?: return@composable
            val profileViewModel: ProfileViewModel = viewModel(
                key = "profile_$targetUid",
                factory = viewModelFactory { ProfileViewModel(myUid = myUid, targetUid = targetUid) }
            )
            val user by profileViewModel.user.collectAsStateWithLifecycle()
            val loadedUser = user

            if (loadedUser != null) {
                ProfileScreen(
                    user = loadedUser,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { },
                    onMessageRequestClick = { },
                    onBlockClick = { },
                    onReportClick = { },
                    onViewAllMedia = { }
                )
            } else {
                LoadingScreen()
            }
        }

        composable(Screen.Search.route) {
            val searchViewModel: SearchViewModel = viewModel(factory = viewModelFactory { SearchViewModel() })
            val query by searchViewModel.query.collectAsStateWithLifecycle()
            val results by searchViewModel.results.collectAsStateWithLifecycle()

            SearchScreen(
                query = query,
                results = results,
                onQueryChange = { searchViewModel.onQueryChange(it) },
                onBackClick = { navController.popBackStack() },
                onUserClick = { user: User -> navController.navigate(Screen.UserProfile.createRoute(user.id)) },
                onMessageClick = { user: User -> navController.navigate(Screen.Chat.createRoute(user.id)) }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val peerUid = backStackEntry.arguments?.getString("userId") ?: return@composable
            val chatViewModel: ChatViewModel = viewModel(
                key = "chat_$peerUid",
                factory = viewModelFactory { ChatViewModel(myUid = myUid, peerUid = peerUid) }
            )
            val messages by chatViewModel.messages.collectAsStateWithLifecycle()
            val peerProfile by chatViewModel.peerProfile.collectAsStateWithLifecycle()
            val peerUser = peerProfile?.toUiUser() ?: User(id = peerUid, name = "...", handle = "")

            ChatScreen(
                user = peerUser,
                messages = messages,
                onBackClick = { navController.popBackStack() },
                onCallClick = { navController.navigate(Screen.VoiceCall.createRoute(peerUid)) },
                onMoreClick = { navController.navigate(Screen.ChatInfo.createRoute(peerUid)) },
                onSendMessage = { text -> chatViewModel.sendMessage(text) }
            )
        }

        composable(
            route = Screen.ChatInfo.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val peerUid = backStackEntry.arguments?.getString("userId") ?: return@composable
            val profileViewModel: ProfileViewModel = viewModel(
                key = "chatinfo_$peerUid",
                factory = viewModelFactory { ProfileViewModel(myUid = myUid, targetUid = peerUid) }
            )
            val user by profileViewModel.user.collectAsStateWithLifecycle()
            val loadedUser = user

            if (loadedUser != null) {
                ChatInfoScreen(
                    user = loadedUser,
                    onBackClick = { navController.popBackStack() },
                    onAudioCallClick = { navController.navigate(Screen.VoiceCall.createRoute(peerUid)) },
                    onVideoCallClick = { },
                    onSearchClick = { },
                    onSharedMediaClick = { },
                    onFilesClick = { },
                    onPinnedMessagesClick = { },
                    onClearChatClick = { },
                    onBlockUserClick = { },
                    onDeleteChatClick = { navController.popBackStack(Screen.Chats.route, inclusive = false) }
                )
            } else {
                LoadingScreen()
            }
        }

        composable(
            route = Screen.VoiceCall.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val peerUid = backStackEntry.arguments?.getString("userId") ?: return@composable
            val profileViewModel: ProfileViewModel = viewModel(
                key = "call_$peerUid",
                factory = viewModelFactory { ProfileViewModel(myUid = myUid, targetUid = peerUid) }
            )
            val user by profileViewModel.user.collectAsStateWithLifecycle()

            // Sila doesn't have real voice/video calling wired up yet (no WebRTC/signaling
            // backend) — this screen is UI-only, same as MessageRequestsScreen.
            VoiceCallScreen(
                userName = user?.name ?: "...",
                onMuteClick = { },
                onSpeakerClick = { },
                onVideoClick = { },
                onEndCall = { navController.popBackStack() }
            )
        }
    }
}
