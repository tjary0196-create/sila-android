package com.sila.messaging.ui

import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.sila.messaging.data.LocationUtils
import com.sila.messaging.ui.screens.profile.ProfileViewModel
import com.sila.messaging.ui.components.SilaLoading
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sila.messaging.data.user.FirestoreUserRepository
import com.sila.messaging.ui.components.SilaPrimaryButton
import com.sila.messaging.ui.components.SilaProfileHeader
import com.sila.messaging.ui.components.SilaSectionCard
import com.sila.messaging.ui.components.SilaSettingRow
import com.sila.messaging.ui.components.SilaSettingTextField
import com.sila.messaging.ui.components.SilaSettingToggle
import com.sila.messaging.ui.components.SilaStatusChip
import com.sila.messaging.ui.components.SilaStatusDots
import com.sila.messaging.ui.components.SilaTopBar
import com.sila.messaging.ui.theme.SilaSpacing
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid ?: return
    
    val viewModel: ProfileViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(FirestoreUserRepository(), uid) as T
        }
    })

    val uiState by viewModel.ui.collectAsState()
    val profile = uiState.profile

    var photoUrl by remember { mutableStateOf<String?>(null) }
    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("online") }

    var showBio by remember { mutableStateOf(true) }
    var showBirthDate by remember { mutableStateOf(true) }
    var showCountry by remember { mutableStateOf(true) }
    var showLastSeen by remember { mutableStateOf(true) }

    LaunchedEffect(profile) {
        if (profile != null) {
            photoUrl = profile.photoUrl
            displayName = profile.displayName
            username = profile.username
            bio = profile.bio ?: ""
            birthDate = profile.birthDate ?: ""
            country = profile.country ?: LocationUtils.detectCountry(context)
            status = profile.status
            showBio = profile.privacy.showBio
            showBirthDate = profile.privacy.showBirthDate
            showCountry = profile.privacy.showCountry
            showLastSeen = profile.privacy.showLastSeen
        }
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) {
                viewModel.uploadPhoto(bytes)
            }
        }
    }

    fun openDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(context, { _, year, month, day ->
            birthDate = "%04d-%02d-%02d".format(year, month + 1, day)
        }, cal.get(Calendar.YEAR) - 20, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    fun saveProfile() {
        viewModel.updateProfile(mapOf(
            "displayName" to displayName,
            "bio" to bio,
            "birthDate" to birthDate,
            "country" to country,
            "status" to status,
            "showBio" to showBio,
            "showBirthDate" to showBirthDate,
            "showCountry" to showCountry,
            "showLastSeen" to showLastSeen
        ))
        onBack()
    }

    Scaffold(
        topBar = {
            SilaTopBar(
                title = "الملف الشخصي",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    TextButton(onClick = { saveProfile() }, enabled = !uiState.isSaving) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text("حفظ", fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            SilaLoading(modifier = Modifier.fillMaxSize().padding(padding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = SilaSpacing.md)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(SilaSpacing.lg))

            // ===== قسم: الملف الشخصي (Profile) — معاينة حية تعكس الحقول تحت =====
            SilaProfileHeader(
                displayName = displayName,
                username = username,
                photoUrl = photoUrl,
                bio = bio,
                avatarSize = 110.dp,
                isAvatarLoading = uiState.isSaving,
                avatarOverlay = {
                    IconButton(
                        onClick = { photoPicker.launch("image/*") },
                        modifier = Modifier
                            .size(34.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            Icons.Filled.CameraAlt,
                            contentDescription = "تغيير الصورة",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(SilaSpacing.xs))

            // مؤشر الحالة الحيّة تحت الاسم، بنفس روح الصورة المرجعية (نقطة + نص)،
            // مربوط بالحالة الفعلية المختارة تحت (Account) لا رقم ثابت.
            val (statusDotColor, statusLabel) = when (status) {
                "online" -> SilaStatusDots.Online to "متصل الآن"
                "away" -> SilaStatusDots.Away to "بعيد"
                else -> null to "غير ظاهر"
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (statusDotColor != null) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(statusDotColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(SilaSpacing.xxs))
                }
                Text(
                    statusLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (statusDotColor != null) statusDotColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.error != null) {
                Text(
                    uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = SilaSpacing.xs),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(SilaSpacing.xl))

            // ===== قسم: المعلومات الشخصية (Personal Information) =====
            SilaSectionCard(title = "المعلومات الشخصية") {
                SilaSettingTextField(label = "الاسم المعروض", value = displayName, onValueChange = { displayName = it })
                Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                SilaSettingTextField(label = "اسم المستخدم", value = username, onValueChange = { username = it }, prefix = "@")
                Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                SilaSettingTextField(label = "نبذة شخصية", value = bio, onValueChange = { bio = it }, singleLine = false)
                Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                SilaSettingRow(
                    label = "تاريخ الميلاد",
                    value = birthDate.ifBlank { "اختر التاريخ" },
                    onClick = { openDatePicker() },
                    icon = Icons.Filled.Cake
                )
                Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                SilaSettingTextField(label = "البلد", value = country, onValueChange = { country = it })
            }

            Spacer(modifier = Modifier.height(SilaSpacing.md))

            // ===== قسم: الخصوصية (Privacy) =====
            SilaSectionCard(title = "الخصوصية") {
                SilaSettingToggle(label = "إظهار النبذة الشخصية للآخرين", checked = showBio, onCheckedChange = { showBio = it })
                Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                SilaSettingToggle(label = "إظهار تاريخ الميلاد للآخرين", checked = showBirthDate, onCheckedChange = { showBirthDate = it })
                Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                SilaSettingToggle(label = "إظهار البلد للآخرين", checked = showCountry, onCheckedChange = { showCountry = it })
                Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                SilaSettingToggle(
                    label = "إظهار آخر ظهور والحالة المتصلة",
                    checked = showLastSeen,
                    onCheckedChange = { showLastSeen = it },
                    helperText = "إذا أخفيت آخر ظهور، لن تتمكن من رؤية آخر ظهور الآخرين أيضاً"
                )
            }

            Spacer(modifier = Modifier.height(SilaSpacing.md))

            // ===== قسم: الحساب (Account) =====
            SilaSectionCard(title = "الحساب") {
                Text(
                    "الحالة",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = SilaSpacing.sm)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = SilaSpacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(SilaSpacing.xs)
                ) {
                    listOf(
                        Triple("online", "متصل", SilaStatusDots.Online),
                        Triple("away", "بعيد", SilaStatusDots.Away),
                        Triple("invisible", "غير ظاهر", null)
                    ).forEach { (value, label, dot) ->
                        SilaStatusChip(
                            label = label,
                            selected = status == value,
                            onClick = { status = value },
                            dotColor = dot
                        )
                    }
                }
                Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = SilaSpacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Public,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(SilaSpacing.xs))
                    Text(
                        "الحساب مرتبط بتسجيل دخول Google",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(SilaSpacing.md))

            // ===== قسم: منطقة الخطر (Danger Zone) =====
            // ملاحظة: هذا زر واجهة غير مفعّل حالياً (لا ينفّذ signOut ولا أي إجراء)
            // لعدم توفر ربط تسجيل الخروج بهذه الشاشة بعد. يظهر معطّلاً بوضوح
            // مع "قريباً" حتى لا يبدو أنه يعمل ولا يعمل فعلياً.
            SilaSectionCard(title = "منطقة الخطر") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = SilaSpacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Logout,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(SilaSpacing.xs))
                        Text(
                            "تسجيل الخروج",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        )
                    }
                    Text(
                        "قريباً",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(SilaSpacing.xl))

            SilaPrimaryButton(
                text = if (uiState.isSaving) "جارِ الحفظ..." else "حفظ التغييرات",
                onClick = { saveProfile() },
                loading = uiState.isSaving
            )

            Spacer(modifier = Modifier.height(SilaSpacing.xxl))
        }
    }
}
