package com.sila.messaging.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sila.messaging.ui.components.SilaPrimaryButton
import com.sila.messaging.ui.theme.SilaSpacing

/**
 * Splash / sign-in screen — first thing a new user sees. Presents the Sila
 * brand mark, a short value proposition, and either a Google sign-in action
 * or a "continue" action once already authenticated.
 */
@Composable
fun LoginScreen(
    isSignedIn: Boolean,
    onSignInClick: () -> Unit,
    onContinue: () -> Unit,
    errorMessage: String?,
    isCheckingProfile: Boolean = false
) {
    var isLoading by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    LaunchedEffect(errorMessage) { if (!errorMessage.isNullOrEmpty()) isLoading = false }

    Box(modifier = Modifier.fillMaxSize()) {
        // Soft brand-tinted glow behind the hero mark, echoing the app's
        // indigo/violet palette without hardcoding new colors.
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .size(260.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = SilaSpacing.xxl),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "S",
                            color = Color.White,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(SilaSpacing.xl))

                    Text(
                        "صلة",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(SilaSpacing.xs))

                    Text(
                        "تواصل بخصوصية وسهولة",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(SilaSpacing.xxxl))

                    if (isSignedIn) {
                        SilaPrimaryButton(
                            text = if (isCheckingProfile) "جارِ التحقق..." else "متابعة",
                            onClick = onContinue,
                            enabled = !isCheckingProfile,
                            loading = isCheckingProfile,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        SilaPrimaryButton(
                            text = if (isLoading) "جارِ تسجيل الدخول..." else "تسجيل الدخول عبر Google",
                            onClick = {
                                isLoading = true
                                onSignInClick()
                            },
                            loading = isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    AnimatedVisibility(visible = !errorMessage.isNullOrEmpty()) {
                        Column {
                            Spacer(modifier = Modifier.height(SilaSpacing.md))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                                    .padding(horizontal = SilaSpacing.md, vertical = SilaSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(SilaSpacing.xs))
                                Text(
                                    errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(SilaSpacing.xl))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(SilaSpacing.xxs))
                        Text(
                            "بياناتك محمية أثناء النقل ومحادثاتك خاصة بحسابك",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
