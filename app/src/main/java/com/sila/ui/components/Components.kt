package com.sila.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sila.model.User
import com.sila.ui.theme.*

// ===== ANIMATED AVATAR WITH STORY RING =====
@Composable
fun SilaAvatar(
    imageUrl: String?,
    isOnline: Boolean = false,
    size: Int = 48,
    hasStory: Boolean = false,
    modifier: Modifier = Modifier
) {
    val storyAnimation by rememberInfiniteTransition(label = "story").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "story_rotation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Story ring
        if (hasStory) {
            Box(
                modifier = Modifier
                    .size((size + 6).dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.5.dp,
                        brush = androidx.compose.ui.graphics.Brush.sweepGradient(
                            colors = listOf(
                                AccentBlue,
                                AccentBlueLight,
                                Color(0xFF8B5CF6),
                                AccentBlue
                            ),
                            // Use rotation based on animation
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Avatar container
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(SurfaceSecondary)
                .border(2.dp, if (hasStory) BackgroundPrimary else BorderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size((size * 0.45).dp)
                )
            }
        }

        // Online indicator
        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(BackgroundPrimary)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(StatusOnline)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-2).dp, y = (-2).dp)
            )
        }
    }
}

// ===== SEARCH BAR =====
@Composable
fun SilaSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search",
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(placeholder, color = TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Normal)
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor   = SurfacePrimary,
            unfocusedContainerColor = SurfacePrimary,
            disabledContainerColor  = SurfacePrimary,
            focusedIndicatorColor   = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor  = Color.Transparent,
            cursorColor             = AccentBlue,
            focusedTextColor        = TextPrimary,
            unfocusedTextColor      = TextPrimary,
            focusedLeadingIconColor = TextMuted,
            unfocusedLeadingIconColor = TextMuted
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    )
}

// ===== BOTTOM NAVIGATION =====
@Composable
fun SilaBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    unreadChats: Int = 0,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple("Chats",  Icons.Outlined.ChatBubbleOutline, Icons.Filled.ChatBubble),
        Triple("Calls",  Icons.Outlined.Call, Icons.Filled.Call),
        Triple("People", Icons.Outlined.People, Icons.Filled.People),
        Triple("Profile",Icons.Outlined.Person, Icons.Filled.Person)
    )

    NavigationBar(
        containerColor = SurfacePrimary,
        tonalElevation = 0.dp,
        modifier = modifier.height(68.dp)
    ) {
        items.forEachIndexed { index, (label, unselectedIcon, selectedIcon) ->
            val selected = selectedTab == index
            val scale by animateFloatAsState(
                targetValue = if (selected) 1.1f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "nav_scale"
            )

            NavigationBarItem(
                icon = {
                    BadgedBox(
                        badge = {
                            if (index == 0 && unreadChats > 0) {
                                Badge(
                                    containerColor = AccentBlue,
                                    contentColor = Color.White,
                                    modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                                ) {
                                    Text(
                                        text = if (unreadChats > 9) "9+" else unreadChats.toString(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (selected) selectedIcon else unselectedIcon,
                            contentDescription = label,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1
                    )
                },
                selected = selected,
                onClick = { onTabSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor       = AccentBlue,
                    selectedTextColor       = AccentBlue,
                    unselectedIconColor     = TextMuted,
                    unselectedTextColor     = TextMuted,
                    indicatorColor          = Color.Transparent
                ),
                alwaysShowLabel = true
            )
        }
    }
}

// ===== TOP APP BAR =====
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SilaTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextPrimary
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor           = BackgroundPrimary,
            scrolledContainerColor   = BackgroundPrimary,
            titleContentColor        = TextPrimary,
            navigationIconContentColor = TextPrimary,
            actionIconContentColor   = TextPrimary
        ),
        modifier = modifier
    )
}

// ===== PRIMARY BUTTON =====
@Composable
fun SilaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentBlue,
            disabledContainerColor = AccentBlue.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .scale(scale)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f)
        )
    }
}

// ===== OUTLINED BUTTON =====
@Composable
fun SilaOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = BorderColor
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "outlined_scale"
    )

    OutlinedButton(
        onClick = onClick,
        interactionSource = interactionSource,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = SurfaceSecondary,
            contentColor   = TextPrimary
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .scale(scale)
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ===== DIVIDER =====
@Composable
fun SilaDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        color = DividerColor,
        thickness = 0.5.dp,
        modifier = modifier
    )
}

// ===== TYPING INDICATOR =====
@Composable
fun TypingIndicator() {
    val dotSize = 6.dp
    val delayUnit = 200

    @Composable
    fun Dot(delay: Int) {
        val infiniteTransition = rememberInfiniteTransition(label = "dot_$delay")
        val offset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -6f,
            animationSpec = infiniteRepeatable(
                animation = tween(400, delayMillis = delay, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot_anim"
        )

        Box(
            modifier = Modifier
                .size(dotSize)
                .offset(y = offset.dp)
                .clip(CircleShape)
                .background(AccentBlue)
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Dot(0)
        Dot(delayUnit)
        Dot(delayUnit * 2)
    }
}

// ===== VOICE WAVE ANIMATION =====
@Composable
fun VoiceWaveform(
    isPlaying: Boolean = false,
    modifier: Modifier = Modifier,
    barColor: Color = TextPrimary.copy(alpha = 0.7f)
) {
    val barCount = 20
    val random = remember { java.util.Random() }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(barCount) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "wave_$index")
            val heightFraction by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = if (isPlaying) random.nextFloat() * 0.8f + 0.2f else 0.4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 400 + random.nextInt(400),
                        delayMillis = index * 30,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "wave_anim"
            )

            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight(heightFraction)
                        .clip(RoundedCornerShape(1.dp))
                        .background(barColor)
                )
            }
        }
    }
}
