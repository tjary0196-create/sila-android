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
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.sila.messaging.BuildConfig
import com.sila.messaging.data.LocationUtils
import com.sila.messaging.data.UserRepository
import com.sila.messaging.ui.components.SilaAvatar
import com.sila.messaging.ui.components.SilaLoading
import com.sila.messaging.ui.components.SilaPrimaryButton
import com.sila.messaging.ui.components.SilaSectionCard
import com.sila.messaging.ui.components.SilaSettingRow
import com.sila.messaging.ui.components.SilaSettingTextField
import com.sila.messaging.ui.components.SilaSettingToggle
import com.sila.messaging.ui.components.SilaTopBar
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid ?: return
    val repo = remember { UserRepository() }
    val scope = rememberCoroutineScope()

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

    var isSaving by remember { mutableStateOf(false) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var isLoadingProfile by remember { mutableStateOf(true) }

    LaunchedEffect(uid) {
        isLoadingProfile = true
        val profile = repo.getUserProfile(uid)
        if (profile != null) {
            photoUrl = profile.photoUrl
            displayName = profile.displayName ?: ""
            username = profile.username
            bio = profile.bio ?: ""
            birthDate = profile.birthDate ?: ""
            country = profile.country?.takeIf { it.isNotBlank() } ?: LocationUtils.detectCountry(context)
            status = profile.status
            showBio = profile.showBio
            showBirthDate = profile.showBirthDate
            showCountry = profile.showCountry
            showLastSeen = profile.showLastSeen
        } else {
            country = LocationUtils.detectCountry(context)
        }
        isLoadingProfile = false
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            isUploadingPhoto = true
            uploadError = null
            scope.launch {
                try {
                    val url = repo.uploadProfilePhoto(context, uri, BuildConfig.IMGBB_API_KEY)
                    photoUrl = url
                    repo.updateProfile(uid, mapOf("photoUrl" to url))
                } catch (e: Exception) {
                    uploadError = "فشل رفع الصورة، حاول مرة تانية"
                } finally {
                    isUploadingPhoto = false
                }
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
        isSaving = true
        scope.launch {
            repo.updateProfile(uid, mapOf(
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
            isSaving = false
            onBack()
        }
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
                    TextButton(onClick = { saveProfile() }, enabled = !isSaving) {
                        if (isSaving) {
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
        if (isLoadingProfile) {
            SilaLoading(modifier = Modifier.fillMaxSize().padding(padding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // ===== قسم: الملف الشخصي (Profile) =====
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    SilaAvatar(
                        name = displayName,
                        photoUrl = photoUrl,
                        seed = uid,
                        size = 110.dp,
                        isLoading = isUploadingPhoto
                    )
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
            }

            if (uploadError != null) {
                Text(
                    uploadError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            // ===== قسم: الحساب (Account) =====
            SilaSectionCard(title = "الحساب") {
                Text(
                    "الحالة",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 14.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("online" to "متصل", "away" to "بعيد", "invisible" to "غير ظاهر").forEach { (value, label) ->
                        FilterChip(
                            selected = status == value,
                            onClick = { status = value },
                            label = { Text(label) },
                            leadingIcon = if (value == "online" && status == value) {
                                { Box(modifier = Modifier.size(8.dp).background(Color(0xFF34C759), CircleShape)) }
                            } else null
                        )
                    }
                }
                Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Public,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "الحساب مرتبط بتسجيل دخول Google",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SilaPrimaryButton(
                text = if (isSaving) "جارِ الحفظ..." else "حفظ التغييرات",
                onClick = { saveProfile() },
                loading = isSaving
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
