package com.sila.messaging.ui

import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.sila.messaging.BuildConfig
import com.sila.messaging.data.LocationUtils
import com.sila.messaging.data.UserRepository
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

    LaunchedEffect(uid) {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الملف الشخصي", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
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
                        },
                        enabled = !isSaving
                    ) {
                        Text(if (isSaving) "جارِ الحفظ..." else "حفظ")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier.size(110.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = "صورة البروفايل",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(displayName.take(1).uppercase().ifBlank { "؟" }, color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                        }
                        if (isUploadingPhoto) {
                            Box(
                                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp)) }
                        }
                    }
                    IconButton(
                        onClick = { photoPicker.launch("image/*") },
                        modifier = Modifier.size(34.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = "تغيير الصورة", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (uploadError != null) {
                Text(uploadError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionCard {
                LabeledField("الاسم المعروض", displayName) { displayName = it }
                Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                LabeledField("اسم المستخدم", username, prefix = "@") { username = it }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionCard {
                LabeledField("نبذة شخصية", bio, singleLine = false) { bio = it }
                VisibilityToggle("إظهار النبذة للآخرين", showBio) { showBio = it }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { openDatePicker() }.padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تاريخ الميلاد", fontSize = 14.sp, color = Color.Gray)
                    Text(birthDate.ifBlank { "اختر التاريخ" }, fontWeight = FontWeight.Medium)
                }
                VisibilityToggle("إظهار تاريخ الميلاد للآخرين", showBirthDate) { showBirthDate = it }
                Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                LabeledField("البلد", country) { country = it }
                VisibilityToggle("إظهار البلد للآخرين", showCountry) { showCountry = it }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionCard {
                Text("الحالة", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(top = 12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("online" to "متصل", "away" to "بعيد", "invisible" to "غير ظاهر").forEach { (value, label) ->
                        FilterChip(selected = status == value, onClick = { status = value }, label = { Text(label) })
                    }
                }
                Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                VisibilityToggle("إظهار آخر ظهور والحالة المتصلة", showLastSeen) { showLastSeen = it }
                Text(
                    "إذا أخفيت آخر ظهور، لن تتمكن من رؤية آخر ظهور الآخرين أيضاً",
                    fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)).padding(horizontal = 16.dp),
        content = content
    )
}

@Composable
private fun LabeledField(label: String, value: String, prefix: String = "", singleLine: Boolean = true, onChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(2.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            singleLine = singleLine,
            prefix = if (prefix.isNotEmpty()) { { Text(prefix) } } else null,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun VisibilityToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
