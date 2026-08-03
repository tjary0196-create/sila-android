package com.sila.messaging.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sila.messaging.ui.components.SilaPrimaryButton
import com.sila.messaging.ui.components.SilaSecondaryButton
import com.sila.messaging.ui.theme.SilaSpacing
import kotlinx.coroutines.launch
import kotlin.random.Random

private val usernameAdjectives = listOf("swift", "calm", "bright", "clever", "gentle", "bold", "kind", "cool")
private val usernameNouns = listOf("qamar", "bahr", "nasim", "yasmin", "salam", "amal", "ward", "sama")

private fun randomUsernameSuggestion(): String {
    val adj = usernameAdjectives.random()
    val noun = usernameNouns.random()
    val number = Random.nextInt(10, 999)
    return "${noun}_${adj}$number"
}

private fun isValidUsernameFormat(value: String): Boolean =
    value.length in 3..20 && value.all { (it in 'a'..'z') || (it in '0'..'9') || it == '_' }

/**
 * Username claim step shown right after first sign-in. Lets the user pick a
 * public handle (with a one-tap random suggestion), validates the format
 * locally, then hands the claim off to [onClaim].
 */
@Composable
fun UsernameScreen(
    onClaim: suspend (String) -> Boolean,
    onSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val trimmed = username.trim()
    val formatValid = trimmed.isEmpty() || isValidUsernameFormat(trimmed)

    fun submit() {
        if (!isValidUsernameFormat(trimmed)) {
            error = "اسم المستخدم لازم يكون ٣-٢٠ حرف/رقم إنجليزي بدون مسافات"
            return
        }
        error = null
        isLoading = true
        scope.launch {
            try {
                val claimed = onClaim(trimmed)
                if (claimed) onSuccess() else error = "اسم المستخدم هذا محجوز، جرب اسم تاني"
            } catch (e: Exception) {
                error = "صار خطأ، حاول مرة تانية"
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = SilaSpacing.xxl),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.AlternateEmail,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(SilaSpacing.lg))

        Text(
            "اختر اسم مستخدم",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(SilaSpacing.xs))
        Text(
            "هيك رح يقدر أصدقاؤك يلاقوك على صلة",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(SilaSpacing.xxl))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it.trim().lowercase()
                error = null
            },
            label = { Text("اسم المستخدم") },
            leadingIcon = {
                Text("@", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
            },
            trailingIcon = {
                if (trimmed.isNotEmpty()) {
                    Icon(
                        if (formatValid) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline,
                        contentDescription = null,
                        tint = if (formatValid) Color(0xFF2FBE71) else MaterialTheme.colorScheme.error
                    )
                }
            },
            isError = trimmed.isNotEmpty() && !formatValid,
            supportingText = {
                Text(
                    "أحرف إنجليزية صغيرة وأرقام و_ فقط، ٣-٢٠ حرف",
                    fontSize = 11.sp
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(SilaSpacing.sm))

        SilaSecondaryButton(
            text = "اقترح اسم لي",
            icon = Icons.Filled.AutoAwesome,
            onClick = {
                username = randomUsernameSuggestion()
                error = null
            },
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(SilaSpacing.xl))

        SilaPrimaryButton(
            text = if (isLoading) "جارٍ الحفظ..." else "متابعة",
            onClick = { submit() },
            enabled = !isLoading && trimmed.isNotEmpty(),
            loading = isLoading
        )

        AnimatedVisibility(visible = error != null) {
            Column {
                Spacer(modifier = Modifier.height(SilaSpacing.sm))
                Text(
                    error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}
