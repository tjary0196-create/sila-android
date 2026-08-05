package com.sila.messaging.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sila.messaging.ui.components.SilaAvatar
import com.sila.messaging.ui.theme.SilaSpacing
import kotlinx.coroutines.delay

/**
 * Pure UI screen for an in-progress call — mirrors the reference design
 * (blurred backdrop photo, centered avatar, live duration timer, circular
 * control cluster, red end button). Intentionally has NO audio/video/WebRTC
 * wiring: [onEndCall] is the only real callback, everything else (mute,
 * speaker, camera toggle) is local UI state only. Wire this up to a real
 * calling backend before shipping — right now it will not place an actual call.
 */
@Composable
fun CallScreen(
    peerName: String,
    peerPhotoUrl: String?,
    isVideoCall: Boolean,
    onBack: () -> Unit,
    onEndCall: () -> Unit
) {
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }
    var isCameraOn by remember { mutableStateOf(isVideoCall) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            elapsedSeconds++
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // خلفية مموّهة من صورة المتصل، مع تدرّج داكن فوقها لضمان وضوح النصوص —
        // نفس روح الصورة المرجعية.
        if (!peerPhotoUrl.isNullOrBlank()) {
            AsyncImage(
                model = peerPhotoUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().blur(38.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                Color.Black
                            )
                        )
                    )
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        // زر الرجوع (تصغير الشاشة أثناء المكالمة)
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(SilaSpacing.sm)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "رجوع",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(top = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val pulse by rememberInfiniteTransition(label = "callPulse").animateFloat(
                initialValue = 1f,
                targetValue = 1.06f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1400, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "callPulseScale"
            )
            Box(
                modifier = Modifier.graphicsLayerScale(pulse),
                contentAlignment = Alignment.Center
            ) {
                SilaAvatar(
                    name = peerName,
                    photoUrl = peerPhotoUrl,
                    seed = peerName,
                    size = 128.dp
                )
            }

            Spacer(modifier = Modifier.height(SilaSpacing.lg))

            Text(
                peerName,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(SilaSpacing.xs))

            Text(
                formatDuration(elapsedSeconds),
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 15.sp
            )
        }

        // مجموعة أزرار التحكم السفلية
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = SilaSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(SilaSpacing.xl),
                modifier = Modifier.padding(bottom = SilaSpacing.xl)
            ) {
                CallControlButton(
                    icon = Icons.Filled.VolumeUp,
                    label = "سماعة",
                    active = isSpeakerOn,
                    onClick = { isSpeakerOn = !isSpeakerOn }
                )
                CallControlButton(
                    icon = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                    label = "كتم",
                    active = isMuted,
                    onClick = { isMuted = !isMuted }
                )
                if (isVideoCall) {
                    CallControlButton(
                        icon = if (isCameraOn) Icons.Filled.VideoCall else Icons.Filled.VideocamOff,
                        label = "فيديو",
                        active = !isCameraOn,
                        onClick = { isCameraOn = !isCameraOn }
                    )
                    CallControlButton(
                        icon = Icons.Filled.SwapHoriz,
                        label = "تبديل",
                        active = false,
                        onClick = { }
                    )
                }
            }

            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
            ) {
                Icon(
                    Icons.Filled.CallEnd,
                    contentDescription = "إنهاء المكالمة",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun CallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(
                    if (active) Color.White else Color.White.copy(alpha = 0.18f)
                )
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (active) Color.Black else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(SilaSpacing.xxs))
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}

private fun formatDuration(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%02d:%02d".format(m, s)
}

private fun Modifier.graphicsLayerScale(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
)
