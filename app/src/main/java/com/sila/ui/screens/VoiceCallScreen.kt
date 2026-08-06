package com.sila.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sila.ui.components.SilaAvatar
import com.sila.ui.theme.*

@Composable
fun VoiceCallScreen(
    userName: String,
    callDuration: String = "00:24",
    onMuteClick: () -> Unit,
    onSpeakerClick: () -> Unit,
    onVideoClick: () -> Unit,
    onEndCall: () -> Unit
) {
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(false) }
    var isVideoOn by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            BackgroundPrimary,
                            BackgroundPrimary.copy(alpha = 0.8f),
                            Color(0xFF0F172A)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "avatar_pulse"
            )

            Box(
                modifier = Modifier.scale(scale),
                contentAlignment = Alignment.Center
            ) {
                SilaAvatar(imageUrl = null, size = 130)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = userName,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = callDuration,
                fontSize = 16.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CallControlButton(
                    icon = Icons.Default.VolumeUp,
                    label = "Speaker",
                    isActive = isSpeakerOn,
                    onClick = { isSpeakerOn = !isSpeakerOn; onSpeakerClick() }
                )
                CallControlButton(
                    icon = Icons.Default.Mic,
                    label = "Mute",
                    isActive = isMuted,
                    activeColor = ErrorRed,
                    onClick = { isMuted = !isMuted; onMuteClick() }
                )
                CallControlButton(
                    icon = Icons.Default.Videocam,
                    label = "Video",
                    isActive = isVideoOn,
                    onClick = { isVideoOn = !isVideoOn; onVideoClick() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            val endInteraction = remember { MutableInteractionSource() }
            val endPressed by endInteraction.collectIsPressedAsState()
            val endScale by animateFloatAsState(
                targetValue = if (endPressed) 0.9f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "end_call_scale"
            )

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .scale(endScale)
                    .clip(CircleShape)
                    .background(ErrorRed)
                    .clickable(
                        interactionSource = endInteraction,
                        indication = null,
                        onClick = onEndCall
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun CallControlButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color = AccentBlue,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "control_scale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(if (isActive) activeColor else SurfacePrimary)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.White else TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
    }
}
