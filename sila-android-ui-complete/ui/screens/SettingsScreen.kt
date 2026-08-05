package com.sila.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sila.model.User
import com.sila.ui.components.SilaAvatar
import com.sila.ui.components.SilaDivider
import com.sila.ui.components.SilaTopBar
import com.sila.ui.theme.*

@Composable
fun SettingsScreen(
    user: User,
    onBackClick: () -> Unit,
    onAccountClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onAppearanceClick: () -> Unit,
    onChatsClick: () -> Unit,
    onDataStorageClick: () -> Unit,
    onHelpSupportClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Scaffold(
        topBar = { SilaTopBar(title = "Settings", onBackClick = onBackClick) },
        containerColor = BackgroundPrimary
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            item {
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.98f else 1f,
                    label = "profile_scale"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(scale)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onAccountClick
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SilaAvatar(imageUrl = user.avatarUrl, isOnline = user.isOnline, size = 56)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(text = user.handle, fontSize = 14.sp, color = TextMuted)
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            item { SilaDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

            item {
                SettingsGroup(
                    items = listOf(
                        SettingsItemData(Icons.Outlined.Person, "Account", null, onAccountClick),
                        SettingsItemData(Icons.Outlined.Lock, "Privacy", null, onPrivacyClick),
                        SettingsItemData(Icons.Outlined.Notifications, "Notifications", null, onNotificationsClick),
                        SettingsItemData(Icons.Outlined.Palette, "Appearance", "Dark", onAppearanceClick),
                        SettingsItemData(Icons.Outlined.ChatBubbleOutline, "Chats", null, onChatsClick),
                        SettingsItemData(Icons.Outlined.Storage, "Data and Storage", null, onDataStorageClick),
                        SettingsItemData(Icons.Outlined.HelpOutline, "Help & Support", null, onHelpSupportClick),
                        SettingsItemData(Icons.Outlined.Info, "About", null, onAboutClick)
                    )
                )
            }

            item { SilaDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

            item {
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.98f else 1f,
                    label = "logout_scale"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(scale)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onLogoutClick
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Log Out",
                        fontSize = 15.sp,
                        color = ErrorRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

data class SettingsItemData(
    val icon: ImageVector,
    val title: String,
    val subtitle: String?,
    val onClick: () -> Unit
)

@Composable
fun SettingsGroup(items: List<SettingsItemData>) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        items.forEach { item ->
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.98f else 1f,
                label = "setting_scale"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = item.onClick
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (item.subtitle != null) {
                    Text(text = item.subtitle, fontSize = 14.sp, color = TextMuted)
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
